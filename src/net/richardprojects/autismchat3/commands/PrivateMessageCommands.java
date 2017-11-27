package net.richardprojects.autismchat3.commands;

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

public class PrivateMessageCommands implements CommandExecutor {

	private AutismChat3 plugin;
	
	public PrivateMessageCommands(AutismChat3 plugin) {
		this.plugin = plugin;		
	}
	
	public boolean onCommand(CommandSender sender, Command command, String arg2,
			String[] args) {
		if(sender instanceof Player) {
			final Player player = (Player) sender;
			String cmd = command.getName();
			
			if(cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("w") || cmd.equalsIgnoreCase("tell")) {				
				// msg command
				if(args.length < 2) {
					String msg = Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs);
					player.sendMessage(msg);
					return false;
				} else {
					Player recipient;
					
					// check if player is online
					if ((recipient = this.plugin.getServer().getPlayerExact(args[0])) != null) {
						// check that both players exist and are in parties
						ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
						ACPlayer recipientPlayer = plugin.getACPlayer(recipient.getUniqueId());
						if (acPlayer == null || recipientPlayer == null) return true;
						ACParty playerParty = plugin.getACParty(acPlayer.getPartyId());
						ACParty recipientParty = plugin.getACParty(recipientPlayer.getPartyId());
						if (playerParty == null || recipientPlayer == null) return true;
						
						// check if the receiving player is red
						if (recipientParty.getColor() == Color.RED) {
							String msg = Messages.prefix_Bad + Messages.error_noAcceptingRed;
							String name = Utils.formatName(plugin, recipient.getUniqueId(), player.getUniqueId());
							msg = msg.replace("{RECEIVER}", name);
							player.sendMessage(Utils.colorCodes(msg));
							return true;
						}
						
						// check if the receiving player is yellow
						if (recipientParty.getColor() == Color.YELLOW) {
							if (!recipientPlayer.getYellowList().contains(player.getUniqueId())) {
								String msg = Messages.prefix_Bad + Messages.error_noAcceptingYellow;
								String name = Utils.formatName(plugin, recipient.getUniqueId(), player.getUniqueId());
								msg = msg.replace("{RECEIVER}", name);
								player.sendMessage(Utils.colorCodes(msg));
								return true;
							}
						}
						
						// check if the sending player is red
						if(playerParty.getColor() == Color.RED) {
							String msg = Messages.prefix_Bad + Messages.error_noSendingRed;
							player.sendMessage(Utils.colorCodes(msg));
							return true;
						}
						
						// check if the sending player is yellow
						if(playerParty.getColor() == Color.YELLOW) {
							if(!acPlayer.getYellowList().contains(recipient.getUniqueId())) {
								String msg = Messages.prefix_Bad + Messages.error_noSendingYellow;
								String name = Utils.formatName(plugin, recipient.getUniqueId(), player.getUniqueId());
								msg = msg.replace("{RECEIVER}", name);
								player.sendMessage(Utils.colorCodes(msg));
								return true;
							}
						}
						
						// create message
						String message = "";
						for(int i = 0; i < args.length; i++) {
							if(i != 0) message += args[i] + " ";
						}
						message = message.trim();
						
						// show message for recipient
						String msg1 = Messages.prefix_MessageReceiving + message;
						msg1 = msg1.replace("PLAYER", Utils.formatName(plugin, player.getUniqueId(), recipient.getUniqueId()));
						recipient.sendMessage(Utils.colorCodes(msg1));
							
						// show message for sender
						String msg2 = Messages.prefix_MessageSending + message;
						msg2 = msg2.replace("PLAYER", Utils.formatName(plugin, recipient.getUniqueId(), player.getUniqueId()));
						player.sendMessage(Utils.colorCodes(msg2));							
					} else {
						// return an error message
						player.sendMessage(Utils.colorCodes(Messages.prefix_Bad + " The specified player is not online."));
					}
				}
			}
		} else {
			sender.sendMessage("Only a player can execute this command.");
		}		
		return true;
	}

}