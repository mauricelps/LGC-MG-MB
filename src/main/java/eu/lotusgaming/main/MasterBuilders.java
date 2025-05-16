//Created by Maurice H. at 13.04.2025
package eu.lotusgaming.main;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class MasterBuilders {
	
	int minPlayers, maxPlayers, maxPlaytime;
	List<String> buildTopics;
	
	public MasterBuilders() {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(LotusManager.mainConfig);
		minPlayers = cfg.getInt("Game.MinPlayers");
		if (minPlayers <= 1) {
			minPlayers = 2;
		}
		if (minPlayers >= 9) {
			minPlayers = 8;
		}
		maxPlayers = cfg.getInt("Game.MaxPlayers");
		if(maxPlayers <= 2) {
			maxPlayers = 3;
		}
		if (maxPlayers >= 9) {
			maxPlayers = 8;
		}
		maxPlaytime = cfg.getInt("Game.MaxGameTimeInSeconds");
		if(maxPlaytime <= 59) {
			maxPlaytime = 60;
		}
		if(maxPlaytime >= 601) {
			maxPlaytime = 600;
		}
		buildTopics = cfg.getStringList("Game.BuildTopics");
	}
	
	public int getMinPlayers() {
		return minPlayers;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public int getMaxPlaytime() {
		return maxPlaytime;
	}
	
	public List<String> getBuildTopics() {
		return buildTopics;
	}
}