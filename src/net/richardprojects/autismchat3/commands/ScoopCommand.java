/*   This file is part of AutismChat4.
*
*    AutismChat4 is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License.
*
*    You can view a copy of the GNU General Public License below
*    http://www.gnu.org/licenses/
*/

package net.richardprojects.autismchat3.commands;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.richardprojects.autismchat3.ACParty;
import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

/**
 * This command is used to pull another green party into your party. This will 
 * combine the two parties into one and change their color to match the party
 * that pulled them.
 * 
 * @author RichardB122
 */
public class ScoopCommand implements CommandExecutor {
	
	private AutismChat3 plugin;
	
	public ScoopCommand(AutismChat3 plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String arg2, final String[] args) {
		if (!(sender instanceof Player)) {
			String msg = Utils.colorCodes(Messages.prefix_Bad + Messages.error_mustBePlayer);
			sender.sendMessage(msg);
			return true;
		}
		
		if (args.length != 1) {
			String msg = Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs);
			sender.sendMessage(msg);
			return false;
		}
		
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				ScoopCommand.this.run((Player) sender, args);
			}
		});
		
		return true;
	}
	
	private void run(Player player, String[] args) {
		// make sure the player's name exists
		if (plugin.getUUID(args[0]) == null) {
			String msg = Messages.prefix_Bad + Messages.error_notValidPlayer;
			msg = msg.replace("{TARGET}", args[0]);
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
		ACPlayer acTarget = plugin.getACPlayer(plugin.getUUID(args[0]));
		
		// make sure the requested player is in a party
		if (acTarget.getPartyId() == 0 || plugin.getACParty(acTarget.getPartyId()) == null) {
			String msg = Messages.prefix_Bad + "That player doesn't appear to be in a party.";
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// make sure the player is in a party
		if (acPlayer.getPartyId() == 0 || plugin.getACParty(acPlayer.getPartyId()) == null) {
			String msg = Messages.prefix_Bad + "You don't appear to be in a party.";
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		ACParty playerParty = plugin.getACParty(acPlayer.getPartyId());
		ACParty targetParty = plugin.getACParty(acTarget.getPartyId());
		
		// check 1 - make sure the requested player isn't already in the player's party
		if (targetParty.getMembers().equals(playerParty.getMembers())) {
			String msg = Messages.prefix_Bad + Messages.error_ScoopParty1;
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// check 2 - make sure the target party is green
		if (targetParty.getColor() != Color.GREEN) {
			String msg = Messages.prefix_Bad + Messages.error_ScoopParty2;
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		
		// check 3 - make sure player party isn't red
		if (playerParty.getColor() == Color.RED) {
			String msg = Messages.prefix_Bad + Messages.error_ScoopParty3;
			player.sendMessage(Utils.colorCodes(msg));
			return;
		}
		

		// yellow related checks
		if (playerParty.getColor() == Color.YELLOW) {
			// check 4 - if current party is yellow make sure all players in the targetParty are on
			// yellow list of playerParty
			for (UUID cUUID : playerParty.getMembers()) {
				ACPlayer cPlayer = plugin.getACPlayer(cUUID);
				if (cPlayer != null) {
					if (!cPlayer.getYellowList().containsAll(targetParty.getMembers())) {
						String msg = Messages.prefix_Bad + Messages.error_ScoopParty4;
						player.sendMessage(Utils.colorCodes(msg));
						return;
					}
				}
			}
			
			// check 5 - if current party yellow make sure everyone in the playerParty is on the yellow
			// list of everyone in the targetParty.
			for (UUID cUUID : targetParty.getMembers()) {
				ACPlayer cPlayer = plugin.getACPlayer(cUUID);
				if (cPlayer != null) {
					if (!cPlayer.getYellowList().containsAll(playerParty.getMembers())) {
						String msg = Messages.prefix_Bad + Messages.error_ScoopParty5;
						player.sendMessage(Utils.colorCodes(msg));
						return;
					}
				}
			}
		}
		
		plugin.scoopParty(targetParty.getId(), playerParty.getId(), player.getUniqueId());
	}
}
