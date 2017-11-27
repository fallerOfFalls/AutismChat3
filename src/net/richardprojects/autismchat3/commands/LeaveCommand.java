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
		if(sender instanceof Player) {
			final Player player = (Player) sender;
			this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
				public void run() {
					if(args.length == 0) {
						int partyId = plugin.getACPlayer(player.getUniqueId()).getPartyId();
						ACParty party = plugin.getACParty(partyId);
						
						if (party != null) {
							Color oldPartyColor = party.getColor();
							if (party.getMembers().size() > 1) {
								try {
									// create a new party and update id
									ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
									int newPartyId = plugin.createNewParty(player.getUniqueId(), acPlayer.getDefaultColor());
									acPlayer.setPartyId(newPartyId);
									
									party.removeMember(player.getUniqueId()); // remove player from old party
									
									// notify old party member that they have left the party
									for (UUID uuid : party.getMembers()) {
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
									String memberList = Utils.partyMembersString(plugin, partyId, player.getUniqueId());								
									String msg = Messages.message_youLeaveParty;
									msg = msg.replace("has", "have");
									msg = msg.replace("{PLAYERS}", memberList);
									msg = msg.replace(" {REASON}", "");
									player.sendMessage(Utils.colorCodes(msg));
									
									// notify player who left they have been switched to default color
									if (oldPartyColor != acPlayer.getDefaultColor()) {
										if (acPlayer.getDefaultColor() == Color.GREEN) {
											msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setGreen);
										} else if (acPlayer.getDefaultColor() == Color.WHITE) {
											msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setWhite);
										} else if (acPlayer.getDefaultColor() == Color.YELLOW) {
											msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setYellow);
										} else if (acPlayer.getDefaultColor() == Color.RED) {
											msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setRed);
										} else if (acPlayer.getDefaultColor() == Color.BLUE) {
											msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setBlue);
										} else {
											msg = "";
										}
										
										// actually message player and update team color
										player.sendMessage(msg);
										Utils.updateTeam(plugin, player.getUniqueId(), acPlayer.getDefaultColor());
									}
									
									// set party color to match player's default color
									if (party.getMembers().size() == 1) {
										UUID lastPlayer = party.getMembers().get(0);
										if (lastPlayer != null && plugin.getACPlayer(lastPlayer) != null) {
											ACPlayer player = plugin.getACPlayer(lastPlayer);
											if (plugin.getACParty(player.getPartyId()) != null) {
												plugin.getACParty(player.getPartyId()).setColor(player.getDefaultColor());
												
												if (player.getDefaultColor() == Color.GREEN) {
													msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setGreen);
												} else if (player.getDefaultColor() == Color.WHITE) {
													msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setWhite);
												} else if (player.getDefaultColor() == Color.YELLOW) {
													msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setYellow);
												} else if (player.getDefaultColor() == Color.RED) {
													msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setRed);
												} else if (player.getDefaultColor() == Color.BLUE) {
													msg = Utils.colorCodes(Messages.prefix_Good + Messages.message_setBlue);
												}
												
												if (plugin.getServer().getPlayer(lastPlayer) != null) {
													plugin.getServer().getPlayer(lastPlayer).sendMessage(msg);
												}
												
												// update team colors
												Utils.updateTeam(plugin, lastPlayer, player.getDefaultColor());
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
					} else {
						String msg = Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs);
						player.sendMessage(msg);
						player.sendMessage("/leave");
					}
				}
			});
		} else {
			sender.sendMessage("Only a player can execute this command.");
		}
		
		return true;
	}

}
