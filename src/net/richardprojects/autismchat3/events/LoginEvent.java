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

import java.util.List;
import java.util.UUID;

import net.richardprojects.autismchat3.ACParty;
import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Config;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginEvent implements Listener {

	private AutismChat3 plugin;

	public LoginEvent(AutismChat3 plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void loginEvent(final PlayerJoinEvent e) {
		e.setJoinMessage("");
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		
		// update the UUID's file every time a player joins
		plugin.updateUUID(player.getName(), player.getUniqueId());;

		// handle new user creation
		if (plugin.getACPlayer(player.getUniqueId()) == null) {
			plugin.createNewPlayer(uuid, -1);
		}

		ACPlayer acPlayer = plugin.getACPlayer(uuid);
		
		// add new player to team based on their color
		switch (acPlayer.getCurrentColor(plugin)) {
			case RED:
				AutismChat3.redTeam.addPlayer(player);
				break;
			case BLUE:
				AutismChat3.blueTeam.addPlayer(player);
				break;
			case GREEN:
				AutismChat3.greenTeam.addPlayer(player);
				break;
			case YELLOW:
				AutismChat3.yellowTeam.addPlayer(player);
				break;
			case WHITE:
				break;
			default:
				break;
		}
		
		player.setScoreboard(AutismChat3.board);

		// show message of the day
		if (acPlayer.getDispalyMotd()) {
			for (String msg : Messages.motd) {
				msg = Utils.colorCodes(msg);
				player.sendMessage(msg);
			}
		}

		// show the login report
		if (Config.loginReport) {
			String msg = Utils.colorCodes(Messages.message_loadingSettings);
			player.sendMessage(msg);

			// send statuses for login report
			String[] loginReport = Config.loginReportStatuses.split(",");
			for (int i = 0; i < loginReport.length; i++) {
				Utils.sendStatus(loginReport[i], player.getUniqueId(), plugin);
			}
		}
		
		// only show the login message to everyone if they are not red
		if (!(Config.redHidesLoginNotification && acPlayer.getCurrentColor(plugin) == Color.RED)) {
			for(Player cPlayer : plugin.getServer().getOnlinePlayers()) {
				if (cPlayer.getUniqueId().equals(e.getPlayer().getUniqueId())) {
					String msg = Messages.message_joinMessage;
					String name = Utils.formatName(plugin, player.getUniqueId(), null);
					msg = msg.replace("{PLAYER}", name);
					cPlayer.sendMessage(Utils.colorCodes(msg));
				} else {
					int cPlayerPartyId = plugin.getACPlayer(cPlayer.getUniqueId()).getPartyId();
					String name = Utils.formatName(plugin, player.getUniqueId(), cPlayer.getUniqueId());
					ACParty cPlayerParty = plugin.getACParty(cPlayerPartyId);
						
					if (cPlayerParty != null) {
						List<UUID> partyMembers = cPlayerParty.getMembers();
						if (partyMembers.contains(e.getPlayer().getUniqueId())) {
							String msg = Messages.message_joinMessageParty;
							msg = msg.replace("{PLAYER}", name);
							cPlayer.sendMessage(Utils.colorCodes(msg));
						} else {
							String msg = Messages.message_joinMessage;
						msg = msg.replace("{PLAYER}", name);
							cPlayer.sendMessage(Utils.colorCodes(msg));
						}
					} else {
						String msg = Messages.message_joinMessage;
						msg = msg.replace("{PLAYER}", name);
						cPlayer.sendMessage(Utils.colorCodes(msg));
					}
				}
			}
		}
	}
}
