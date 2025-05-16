package eu.lotusgaming.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;

public class Main extends JavaPlugin {
	
	public static Main main;
	public static Logger logger;
	public static LuckPerms luckPerms;
	public static String consoleSend = "§cPlease execute this command in-Game!";

    @Override
    public void onEnable() {
    	main = this;
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.ALL);
		
		LotusManager mgr = new LotusManager();
		mgr.preInit();
		mgr.init();
		mgr.postInit();
    }

}
