//Created by Maurice H. at 26.09.2024
package eu.lotusgaming.command;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import eu.lotusgaming.main.LotusController;
import eu.lotusgaming.main.LotusManager;
import eu.lotusgaming.main.Main;
import eu.lotusgaming.misc.Prefix;

public class SpawnHandler implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			LotusController lc = new LotusController();
			if(args.length == 1) {
				if(player.hasPermission("lgc.masterbuilders.setspawn")) {
					String key = args[0];
					if(key.equalsIgnoreCase("preGame") || key.equalsIgnoreCase("midGame") || key.equalsIgnoreCase("postGame")) {
						setLocationPoint(player.getLocation(), key);
						player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "mg_mb.command.setspawn.primaryKeys").replace("%key%", key));
					}else {
						setLocationPoint(player.getLocation(), key);
						player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "mg_mb.command.setspawn.success").replace("%key%", key));
					}
				}else {
					lc.noPerm(player, "lgc.masterbuilders.setspawn");
				}
			}else {
				player.sendMessage(lc.getPrefix(Prefix.MAIN) + lc.sendMessageToFormat(player, "global.args") + "/setspawn <Key> | Keys: preGame|midGame|postGame");
			}
		}else {
			sender.sendMessage(Main.consoleSend);
		}
		return true;
	}
	
	void setLocationPoint(Location location, String key) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File(LotusManager.folderName + "/spawn.yml"));
		cfg.set(key + ".X", location.getX());
		cfg.set(key + ".Y", location.getY());
		cfg.set(key + ".Z", location.getZ());
		cfg.set(key + ".Yaw", location.getYaw());
		cfg.set(key + ".Pitch", location.getPitch());
		cfg.set(key + ".World", location.getWorld().getName());
		try {
			cfg.save(new File(LotusManager.folderName + "/spawn.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Location getLocationPoint(String key) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File(LotusManager.folderName + "/spawn.yml"));
		double x = cfg.getDouble(key + ".X");
		double y = cfg.getDouble(key + ".Y");
		double z = cfg.getDouble(key + ".Z");
		float yaw = (float) cfg.getDouble(key + ".Yaw");
		float pitch = (float) cfg.getDouble(key + ".Pitch");
		String world = cfg.getString(key + ".World");
		return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
	}
}