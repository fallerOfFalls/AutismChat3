package net.richardprojects.autismchat3.events;

import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Config;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {
	
	private AutismChat3 plugin;
	
	public LeaveEvent(AutismChat3 plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void leaveEvent(PlayerQuitEvent e) {
		if(Config.redHidesLoginNotification) {
			String msg = e.getQuitMessage();
			e.setQuitMessage("");
			
			for(Player p : plugin.getServer().getOnlinePlayers()) {
				ACPlayer acPlayer = plugin.getACPlayer(p.getUniqueId());
				if (acPlayer != null && plugin.getACParty(acPlayer.getPartyId()) != null) {
					if (plugin.getACParty(acPlayer.getPartyId()).getColor() != Color.RED) {
						p.sendMessage(msg);
					}
				}
			}
		}		
	}
}
