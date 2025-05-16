//Created by Christopher at 07.06.2024
package eu.lotusgaming.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import eu.lotusgaming.misc.Gamestate;
import eu.lotusgaming.misc.InputType;
import eu.lotusgaming.misc.Money;
import eu.lotusgaming.misc.MySQL;
import eu.lotusgaming.misc.Playerdata;
import eu.lotusgaming.misc.Prefix;
import eu.lotusgaming.misc.RAMInfo;
import eu.lotusgaming.misc.Serverdata;
import net.md_5.bungee.api.ChatColor;

public class LotusController {
	
	// < - - - INSTANCES FOR ALL SECTIONS GROUPED IN ORDER - - - >
	
	//Language System
	private static HashMap<String, HashMap<String, String>> langMap = new HashMap<>();
	public static HashMap<String, String> playerLanguages = new HashMap<>();
	private static List<String> availableLanguages = new ArrayList<>();

	//Prefix System
	private static HashMap<String, String> prefix = new HashMap<>();
	private static boolean useSeasonalPrefix = false;

	//Servername and ServerID
	private static String servername = "Server";
	private static String serverid = "0";

	//Gamestate
	private static Gamestate gamestate;
	private static String currentBuildTopic;

	public boolean initLanguageSystem() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM core_translations");
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd =  rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			int languageStrings = 0;
			int colToStartFrom = 0;
			if(rs.next()) {
				for(int i = 1; i <= columnCount; i++) {
					String name = rsmd.getColumnName(i);
					if(name.equals("German")) {
						colToStartFrom = i;
						break;
					}
				}
				HashMap<String, String> map;
				for(int i = colToStartFrom; i <= columnCount; i++) {
					String name = rsmd.getColumnName(i);
					availableLanguages.add(name);
					Main.logger.info("Logged language " + name + " to List");
					PreparedStatement ps1 = MySQL.getConnection().prepareStatement("SELECT path," + name + ",isGame FROM core_translations");
					ResultSet rs1 = ps1.executeQuery();
					map = new HashMap<>();
					int subLangStrings = 0;
					while(rs1.next()) {
						if(rs1.getBoolean("isGame")) {
							subLangStrings++;
							//Only get Strings, which are for the game (what would we do with website/bot string, right?)
							map.put(rs1.getString("path"), rs1.getString(name));
						}
					}
					languageStrings = subLangStrings;
					langMap.put(name, map);
				}
				Main.logger.info("langMap logged " + langMap.size() + " entries with each " + languageStrings + " entries per language.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return langMap.isEmpty();
	}

	public List<String> getAvailableLanguages() {
		return availableLanguages;
	}

	public boolean initPlayerLanguages() {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT mcuuid,language FROM mc_users");
			ResultSet rs = ps.executeQuery();
			int count = 0;
			while(rs.next()) {
				count++;
				playerLanguages.put(rs.getString("mcuuid"), rs.getString("language"));
			}
			rs.close();
			ps.close();
			Main.logger.info("Initialised " + count + " users for the language system. | Source: LotusController#initPlayerLanguages();");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return playerLanguages.isEmpty();
	}

	//only used, when a never-joined player joins the network.
	public void addPlayerLanguageWhenRegistered(Player player) {
		playerLanguages.put(player.getUniqueId().toString(), "English");
		Main.logger.info("Added " + player.getName() + " to the languageMap with default. | Source: LotusController#addPlayerLanguageWhenRegistered(PLAYER);");
	}

	//This method is used if no spaceholders needs to be translated additionally.
	public void sendMessageReady(Player player, String path) {
		player.sendMessage(getPrefix(Prefix.MAIN) + sendMessageToFormat(player, path));
	}

	//This method is used if spaceholders needs to be translated before sending (or if the target is NOT a player).
	public String sendMessageToFormat(Player player, String path) {
		String toReturn = returnString(returnLanguage(player), path);
		if(toReturn.equalsIgnoreCase("none")) {
			return returnString("English", path);
		}else {
			return toReturn;
		}
	}

	//This method is returns the player's selected language.
	public String returnLanguage(Player player) {
		String defaultLanguage = "English";
		if(playerLanguages.containsKey(player.getUniqueId().toString())) {
			defaultLanguage = playerLanguages.get(player.getUniqueId().toString());
		}
		return defaultLanguage;
	}

	//This method is just for one string, the NoPerm one
	public void noPerm(Player player, String lackingPermissionNode) {
		player.sendMessage(getPrefix(Prefix.System) + sendMessageToFormat(player, "global.noPermission").replace("%permissionNode%", lackingPermissionNode));
	}

	//This method returns the String from the language selected.
	private String returnString(String language, String path) {
		if(langMap.containsKey(language)) {
			HashMap<String, String> localMap = langMap.get(language);
			if(localMap.containsKey(path)) {
				return ChatColor.translateAlternateColorCodes('&', localMap.get(path));
			}else {
				return "The path '" + path + "' does not exist!";
			}
		}else {
			return "The language '" + language + "' does not exist!";
		}
	}

	// < - - - END OF LANGUAGE SYSTEM - - - >
	// < - - - BEGIN OF THE PREFIX SYSTEM - - - >

	//initialise the Prefix System (also used to re-load it after a command reload)
	public void initPrefixSystem() {
		if(!prefix.isEmpty()) prefix.clear();

		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM mc_prefix");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				if(rs.getString("type").equalsIgnoreCase("UseSeason")) {
					useSeasonalPrefix = translateToBool(rs.getString("prefix"));
					if(useSeasonalPrefix) {
						Main.logger.info("Using Seasonal Prefix | Source: LotusController#initPrefixSystem()");
					}else {
						Main.logger.info("Using Normal Prefix | Source: LotusController#initPrefixSystem()");
					}
				}
				prefix.put(rs.getString("type"), rs.getString("prefix").replace('&', '§'));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean translateToBool(String input) {
		switch(input) {
		case "TRUE": return true;
		case "FALSE": return false;
		default: return false;
		}
	}

	//get Prefix with the Enum class "eu.lotusgc.mc.misc.Prefix"
	public String getPrefix(Prefix prefixType) {
		String toReturn = "";
		switch(prefixType) {
		case MAIN: if(useSeasonalPrefix) { toReturn = prefix.get("SEASONAL_MAIN"); } else { toReturn = prefix.get("MAIN"); }
		break;
		case PMSYS: toReturn = prefix.get("PMSYS");
		break;
		case SCOREBOARD: if(useSeasonalPrefix) { toReturn = prefix.get("SEASONAL_SB"); } else { toReturn = prefix.get("SCOREBOARD"); }
		break;
		case System: toReturn = prefix.get("SYSTEM");
		break;
		default: toReturn = prefix.get("MAIN");
		break;
		}
		return toReturn;
	}

	// < - - - END OF PREFIX SYSTEM - - - >
	// < - - - BEGIN OF THE MONEY API - - - >

	public double getMoney(Player player, Money type) {
		double money = 0.0;
		if(type == Money.BANK) {
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT money_bank FROM mc_users WHERE mcuuid = ?");
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					money = rs.getDouble("money_bank");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else if(type == Money.POCKET) {
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT money_pocket FROM mc_users WHERE mcuuid = ?");
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					money = rs.getDouble("money_pocket");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return money;
	}

	public int getInterestLevel(Player player) {
		int interestLevel = 0;
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT money_interestLevel FROM mc_users WHERE mcuuid = ?");
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				interestLevel = rs.getInt("money_interestLevel");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return interestLevel;
	}

	public boolean hasAccount(Player player) {
		return true;
		//Automatic true, as upon joining the server everything will be created in database - thus also has an account!
	}

	public void setMoney(Player player, double money, Money type) {
		BigDecimal dec = new BigDecimal(money).setScale(2, RoundingMode.DOWN);
		if(type == Money.BANK) {
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_users SET money_bank = ? WHERE mcuuid = ?");
				ps.setDouble(1, dec.doubleValue());
				ps.setString(2, player.getUniqueId().toString());
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else if(type == Money.POCKET) {
			try {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_users SET money_pocket = ? WHERE mcuuid = ?");
				ps.setDouble(1, dec.doubleValue());
				ps.setString(2, player.getUniqueId().toString());
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void addMoney(Player player, double moneyToAdd, Money type) {
		double oldMoney = getMoney(player, type);
		double newMoney = (oldMoney + moneyToAdd);
		setMoney(player, newMoney, type);
	}

	//if return is true, enough funds were there and a transaction was made; if false, no transaction was made and no money was removed.
	public boolean removeMoney(Player player, double moneyToRemove, Money type) {
		boolean hadEnoughFunds = false;
		double oldMoney = getMoney(player, type);
		if(moneyToRemove > oldMoney) {
			hadEnoughFunds = false;
		}else {
			double newMoney = (oldMoney - moneyToRemove);
			setMoney(player, newMoney, type);
			hadEnoughFunds = true;
		}
		return hadEnoughFunds;
	}

	public void setInterestLevel(Player player, int newInterestLevel) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE mc_users SET money_interestLevel = ? WHERE mcuuid = ?");
			ps.setInt(1, newInterestLevel);
			ps.setString(2, player.getUniqueId().toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasEnoughFunds(Player player, double moneyToCheck, Money type) {
		double current = getMoney(player, type);
		return (current > moneyToCheck);
	}

	// < - - - END OF THE MONEY API - - - >
	// < - - - BEGIN OF THE MISC UTILS - - - >

	//load server id and name into cache
	public void loadServerIDName() {
		File file = new File("server.properties");
		Properties p = new Properties();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))){
			p.load(bis);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		servername = p.getProperty("server-name");
		serverid = p.getProperty("server-id");
	}

	//get the server name
	public String getServerName() {
		return servername;
	}

	//get the server id
	public String getServerId() {
		return serverid;
	}

	//Original by Grubsic (LGC Vice Project Leader) | Thank you for your contributions! <3
	private static final Pattern HEX_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}");
	public static String translateHEX(String text) {
		Matcher matcher = HEX_PATTERN.matcher(text);
		while(matcher.find()) { text = text.replace(matcher.group(), ChatColor.of(matcher.group()).toString()); }
		return text;
	}

	public String getServerData(String servername, Serverdata data, InputType type) {
		String toReturn = "";
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT " + data.getColumnName() + " FROM mc_serverstats WHERE " + type.getColumnName() + " = ?");
			ps.setString(1, servername);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				toReturn = rs.getString(data.getColumnName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	public HashMap<String, String> getServerData(String servername) {
		HashMap<String, String> hashMap = new HashMap<>();
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM mc_serverstats WHERE servername = ?");
			ps.setString(1, servername);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				for(Serverdata data : Serverdata.values()) {
					hashMap.put(data.getColumnName(), rs.getString(data.getColumnName()));
				}
			}else {
				for(Serverdata data : Serverdata.values()) {
					hashMap.put(data.getColumnName(), "none");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hashMap;
	}

	public String getPlayerData(Player player, Playerdata data) {
		String toReturn = "";
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT " + data.getColumnName() + " FROM mc_users WHERE mcuuid = ?");
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				toReturn = rs.getString(data.getColumnName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	public HashMap<String, String> getPlayerData(Player player){
		HashMap<String, String> hashMap = new HashMap<>();
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM mc_users WHERE mcuuid = ?");
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				for(Playerdata data : Playerdata.values()) {
					hashMap.put(data.getColumnName(), rs.getString(data.getColumnName()));
				}
			}else {
				for(Playerdata data : Playerdata.values()) {
					hashMap.put(data.getColumnName(), "none");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hashMap;
	}

	public String getRAMInfo(RAMInfo type) {
		String toReturn = "";
		Runtime runtime = Runtime.getRuntime();
		if(type == RAMInfo.ALLOCATED) {
			toReturn = runtime.totalMemory() / 1048576L + "";
		}else if(type == RAMInfo.USING) {
			toReturn = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L + "";
		}else if(type == RAMInfo.FREE) {
			toReturn = runtime.freeMemory() / 1048576L + "";
		}
		return toReturn;
	}

	public boolean translateBoolean(String input) {
		switch(input) {
		case "0": return false;
		case "false": return false;
		case "1": return true;
		case "true": return true;
		default: Main.logger.severe("Error in LotusController#translateBoolean() - expected 0,1,true,false but got " + input); return false;
		}
	}

	public int translateInt(String input) {
		if(input.matches("^[0-9]+-$")) {
			return Integer.parseInt(input);
		}else {
			return -1;
		}
	}

	public String translateJoinLevel(String input) {
		switch(input) {
		case "ALPHA": return "§cAlpha";
		case "BETA": return "§dBeta";
		case "EVERYONE": return "§aEveryone";
		case "STAFF": return "§cStaff";
		default: Main.logger.severe("Error in LotusController#translateJoinLevel() - expected ALPHA,BETA,STAFF,EVERYONE but got " + input); return "§aEveryone";
		}
	}
	
	public static Gamestate getGamestate() {
		return gamestate;
	}
	
	public static void setGamestate(Gamestate state) {
		gamestate = state;
	}
	
	public static String getBuildTopic() {
		return currentBuildTopic;
	}
	
	public static void setBuildTopic(String newBuildTopic) {
		currentBuildTopic = newBuildTopic;
	}

}