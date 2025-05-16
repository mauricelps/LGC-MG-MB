//Created by Maurice H. at 13.04.2025
package eu.lotusgaming.misc.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import eu.lotusgaming.misc.MySQL;

public class LotusPlayerGame {
	
	private int points, wins, losses;
	private Player player;
	
	public LotusPlayerGame(Player player) {
		this.player = player;
		try(PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM mc_minigameusers WHERE mg_name = ? AND mg_user = ?")){
			ps.setString(1, "MasterBuilders");
			ps.setString(2, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				this.points = rs.getInt("points");
				this.wins = rs.getInt("wins");
				this.losses = rs.getInt("loose");
				rs.close();
				ps.close();
			}else {
				// Insert new Player
				PreparedStatement ps2 = MySQL.getConnection().prepareStatement("INSERT INTO mc_minigameusers (mg_name, mg_user) VALUES (?, ?)");
				ps2.setString(1, "MasterBuilders");
				ps2.setString(2, player.getUniqueId().toString());
				ps2.executeUpdate();
				ps2.close();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getPoints() {
		return points;
	}
	
	public int getWins() {
		return wins;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public void setPoints(int points) {
		this.points = points;
		try (PreparedStatement ps = MySQL.getConnection()
				.prepareStatement("UPDATE mc_minigameusers SET points = ? WHERE mg_name = ? AND mg_user = ?")) {
			ps.setInt(1, points);
			ps.setString(2, "MasterBuilders");
			ps.setString(3, this.player.getUniqueId().toString());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setWins(int wins) {
		this.wins = wins;
		try (PreparedStatement ps = MySQL.getConnection()
				.prepareStatement("UPDATE mc_minigameusers SET wins = ? WHERE mg_name = ? AND mg_user = ?")) {
			ps.setInt(1, wins);
			ps.setString(2, "MasterBuilders");
			ps.setString(3, this.player.getUniqueId().toString());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setLosses(int losses) {
		this.losses = losses;
		try (PreparedStatement ps = MySQL.getConnection()
				.prepareStatement("UPDATE mc_minigameusers SET loose = ? WHERE mg_name = ? AND mg_user = ?")) {
			ps.setInt(1, losses);
			ps.setString(2, "MasterBuilders");
			ps.setString(3, this.player.getUniqueId().toString());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}