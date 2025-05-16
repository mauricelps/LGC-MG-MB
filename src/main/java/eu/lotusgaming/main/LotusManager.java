//Created by Christopher at 06.06.2024
package eu.lotusgaming.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import eu.lotusgaming.misc.MySQL;
import net.luckperms.api.LuckPerms;

public class LotusManager {
	
	public static String folderName = "plugins/LotusGaming";
	public static File mainFolder = new File(folderName);
	public static File mainConfig = new File(folderName + "/config.yml");
	public static File spawnConfig = new File(folderName + "/spawn.yml");
	public static boolean useProtocolLib = false;
	
	public void preInit() {
		long old = System.currentTimeMillis();
		
		if(!mainFolder.exists()) mainFolder.mkdirs();
		if(!mainConfig.exists()) try { mainConfig.createNewFile(); } catch (Exception ex) { };
		if(!spawnConfig.exists()) try { spawnConfig.createNewFile(); } catch (Exception ex) { };
		
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(mainConfig);
		cfg.addDefault("MySQL.Host", "127.0.0.1");
		cfg.addDefault("MySQL.Port", "3306");
		cfg.addDefault("MySQL.Database", "TheDataBaseTM");
		cfg.addDefault("MySQL.Username", "user");
		cfg.addDefault("MySQL.Password", "pass");
		
		cfg.addDefault("Game.MinPlayers", 2);
		cfg.addDefault("Game.MaxPlayers", 8);
		cfg.addDefault("Game.MaxGameTimeInSeconds", 300);
		
		List<String> builds = new ArrayList<>();
		builds.add("Spider");
		builds.add("Sheep");
		builds.add("Creeper");
		builds.add("House");
		builds.add("Oak Tree");
		builds.add("Ship");
		builds.add("Castle");
		cfg.addDefault("Game.BuildTopics", builds);
		
		cfg.options().copyDefaults(true);
		
		if(!cfg.getString("MySQL.Password").equalsIgnoreCase("pass")) {
			MySQL.connect(cfg.getString("MySQL.Host"), cfg.getString("MySQL.Port"), cfg.getString("MySQL.Database"), cfg.getString("MySQL.Username"), cfg.getString("MySQL.Password"));
		}
		
		Bukkit.getConsoleSender().sendMessage("§aPre-Initialisation took §e" + (System.currentTimeMillis() - old) + "ms");
	}
	
	public void init() {
		long old = System.currentTimeMillis();
		
		
		
		Bukkit.getConsoleSender().sendMessage("§aMain Initialisation took §e" + (System.currentTimeMillis() - old) + "ms");
	}
	
	public void postInit() {
		long old = System.currentTimeMillis();
		
		
		if(Bukkit.getPluginManager().getPlugin("LuckPerms")!=null) {
			Main.luckPerms = (LuckPerms) Bukkit.getServer().getServicesManager().load(LuckPerms.class);
		}
		if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
			useProtocolLib = true;
		}
		
		Bukkit.getConsoleSender().sendMessage("§aPost-Initialisation took §e" + (System.currentTimeMillis() - old) + "ms");
	}

}