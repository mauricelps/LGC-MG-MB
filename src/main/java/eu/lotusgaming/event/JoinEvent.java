//Created by Christopher at 09.06.2024
package eu.lotusgaming.event;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import eu.lotusgaming.command.SpawnHandler;
import eu.lotusgaming.main.LotusController;
import eu.lotusgaming.misc.Gamestate;

public class JoinEvent implements Listener {
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		if(LotusController.getGamestate() == Gamestate.END || LotusController.getGamestate() == Gamestate.INGAME || LotusController.getGamestate() == Gamestate.VOTING) {
			if(event.getPlayer().hasPermission("lgc.masterbuilders.joinMidgame")) {
				event.setResult(Result.ALLOWED);
			}else {
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("§cYou cannot join the game at this time! Please try again later.");
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		LotusController lc = new LotusController();
		if(LotusController.getGamestate() == Gamestate.AWAITING_PLAYERS) {
			//teleports to the waiting lobby and set to survival
			player.teleport(new SpawnHandler().getLocationPoint("preGame"));
		}else if(LotusController.getGamestate() == Gamestate.INGAME) {
			//teleports onto the map and set to spectator
			player.teleport(new SpawnHandler().getLocationPoint("midGame"));
			player.setGameMode(GameMode.SPECTATOR);
		}else if(LotusController.getGamestate() == Gamestate.VOTING) {
			player.teleport(new SpawnHandler().getLocationPoint("votingGame"));
		}else if(LotusController.getGamestate() == Gamestate.END) {
			//teleports to the waiting lobby and set to spectator
			player.teleport(new SpawnHandler().getLocationPoint("postGame"));
		}
	}

}