//Created by Christopher at 09.06.2024
package eu.lotusgaming.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import eu.lotusgaming.main.LotusController;
import eu.lotusgaming.misc.Gamestate;

public class JoinEvent implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		LotusController lc = new LotusController();
		if(LotusController.getGamestate() == Gamestate.AWAITING_PLAYERS) {
			//teleports to the waiting lobby and set to survival
		}else if(LotusController.getGamestate() == Gamestate.INGAME) {
			//teleports onto the map and set to spectator
		}else if(LotusController.getGamestate() == Gamestate.END) {
			//teleports to the waiting lobby and set to spectator
		}
	}

}