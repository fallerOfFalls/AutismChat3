/*   This file is part of AutismChat3.
*
*    AutismChat3 is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License.
*
*    You can view a copy of the GNU General Public License below
*    http://www.gnu.org/licenses/
*/

package net.richardprojects.autismchat3;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * A simple class that represents an AutismChat3 player.
 * 
 * @author RichardB122
 * @version 11/22/17
 */
public class ACPlayer {

	private UUID uuid;
	private int partyId;
	private boolean globalChat;
	private ArrayList<UUID> yellowList;
	private boolean displayMotd;
	public boolean needsUpdate;
	private Color defaultColor;
	
	/**
	 * Constructor for an ACPlayer that creates a completely new player. Use
	 * this constructor if you are creating a new player and not loading an
	 * existing one.
	 * 
	 * @param uuid the UUID of the player
	 * @param partyId the party they are in
	 */
	public ACPlayer(UUID uuid, int partyId) {
		this.uuid = uuid;
		this.partyId = partyId;		
		this.defaultColor = Config.templateDefaultColor;
		this.globalChat = Config.templateGlobalChat;
		this.displayMotd = Config.templateMotd;
		this.yellowList = new ArrayList<UUID>();
		
		this.needsUpdate = true;
	}
	
	/**
	 * Constructor for an ACPlayer that is being loaded. In this constructor 
	 * you must provide all variables.
	 */
	public ACPlayer(UUID uuid, int partyId, Color defaultColor, boolean globalChat, ArrayList<UUID> yellowList, boolean displayMotd) {
		this.uuid = uuid;
		this.partyId = partyId;
		this.globalChat = globalChat;
		this.yellowList = yellowList;
		this.displayMotd = displayMotd;
		this.defaultColor = defaultColor;
		this.needsUpdate = false;
	}
	
	public int getPartyId() {
		return partyId;
	}

	public void setPartyId(int partyId) {
		this.partyId = partyId;
		this.needsUpdate = true;
	}

	public Color getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(Color color) {
		this.defaultColor = color;
		this.needsUpdate = true;
	}

	public boolean isGlobalChatEnabled() {
		return globalChat;
	}

	public void setGlobalChat(boolean globalChat) {
		this.globalChat = globalChat;
		this.needsUpdate = true;
	}

	public boolean getDispalyMotd() {
		return displayMotd;
	}
	
	public void setDisplayMotd(boolean motd) {
		this.displayMotd = motd;
		this.needsUpdate = true;
	}
	
	public ArrayList<UUID> getYellowList() {
		return yellowList;
	}

	public void setYellowList(ArrayList<UUID> yellowList) {
		this.yellowList = yellowList;
		this.needsUpdate = true;
	}
	
	public void addPlayerToYellowList(UUID newUUID) {
		this.yellowList.add(newUUID);
		this.needsUpdate = true;
	}
	
	public void removePlayerFromYellowList(UUID uuid) {
		if (this.yellowList.contains(uuid)) {
			this.yellowList.remove(uuid);
		}		
		this.needsUpdate = true;
	}
	
	/**
	 * Attempts to save a copy of the ACPlayer to disk. Returns true if 
	 * successful and false if the operation failed.
	 * 
	 * @param plugin a reference to the AutismChat3 plugin
	 * @return whether the operation was successful or not
	 */
	public boolean save(AutismChat3 plugin) {
		try {
			// load or create new file
			FileConfiguration playerFile = new YamlConfiguration();
			File file = new File(plugin.getDataFolder().toString() + File.separator + "userdata" + File.separator + uuid.toString() + ".yml");
			if (!file.exists()) {
				boolean result = file.createNewFile();
				if (!result) return false;
			}
			playerFile.load(file);
			
			// save data
			playerFile.set("partyId", partyId);
			playerFile.set("defaultColor", Color.toString(defaultColor));
			playerFile.set("globalChat", globalChat);
			playerFile.set("yellowList", Utils.convertListToString(yellowList));
			playerFile.set("displayMotd", displayMotd);
			playerFile.save(file);
			
			this.needsUpdate = false;			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
