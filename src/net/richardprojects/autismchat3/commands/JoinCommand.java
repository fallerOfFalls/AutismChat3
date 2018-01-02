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
		if(sender instanceof Player) {
			final Player player = (Player) sender;
			if(args.length == 1) {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					public void run() {
						JoinCommand.this.run(player, args);
					}
				});
				return true;
			} else {
				String msg = Messages.prefix_Bad + Messages.error_invalidArgs;
				player.sendMessage(Utils.colorCodes(msg));
				return false;
			}
		} else {
			
			return true;
		}
	}
	
	private void run(Player player, String[] args) {
		UUID newUUID = plugin.getUUID(args[0]);
		ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
		ACPlayer acTarget = plugin.getACPlayer(newUUID);
		int partyId = acTarget.getPartyId();
		ACParty requestedParty;
		
		// make sure the player's name exists
		if (newUUID == null) {
			String msg = Messages.prefix_Bad + Messages.error_notValidPlayer;
			msg = msg.replace("{TARGET}", args[0]);
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// make sure they are in a party
		if(partyId == 0 || plugin.getACParty(partyId) == null) {
			String msg = Messages.prefix_Bad + "&7That player doesn't appear to be in a party.";
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		requestedParty = plugin.getACParty(partyId);
		List<UUID> partyMembers = plugin.getACParty(partyId).getMembers();
		
		// check 6 - prevent player from partying with them self
		if(newUUID.equals(player.getUniqueId())) {
			String msg = Messages.prefix_Bad + Messages.error_JoinParty6;
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// check 5 - prevent a player from partying with a player they are already in a party with
		if (acPlayer.getPartyId() == requestedParty.getId()) {
			String pName = Utils.formatName(plugin, newUUID, player.getUniqueId());
			String msg = Messages.prefix_Bad + Messages.error_JoinParty5;
			msg = msg.replace("{PLAYER}", pName);
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// check 2 - if party is yellow make player is on party members' yellow lists.
		if (requestedParty.getColor() == Color.YELLOW) {
			String partyMemberString = Utils.partyMembersString(plugin, partyId, player.getUniqueId());
			List<UUID> playersNotOnYellowList = new ArrayList<>();
			
			for (UUID cUUID : requestedParty.getMembers()) {
				ACPlayer acUUID = plugin.getACPlayer(cUUID);
				if (acUUID != null) {
					if (!acUUID.getYellowList().contains(player.getUniqueId())) {
						playersNotOnYellowList.add(cUUID);
					}
				}
			}
			
			if (playersNotOnYellowList.size() > 0) {
				String msg = Messages.prefix_Bad + Messages.error_JoinParty2;
				msg = msg.replace("{MEMBERS}", partyMemberString);
				msg = msg.replace("{NOT_ON_LIST}", Utils.playersString(plugin, playersNotOnYellowList, player.getUniqueId()));
				player.sendMessage(Utils.colorCodes(msg));
				return;
			}			
		}
				
		// check 3 - removed since players no longer have colors
		/*if(requestedParty.getColor() == Color.RED) {
			String msg = Messages.prefix_Bad + Messages.error_JoinParty3;
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}*/
		
		// check 4 - if party is yellow make players are on the player who is joining's yellow list.
		if(requestedParty.getColor() == Color.YELLOW) {
			String partyMemberString = Utils.partyMembersString(plugin, partyId, player.getUniqueId());
			List<UUID> playersNotOnYellowList = new ArrayList<>();
			
			// find any players in party who are not on player's yellow list
			for (UUID cUUID : requestedParty.getMembers()) {
				if (!acPlayer.getYellowList().contains(cUUID)) {
					playersNotOnYellowList.add(cUUID);
				}
			}
			
			if (playersNotOnYellowList.size() > 0) {
				String msg = Messages.prefix_Bad + Messages.error_JoinParty4;
				msg = msg.replace("{MEMBERS}", partyMemberString);
				msg = msg.replace("{NOT_ON_LIST}", Utils.playersString(plugin, playersNotOnYellowList, player.getUniqueId()));
				player.sendMessage(Utils.colorCodes(msg));
				return;
			}
		}
		
		plugin.joinParty(partyId, player.getUniqueId()); // join new party
	}
}
