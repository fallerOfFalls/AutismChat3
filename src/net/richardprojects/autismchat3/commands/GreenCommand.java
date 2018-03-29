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

import net.richardprojects.autismchat3.ACParty;
import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class GreenCommand implements CommandExecutor {

	private AutismChat3 plugin;
	
	public GreenCommand(AutismChat3 plugin) {
		this.plugin = plugin;		
	}
	
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] args) {
		if(sender instanceof Player) {
			final Player player = (Player) sender;
			if(args.length == 0) {
				// make sure player and party exist
				ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
				if (acPlayer == null || plugin.getACParty(acPlayer.getPartyId()) == null) {
					String msg = Utils.colorCodes(Messages.prefix_Bad + "You are not a member of a party.");
					player.sendMessage(msg);
					return true;
				}
				ACParty acParty = plugin.getACParty(acPlayer.getPartyId());

				acParty.setColor(Color.GREEN); // update party to green
				
				// check if they are alone
				if (acParty.getMembers().size() == 1) {
					// update the player's default color					
					acPlayer.setDefaultColor(Color.GREEN);
					
					String msg = Messages.message_defaultCommand;
					msg = msg.replace("{COLOR}", Messages.color_green + "Green&6");
					msg = Utils.colorCodes(msg);
					player.sendMessage(msg);
				} else {
					// notify all players on team and update their color on scoreboard
					for (UUID cUUID : acParty.getMembers()) {
						Player partyPlayer = plugin.getServer().getPlayer(cUUID);
						
						if (partyPlayer != null) {
							// notify player
							String msg = Messages.prefix_Good + Messages.message_setGreen;
							msg = msg.replace("{PLAYER}", Utils.formatName(plugin, player.getUniqueId(), partyPlayer.getUniqueId()));
							msg = Utils.colorCodes(msg);
							partyPlayer.sendMessage(msg);
						}
					}
				}
				
				// update scoreboard
				for (UUID uuid : acParty.getMembers()) {
					Utils.updateTeam(plugin, uuid, Color.GREEN);
				}				
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

}