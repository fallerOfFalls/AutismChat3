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

import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.ACParty;
import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

	private AutismChat3 plugin;
	
	public LeaveCommand(AutismChat3 plugin) {
		this.plugin = plugin;		
	}
	
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			final String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("Only a player can execute this command.");
		}
			
		final Player player = (Player) sender;
		this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
			public void run() {
				
				// handle invalid arguments
				if(args.length != 0) {
					 player.sendMessage(Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs));
					 player.sendMessage("/leave");
				}
					
				ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
				ACParty acParty = acPlayer != null ? plugin.getACParty(acPlayer.getPartyId()) : null;
				
				if (acParty != null) {
					Color oldPartyColor = acParty.getColor();
					if (acParty.getMembers().size() > 1) {
						try {
							// remove player from old party
							acParty.removeMember(player.getUniqueId());
							acPlayer.setPartyId(-1);
							
							// notify old party member that they have left the party
							for (UUID uuid : acParty.getMembers()) {
								if (!uuid.equals(player.getUniqueId())) {
									Player cPlayer = plugin.getServer().getPlayer(uuid);
									
									// send leave party message
									if (cPlayer != null) {												
										String msg = Messages.message_leaveParty;
										String pName = Utils.formatName(plugin, player.getUniqueId(), cPlayer.getUniqueId());
										msg = msg.replace("{PLAYER}", pName);
										msg = msg.replace(" {PLAYERS} {REASON}", "");
										cPlayer.sendMessage(Utils.colorCodes(msg));
									}
								}
							}
							
							// send message to player who just left
							String memberList = Utils.partyMembersString(plugin, acParty.getId(), player.getUniqueId());								
							String msg = Messages.message_youLeaveParty;
							msg = msg.replace("has", "have");
							msg = msg.replace("{PLAYERS}", memberList);
							msg = msg.replace(" {REASON}", "");
							player.sendMessage(Utils.colorCodes(msg));
							
							// notify player who left they have been switched to default color
							if (oldPartyColor != acPlayer.getDefaultColor()) {
								msg = Messages.prefix_Good + Messages.message_setDefault; 
								msg = msg.replace("{COLOR}", Utils.formatColor(acPlayer.getDefaultColor()));
								
								// actually message player and update team color
								msg = Utils.colorCodes(msg);
								player.sendMessage(msg);
								Utils.updateTeam(plugin, player.getUniqueId(), acPlayer.getDefaultColor());
							}
							
							// set party color to match player's default color
							if (acParty.getMembers().size() == 1) {
								UUID lastPlayerUUID = acParty.getMembers().get(0);
								
								if (lastPlayerUUID != null && plugin.getACPlayer(lastPlayerUUID) != null) {
									ACPlayer lastPlayer = plugin.getACPlayer(lastPlayerUUID);
									if (plugin.getACParty(lastPlayer.getPartyId()) != null) {
										plugin.getACParty(lastPlayer.getPartyId()).setColor(lastPlayer.getDefaultColor());
										
										msg = Messages.prefix_Good + Messages.message_setDefault; 
										msg = msg.replace("{COLOR}", Utils.formatColor(lastPlayer.getDefaultColor()));
										msg = Utils.colorCodes(msg);
										
										if (plugin.getServer().getPlayer(lastPlayerUUID) != null) {
											plugin.getServer().getPlayer(lastPlayerUUID).sendMessage(msg);
										}
										
										// update team colors
										Utils.updateTeam(plugin, lastPlayerUUID, lastPlayer.getDefaultColor());
									}
									
								}
							}									
						} catch(Exception e) {
							e.printStackTrace();
						}
					} else {
						player.sendMessage(Utils.colorCodes(Messages.prefix_Bad + Messages.message_onlyOneInParty));
					}
				}
			}
		});
		
		return true;
	}

}
