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

import java.util.UUID;

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

public class RedCommand implements CommandExecutor {

	private AutismChat3 plugin;
	
	public RedCommand(AutismChat3 plugin) {
		this.plugin = plugin;		
	}
	
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			
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
			boolean shouldReturnToDefault = false;
			
			if (party == null) {
				// TODO: Write a method that can automatically recover from a 
				// player not having a party by making a new one for them and 
				// reporting it in the log
				
				return; // something really weird has happened
			}
			
			if (party.getMembers().size() > 1) {
				shouldReturnToDefault = true;
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
			
			if (party.getMembers().size() == 1) {
				if (shouldReturnToDefault) {
					// if previous party now only has one member set their color back to their default
					UUID lastPlayer = party.getMembers().get(0);
					if (lastPlayer != null && plugin.getACPlayer(lastPlayer) != null) {
						ACPlayer player = plugin.getACPlayer(lastPlayer);
						party.setColor(player.getDefaultColor());
						String msg = "";
						
						msg = Messages.prefix_Good + Messages.message_setDefault; 
						if (player.getDefaultColor() == Color.GREEN) {
							msg = msg.replace("{COLOR}", Messages.color_green + "Green&6");
						} else if (player.getDefaultColor() == Color.WHITE) {
							msg = msg.replace("{COLOR}", "&fWhite&6");
						} else if (player.getDefaultColor() == Color.YELLOW) {
							msg = msg.replace("{COLOR}", Messages.color_yellow + "Yellow&6");
						} else if (player.getDefaultColor() == Color.RED) {
							msg = msg.replace("{COLOR}", Messages.color_red + "Red&6");
						} else if (player.getDefaultColor() == Color.BLUE) {
							msg = msg.replace("{COLOR}", Messages.color_blue + "Blue&6");
						}
						msg = Utils.colorCodes(msg);
						
						if (plugin.getServer().getPlayer(lastPlayer) != null) {
							plugin.getServer().getPlayer(lastPlayer).sendMessage(msg);
						}
						
						// update team colors
						Utils.updateTeam(plugin, lastPlayer, player.getDefaultColor());
					}
				} else {
					party.setColor(Color.RED); // the player was alone and is just updating their color
				}
			}
			
			// send message last so it is after the message about leaving the party.
			if (plugin.getServer().getPlayer(player) != null) {
				Player mPlayer = plugin.getServer().getPlayer(player);
				String msg = Messages.prefix_Good + Messages.message_setRed;
				msg = msg.replace("{PLAYER}", Utils.formatName(plugin, player, mPlayer.getUniqueId()));
				msg = Utils.colorCodes(msg);
				mPlayer.sendMessage(msg);
			}
		}
		
	}
}