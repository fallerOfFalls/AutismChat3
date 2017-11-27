/*   This file is part of AutismChat3.
*
*    AutismChat3 is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License.
*
*    You can view a copy of the GNU General Public License below
*    http://www.gnu.org/licenses/
*/

package net.richardprojects.autismchat3.events;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.richardprojects.autismchat3.ACParty;
import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
	
	private AutismChat3 plugin;
	
	public ChatEvent(AutismChat3 plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void chatEvent(AsyncPlayerChatEvent e) {
		String chatMsg = e.getMessage();
		e.setCancelled(true);
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		ACPlayer acPlayer = plugin.getACPlayer(uuid);
		ACParty acParty = plugin.getACParty(acPlayer.getPartyId());
		
		if (acParty != null) {
			String playerName = Color.colorCode(acParty.getColor()) + player.getName();
			
			int partyID = acPlayer.getPartyId();
			ACParty party = plugin.getACParty(partyID);
			
			if (party != null) {
				int playersSentTo = 0;
				for (UUID cUUID : party.getMembers()) {
					Player cPlayer = plugin.getServer().getPlayer(cUUID);
					
					if (cPlayer != null) {
						String msg = Messages.partyChatFormat;
						msg = msg.replace("%name%", playerName + ChatColor.RESET);
						msg = msg.replace("%message%", chatMsg);
						msg = Utils.colorCodes(msg);
						cPlayer.sendMessage(msg);
						playersSentTo++;
					}
				}
				
				// notify the player if nobody heard their message
				if(playersSentTo == 1 || playersSentTo == 0) {
					String msg = Messages.prefix_Bad + Messages.message_nobodyHeardMessage;
					player.sendMessage(Utils.colorCodes(msg));
				}
			}
		}
	}
}