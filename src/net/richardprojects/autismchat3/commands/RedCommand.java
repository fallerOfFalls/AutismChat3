/*   This file is part of AutismChat3.
*
*    AutismChat3 is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License.
*
*    You can view a copy of the GNU General Public License below
*    http://www.gnu.org/licenses/
*/

package net.richardprojects.autismchat3.commands;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.richardprojects.autismchat3.ACParty;
import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RedCommand implements CommandExecutor {

	private AutismChat3 plugin;
	
	public RedCommand(AutismChat3 plugin) {
		this.plugin = plugin;		
	}
	
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
			
			if (args.length == 0) {
				Utils.updateTeam(plugin, player.getUniqueId(), Color.RED); // update team
				new SwitchRedTask(player.getUniqueId()).runTaskAsynchronously(plugin);	
			} else {
				String msg = Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs);
				player.sendMessage(msg);
				return false;
			}
		} else {
			sender.sendMessage("Only a player can execute this command.");
		}		
		return true;
	}
	
	/**
	 * Helper task that updates the player's party when they switch to red.
	 * 
	 * @author RichardB122
	 * @version 4/15/17
	 */
	private class SwitchRedTask extends BukkitRunnable {
		
		UUID player;
		
		public SwitchRedTask(UUID player) {
			this.player = player;
		}
		
		public void run() {
			
			ACPlayer acPlayer = plugin.getACPlayer(player);
			int cPartyId = acPlayer.getPartyId();
			ACParty party = plugin.getACParty(cPartyId);
			
			if (party != null && party.getMembers().size() > 1) {
				try {
					// message everyone
					for (UUID member : party.getMembers()) {
						Player cPlayer = plugin.getServer().getPlayer(member);
											
						if (cPlayer != null) {
							String msg = "";
							
							if (!member.equals(player)) {
								msg = Messages.message_leaveParty;
								String name = Utils.formatName(plugin, player, cPlayer.getUniqueId());
								msg = msg.replace("{PLAYER}", name);
								msg = msg.replace("{PLAYERS} {REASON}", Messages.reasonLeaveRed);
							} else {
								String list = Utils.partyMembersString(plugin, cPartyId, player);						
								msg = Messages.message_youLeaveParty;
								msg = msg.replace("{PLAYERS}", list);
								msg = msg.replace("{REASON}", Messages.reasonYouRed);
							}
							
							cPlayer.sendMessage(Utils.colorCodes(msg));
						}
					}
					
					party.removeMember(player); // remove player from old party
					
					// create a new party for the player
					int newPartyId = plugin.createNewParty(player, Color.RED);					
					acPlayer.setPartyId(newPartyId);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			// send message last so it is after the message about leaving the party.
			if (plugin.getServer().getPlayer(player) != null) {
				String msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setRed);
				plugin.getServer().getPlayer(player).sendMessage(msg);
			}
		}
		
	}
}