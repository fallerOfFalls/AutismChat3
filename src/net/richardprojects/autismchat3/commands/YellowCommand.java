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

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.richardprojects.autismchat3.ACParty;
import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Config;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class YellowCommand implements CommandExecutor {
	
	private AutismChat3 plugin;
	
	public YellowCommand(AutismChat3 plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
			
			if (args.length == 0) {
				new SwitchYellowTask(player.getUniqueId(), player).runTaskAsynchronously(plugin);
				return true;
			} else {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("list")) {
						// send yellow statuses
						String[] statuses = Config.yellowStatuses.split(",");
						for (String status : statuses) {
							Utils.sendStatus(status, player.getUniqueId(), plugin);
						}
						
						return true;
					} else {
						sender.sendMessage(Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs));
						return false;
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("add")) {
						String playerName = args[1];
						if (!playerName.equalsIgnoreCase(player.getName())) {
							UUID newUUID = plugin.getUUID(playerName);
							
							List<UUID> yellowListMembers = acPlayer.getYellowList();	
							if(newUUID != null) {
								playerName = Utils.formatName(plugin, newUUID, player.getUniqueId());
								
								boolean addPersonToList = true;
								for(UUID member : yellowListMembers) {
									if(member.equals(newUUID)) {
										addPersonToList = false;
									}
								}
								
								if (addPersonToList) {
									String notification = Messages.prefix_Good + Messages.message_yellowAdd;
									notification = notification.replace("{TARGET}", playerName);
									acPlayer.addPlayerToYellowList(newUUID);
									player.sendMessage(Utils.colorCodes(notification));
									return true;
								} else {
									String notification = Messages.prefix_Bad + Messages.error_yellowDuplicate;
									notification = notification.replace("{TARGET}", playerName);
									player.sendMessage(Utils.colorCodes(notification));
									return true;
								}
							} else {
								String notification = Messages.prefix_Bad + Messages.error_notValidPlayer;
								notification = notification.replace("{TARGET}", playerName);
								player.sendMessage(Utils.colorCodes(notification));
								return true;
							}
						} else {
							String notification = Messages.prefix_Bad + "You cannot add yourself to your own yellow list.";
							player.sendMessage(Utils.colorCodes(notification));
						}
						return true;
					} else if (args[0].equalsIgnoreCase("remove")) {
						String playerName = args[1];
						if (!playerName.equalsIgnoreCase(player.getName())) {
							UUID newUUID = plugin.getUUID(playerName);

							List<UUID> yellowListMembers = acPlayer.getYellowList();							
							if (newUUID != null) {
								playerName = Utils.formatName(plugin, newUUID, player.getUniqueId());
								boolean removePersonFromList = false;
								for(UUID member : yellowListMembers) {
									if(member.equals(newUUID)) {
										removePersonFromList = true;
									}
								}
								
								if (removePersonFromList) {
									String notification = Messages.prefix_Good + Messages.message_yellowRemove;
									notification = notification.replace("{TARGET}", playerName);
									acPlayer.removePlayerFromYellowList(newUUID);
									player.sendMessage(Utils.colorCodes(notification));
									
									// check if the player is set to yellow and they are currently in a party with the person they just removed
									if (plugin.getACParty(acPlayer.getPartyId()).getColor() == Color.YELLOW) {
										int partyId = acPlayer.getPartyId();
										ACParty party = plugin.getACParty(partyId);
																				
										if (party != null) {
											List<UUID> partyMembers = party.getMembers();
											if (partyMembers.contains(newUUID)) {
												try {
													 // remove player from old party
													party.removeMember(player.getUniqueId());
													
													// create a new party and update the player's party id
													int newPartyId = plugin.createNewParty(player.getUniqueId(), Color.YELLOW);
													plugin.getACPlayer(player.getUniqueId()).setPartyId(newPartyId);
													
													// notify old party members that they have left the party
													for(UUID uuid2 : partyMembers) {
														if(!uuid2.equals(player.getUniqueId())) {
															Player cPlayer = plugin.getServer().getPlayer(uuid2);
															if(cPlayer != null) {
																// leave party message
																String msg = Messages.message_leaveParty;
																String name = Utils.formatName(plugin, player.getUniqueId(), cPlayer.getUniqueId());
																msg = msg.replace("{PLAYER}", name);
																String reason = Messages.reasonNotOnYellowList;
																String name2 = Utils.formatName(plugin, newUUID, cPlayer.getUniqueId());
																reason = reason.replace("{Player}", name2);
																msg = msg.replace(" {PLAYERS} {REASON}", ChatColor.RESET + reason);
																cPlayer.sendMessage(Utils.colorCodes(msg));
															}
														}
													}
													
													// send Message to player who just left
													String partyMemberlist = "";
													for(UUID playerUUID : partyMembers) {
														if(!playerUUID.equals(player.getUniqueId())) {
															String name = Utils.formatName(plugin, playerUUID, player.getUniqueId());
															partyMemberlist += ", " + name;
														}
													}
													partyMemberlist = partyMemberlist.substring(2);
													
													String msg = Messages.message_youLeaveParty;
													msg = msg.replace("has", "have");
													msg = msg.replace("{PLAYERS}", partyMemberlist);
													String reason = Messages.reasonNotOnYourYellowList;
													String name = Utils.formatName(plugin, newUUID, player.getUniqueId());
													reason = reason.replace("{Player}", name);
													msg = msg.replace("{REASON}", ChatColor.RESET + reason);
													player.sendMessage(Utils.colorCodes(msg));
													
												} catch(Exception e) {
													e.printStackTrace();
												}												
											}
										}
									}
									
									return true;
								} else {
									String notification = Messages.prefix_Bad + Messages.error_yellowNoMatch;
									notification = notification.replace("{TARGET}", playerName);
									player.sendMessage(Utils.colorCodes(notification));
									return true;
								}
							} else {
								String notification = Messages.prefix_Bad + Messages.error_notValidPlayer;
								notification = notification.replace("{TARGET}", playerName);
								player.sendMessage(Utils.colorCodes(notification));
								return true;
							}
						} else {
							String notification = Messages.prefix_Bad + "You cannot remove yourself from your own yellow list.";
							player.sendMessage(Utils.colorCodes(notification));
							return true;
						}
					} else {
						sender.sendMessage(Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs));
						return false;
					}
				} else {
					sender.sendMessage(Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs));
					return false;
				}
			}
		} else {
			sender.sendMessage("Only players can use this command.");
			return true;
		}
	}
	
	private class SwitchYellowTask extends BukkitRunnable {
		
		private Player player;
		private UUID uuid;		
		
		public SwitchYellowTask(UUID uuid, Player player) {
			this.uuid = uuid;
			this.player = player;
		}
		
		public void run() {
			ACPlayer acPlayer = plugin.getACPlayer(uuid);
			int currentPartyId = acPlayer.getPartyId();
			ACParty party;
			
			// make sure player and party exist
			if (acPlayer == null || plugin.getACParty(acPlayer.getPartyId()) == null) {
				String msg = Utils.colorCodes(Messages.prefix_Bad + "You are not a member of a party.");
				player.sendMessage(msg);
				return;
			}
			
			party = plugin.getACParty(currentPartyId);
			boolean stayInParty = true;
			
			List<UUID> currentPartyMemberlist = null;
			currentPartyMemberlist = party.getMembers();
			List<UUID> yellowMemberlist = acPlayer.getYellowList();
			if (currentPartyMemberlist.size() > 1) {
				// check if all players in the party are on this player's yellow list (check 1)
				for (UUID member : currentPartyMemberlist) {
					if (!yellowMemberlist.contains(member) && !member.equals(player.getUniqueId())) {
						stayInParty = false;
						break;
					}
				}
				
				// make sure the player is on all the players yellow list's (check 2)
				for (UUID member : currentPartyMemberlist) {
					if (!member.equals(player.getUniqueId())) {
						ACPlayer cPlayer = plugin.getACPlayer(member);
						if (cPlayer != null) {
							if (cPlayer.getYellowList() != null) {
								if (!cPlayer.getYellowList().contains(player.getUniqueId())) {
									stayInParty = false;
									break;
								}
							} else {
								stayInParty = false;
								break;
							}
						}
					}
				}
			}
			
			if (!stayInParty) {
				try {
					// message everyone
					for (UUID member : currentPartyMemberlist) {
						Player cPlayer = plugin.getServer().getPlayer(member);
						
						if (cPlayer != null) {
							String msg = "";
							
							if (!member.equals(uuid)) {
								msg = Messages.message_leaveParty;
								String name = Utils.formatName(plugin, player.getUniqueId(), cPlayer.getUniqueId(), Color.YELLOW);
								msg = msg.replace("{PLAYER}", name);
								msg = msg.replace("{PLAYERS} {REASON}", Messages.reasonLeaveYellow);
							} else {
								String list = Utils.partyMembersString(plugin, currentPartyId, cPlayer.getUniqueId());								
								msg = Messages.message_youLeaveParty;
								msg = msg.replace("has", "have");
								msg = msg.replace("{PLAYERS}", list);
								msg = msg.replace("{REASON}", Messages.reasonYouYellow);
							}
							
							cPlayer.sendMessage(Utils.colorCodes(msg));
						}	
					}
					
					party.removeMember(uuid); // remove player from old party
					
					// create a new party for the player
					int newPartyId = plugin.createNewParty(uuid, Color.YELLOW);
					plugin.getACPlayer(uuid).setPartyId(newPartyId);
					
					Utils.updateTeam(plugin, player.getUniqueId(), Color.YELLOW); // update the player teams
					
					// if previous party now only has one member set their color back to their default
					if (party.getMembers().size() == 1) {
						UUID lastPlayer = party.getMembers().get(0);
						if (lastPlayer != null && plugin.getACPlayer(lastPlayer) != null) {
							ACPlayer player = plugin.getACPlayer(lastPlayer);
							party.setColor(player.getDefaultColor());
							
							String msg = Messages.prefix_Good + Messages.message_setDefault; 
							if (acPlayer.getDefaultColor() == Color.GREEN) {
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
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				party.setColor(Color.YELLOW); // update party color
				
				// notify all players on team and update their color on scoreboard
				for (UUID member : party.getMembers()) {
					Player partyPlayer = plugin.getServer().getPlayer(member);
						
					if (partyPlayer != null && plugin.getACPlayer(member) != null) {
						ACPlayer currentACPlayer = plugin.getACPlayer(member);
						
						// notify player
						String msg = Messages.prefix_Good + Messages.message_setYellow;
						msg = msg.replace("{yellow_list}", Utils.playersString(plugin, currentACPlayer.getYellowList(), member));
						msg = msg.replace("{PLAYER}", Utils.formatName(plugin, uuid, partyPlayer.getUniqueId()));
						msg = Utils.colorCodes(msg);
						partyPlayer.sendMessage(msg);
						
						Utils.updateTeam(plugin, partyPlayer.getUniqueId(), Color.YELLOW); // update teams
					}	
				}
			}
		}
		
	}
}
