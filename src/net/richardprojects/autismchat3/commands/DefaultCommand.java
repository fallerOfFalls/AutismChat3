package net.richardprojects.autismchat3.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.richardprojects.autismchat3.ACPlayer;
import net.richardprojects.autismchat3.AutismChat3;
import net.richardprojects.autismchat3.Color;
import net.richardprojects.autismchat3.Messages;
import net.richardprojects.autismchat3.Utils;

public class DefaultCommand implements CommandExecutor {

	private AutismChat3 plugin;
	
	public DefaultCommand(AutismChat3 plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			ACPlayer acPlayer = plugin.getACPlayer(player.getUniqueId());
			
			if (acPlayer != null) {
				if (args.length == 1) {
					String msg = Messages.message_defaultCommand;
					
					if (args[0].equalsIgnoreCase("white")) {
						acPlayer.setDefaultColor(Color.WHITE);
						msg = msg.replace("{COLOR}", "&fWhite&6");
					} else if (args[0].equalsIgnoreCase("green")) {
						acPlayer.setDefaultColor(Color.GREEN);
						msg = msg.replace("{COLOR}", Messages.color_green + "Green&6");
					} else if (args[0].equalsIgnoreCase("red")) {
						acPlayer.setDefaultColor(Color.RED);
						msg = msg.replace("{COLOR}", Messages.color_red + "Red&6");
					} else if (args[0].equalsIgnoreCase("yellow")) {
						acPlayer.setDefaultColor(Color.YELLOW);
						msg = msg.replace("{COLOR}", Messages.color_yellow + "Yellow&6");
					}
					
					msg = Utils.colorCodes(msg);
					player.sendMessage(msg);
					
					return true;
				} else {
					String msg = Utils.colorCodes(Messages.prefix_Bad + Messages.error_invalidArgs);
					sender.sendMessage(msg);
					return false;
				}
			} else return true;

		} else {
			sender.sendMessage("Only players can use this command.");
			return true;
		}		
	}
}
