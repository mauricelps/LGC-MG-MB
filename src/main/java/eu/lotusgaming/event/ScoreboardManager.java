//Created by Maurice H. at 30.09.2024
package eu.lotusgaming.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import eu.lotusgaming.main.LotusController;
import eu.lotusgaming.main.LotusManager;
import eu.lotusgaming.main.Main;
import eu.lotusgaming.misc.Gamestate;
import eu.lotusgaming.misc.MySQL;
import eu.lotusgaming.misc.Playerdata;
import eu.lotusgaming.misc.Prefix;
import eu.lotusgaming.misc.ServerRestarter;
import eu.lotusgaming.misc.util.LotusPlayerGame;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

public class ScoreboardManager {
	
	private static HashMap<String, String> tabHM = new HashMap<>(); //HashMap for Tab
	private static HashMap<String, String> chatHM = new HashMap<>(); //HashMap for Chat
	private static HashMap<String, String> roleHM = new HashMap<>(); //HashMap for Team Priority (Sorted)
	private static HashMap<String, String> sbHM = new HashMap<>(); //HashMap for Sideboard (Like Chat, just with no additional chars)
	
	public void setScoreboard(Player player) {
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective o = sb.registerNewObjective("aaa", Criteria.DUMMY, "LGCINFOBOARD");
		LotusController lc = new LotusController();
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName("§bMaster§9Builders");
		
		Gamestate gs = LotusController.getGamestate();
		LotusPlayerGame lpg = new LotusPlayerGame(player);
		if(gs == Gamestate.AWAITING_PLAYERS) {
			o.getScore("§7» Wins: §a" + lpg.getWins()).setScore(0);
			o.getScore("§7» Losses: §a" + lpg.getLosses()).setScore(0);
			o.getScore("§6§a").setScore(0);
			o.getScore("§7» Points: §6" + lpg.getPoints()).setScore(0);
		}else if(gs == Gamestate.INGAME) {
			//remaining time, build topic
		}
		player.setScoreboard(sb);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		LotusController lc = new LotusController();
		String message = ChatColor.translateAlternateColorCodes('&', event.getMessage().replace("%", "%%"));
		
		event.setFormat(player.getDisplayName() + " §7(" + lc.getPlayerData(player, Playerdata.LotusChangeID)+ "): " + message);
		
	}
	
	String colorisePing(int ping) {
		String toReturn = "";
		if(ping >= 0 && ping <= 99) {
			toReturn = "§a" + ping + "§7ms";
		}else if(ping >= 100 && ping <= 250) {
			toReturn = "§e" + ping + "§7ms";
		}else if(ping >= 251 && ping <= 400) {
			toReturn = "§c" + ping + "§7ms";
		}else if(ping >= 401) {
			toReturn = "§4" + ping + "§7ms";
		}
		return toReturn;
	}
	
	public Team getTeam(Scoreboard scoreboard, String role, ChatColor chatcolor) {
		Team team = scoreboard.registerNewTeam(returnPrefix(role, RankType.TEAM));
		team.setPrefix(returnPrefix(role, RankType.TAB));
		team.setColor(chatcolor);
		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER); //TBD for removal if issues arise.
		return team;
	}
	
	public static void initRoles() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM core_ranks");
			ResultSet rs = ps.executeQuery();
			tabHM.clear();
			chatHM.clear();
			roleHM.clear();
			sbHM.clear();
			int count = 0;
			while(rs.next()) {
				count++;
				tabHM.put(rs.getString("ingame_id"), rs.getString("colour") + rs.getString("short"));
				chatHM.put(rs.getString("ingame_id"), rs.getString("colour") + rs.getString("name"));
				roleHM.put(rs.getString("ingame_id"), rs.getString("priority"));
				sbHM.put(rs.getString("ingame_id"), rs.getString("name"));
			}
			Main.logger.info("Downloaded " + count + " roles for the Prefix System. | Source: ScoreboardHandler#initRoles();");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//needs World#getTime()
	private String parseTimeWorld(long time) {
		long gameTime = time;
		long hours = gameTime / 1000 + 6;
		long minutes = (gameTime % 1000) * 60 / 1000;
		String ampm = "AM";
		if(hours >= 12) {
			hours -= 12; ampm = "PM";
		}
		if(hours >= 12) {
			hours -= 12; ampm = "AM";
		}
		if(hours == 0) hours = 12;
		String mm = "0" + minutes;
		mm = mm.substring(mm.length() - 2, mm.length());
		return hours + ":" + mm + " " + ampm;
	}
	
	private String parseWorldWeather(World world) {
		if(world.isThundering() && world.hasStorm()) {
			//thunder
			return "Thundering";
		}else if(!world.isThundering() && world.hasStorm()) {
			//rain only
			return "Storming";
		}else if(!world.isThundering() && !world.hasStorm()) {
			//clear
			return "Clear";
		}else {
			return "UK";
		}
	}
	
	private String returnPrefix(String role, RankType type) {
		String toReturn = "";
		if(type == RankType.TAB) {
			if(tabHM.containsKey(role)) {
				toReturn = tabHM.get(role) + " §7» ";
			}else {
				toReturn = "&cDEF";
			}
		}else if(type == RankType.CHAT) {
			if(chatHM.containsKey(role)) {
				toReturn = chatHM.get(role) + " §7» ";
			}else {
				toReturn = "&cDEF";
			}
		}else if(type == RankType.SIDEBOARD) {
			if(sbHM.containsKey(role)) {
				toReturn = sbHM.get(role);
			}else {
				toReturn = "DEF";
			}
		}else if(type == RankType.TEAM) {
			if(roleHM.containsKey(role)) {
				toReturn = roleHM.get(role);
			}else {
				Random r = new Random();
				toReturn = "0" + r.nextInt(0, 250) + "0";
			}
		}
		toReturn = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', toReturn); //transforms & -> §
		toReturn = LotusController.translateHEX(toReturn); //translates HEX Color Codes into Minecraft (Custom Color Codes ability)
		return toReturn;
	}
	
	public enum RankType {
		TAB,
		SIDEBOARD,
		CHAT,
		TEAM
	}
	
	private String translatePercentIntoColorCode(int input) {
		if(input >= 0 && input <= 19) {
			return "§2";
		}else if(input >= 20 && input <= 39) {
			return "§a";
		}else if(input >= 40 && input <= 59) {
			return "§e";
		}else if(input >= 60 && input <= 79) {
			return "§6";
		}else if(input >= 80 && input <= 89) {
			return "§c";
		}else if(input >= 90 && input <= 100) {
			return "§4";
		}else {
			return "§9";
		}
	}
	
	private String translateBooleanCustom(boolean input, String positive, String negative) {
		if(input) {
			return positive;
		}else {
			return negative;
		}
	}
	
	public void startScheduler(int delay, int sideboardRefresh, int tabRefresh) {
		//SYNC TASK - ONLY FOR THE SIDEBOARD
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player all : Bukkit.getOnlinePlayers()) {
					setScoreboard(all);
				}
			}
		}.runTaskTimer(Main.main, delay, sideboardRefresh);
		
		//For tasks which needs to run on main thread
		new BukkitRunnable() {
			@Override
			public void run() {
				new ServerRestarter().triggerRestart();
			}
		}.runTaskTimer(Main.main, delay, tabRefresh);
		
		//For tasks which can run on alternative threads (async)
		new BukkitRunnable() {
			@Override
			public void run() {
				LotusController lc = new LotusController();
				for(Player all : Bukkit.getOnlinePlayers()) {
					String timeZone = lc.getPlayerData(all, Playerdata.TimeZone);
					ZoneId zoneId = ZoneId.ofOffset("GMT", ZoneOffset.of(timeZone));
					SimpleDateFormat sdf = new SimpleDateFormat(lc.getPlayerData(all, Playerdata.CustomTimeFormat));
					sdf.setTimeZone(TimeZone.getTimeZone(Objects.requireNonNullElse(zoneId.getId(), "UTC")));
					all.setPlayerListHeaderFooter("§cLotus §aGaming §fCommunity", "§7Server: §a" + lc.getServerName() + "\n§7Time: §a" + sdf.format(new Date()) + "\n§7Ping: §a" + all.getPing());
				}
			}
		}.runTaskTimerAsynchronously(Main.main, delay, tabRefresh);
	}
}