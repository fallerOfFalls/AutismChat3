package net.richardprojects.autismchat3.commands;

import java.util.ArrayList;
import java.util.List;
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

public class JoinCommand implements CommandExecutor {
private AutismChat3 plugin;
	
	public JoinCommand(AutismChat3 plugin) {
		this.plugin = plugin;		
	}
	
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			final String[] args) {
		
		// make sure sender is a player
		if(!(sender instanceof Player)) {
			String msg = Utils.colorCodes(Messages.prefix_Bad + Messages.error_mustBePlayer);
			sender.sendMessage(msg);
			return true;
		}
		
		// make sure there is only 1 argument
		if(args.length != 1) {
			String msg = Messages.prefix_Bad + Messages.error_invalidArgs;
			sender.sendMessage(Utils.colorCodes(msg));
			return false;
		}
		
		// run the join command with the player and arguments
		final Player player = (Player) sender;
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				JoinCommand.this.run(player, args);
			}
		});
		return true;
	}
	
	private void run(Player player, String[] args) {
		UUID targetUUID = plugin.getUUID(args[0]);
		ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
		ACPlayer acTarget = plugin.getACPlayer(targetUUID);
		ACParty targetParty = null;
		
		// make sure the player's name exists
		if (targetUUID == null) {
			String msg = Messages.prefix_Bad + Messages.error_notValidPlayer;
			msg = msg.replace("{TARGET}", args[0]);
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// check 6 - prevent player from partying with them self
		if(targetUUID.equals(player.getUniqueId())) {
			String msg = Messages.prefix_Bad + Messages.error_JoinParty6;
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		if(acTarget.getPartyId() != -1 && acTarget.getPartyId() != 0) {
			targetParty = plugin.getACParty(acTarget.getPartyId());
		}
		
		// check 5 - prevent a player from partying with a player they are already in a party with
		if (targetParty != null && acPlayer.getPartyId() == targetParty.getId()) {
			String pName = Utils.formatName(plugin, targetUUID, player.getUniqueId());
			String msg = Messages.prefix_Bad + Messages.error_JoinParty5;
			msg = msg.replace("{PLAYER}", pName);
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// check 1 - prevent a player from joining a party that is red
		if(acTarget.getCurrentColor(plugin) == Color.RED) {
			String msg = Messages.prefix_Bad + Messages.error_JoinParty1;
			String pName = Utils.formatName(plugin, targetUUID, player.getUniqueId());
			msg = msg.replace("{PLAYER}", pName);
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// check 2 - if party is yellow make player is on party members' yellow lists.
		if (acTarget.getCurrentColor(plugin) == Color.YELLOW) {
			List<UUID> playersNotOnYellowList = new ArrayList<>();
			
			if (targetParty == null) {
				if (!acTarget.getYellowList().contains(player.getUniqueId())) {
					playersNotOnYellowList.add(targetUUID);
				}
			} else {
				for (UUID cUUID : targetParty.getMembers()) {
					ACPlayer acUUID = plugin.getACPlayer(cUUID);
					if (acUUID != null) {
						if (!acUUID.getYellowList().contains(player.getUniqueId())) {
							playersNotOnYellowList.add(cUUID);
						}
					}
				}
			}
			
			if (playersNotOnYellowList.size() > 0) {
				String partyMemberString = "";
				if (targetParty == null) {
					partyMemberString = Utils.formatName(plugin, targetUUID);
				} else {
					partyMemberString = Utils.partyMembersString(plugin, targetParty.getId(), player.getUniqueId());
				}
				
				String msg = Messages.prefix_Bad + Messages.error_JoinParty2;
				msg = msg.replace("{MEMBERS}", partyMemberString);
				msg = msg.replace("{NOT_ON_LIST}", Utils.playersString(plugin, playersNotOnYellowList, player.getUniqueId()));
				player.sendMessage(Utils.colorCodes(msg));
				return;
			}			
		}
		
		// check 4 - if party is yellow make players are on the player who is joining's yellow list.
		if(acTarget.getCurrentColor(plugin) == Color.YELLOW) {
			List<UUID> playersNotOnYellowList = new ArrayList<>();
			
			// find any players in party who are not on player's yellow list
			if (targetParty == null) {
				if (!acPlayer.getYellowList().contains(targetUUID)) {
					playersNotOnYellowList.add(targetUUID);
				}
			} else {
				for (UUID cUUID : targetParty.getMembers()) {
					if (!acPlayer.getYellowList().contains(cUUID)) {
						playersNotOnYellowList.add(cUUID);
					}
				}
			}	
			
			if (playersNotOnYellowList.size() > 0) {
				String partyMemberString = "";
				if (targetParty == null) {
					partyMemberString = Utils.formatName(plugin, targetUUID);
				} else {
					partyMemberString = Utils.partyMembersString(plugin, targetParty.getId(), player.getUniqueId());
				}
				
				String msg = Messages.prefix_Bad + Messages.error_JoinParty4;
				msg = msg.replace("{MEMBERS}", partyMemberString);
				msg = msg.replace("{NOT_ON_LIST}", Utils.playersString(plugin, playersNotOnYellowList, player.getUniqueId()));
				player.sendMessage(Utils.colorCodes(msg));
				return;
			}
		}
		
		// join new party
		if (targetParty == null) {
			plugin.joinPlayer(targetUUID, player.getUniqueId());
		} else {
			plugin.joinParty(targetParty.getId(), player.getUniqueId());
		}
	}
}
