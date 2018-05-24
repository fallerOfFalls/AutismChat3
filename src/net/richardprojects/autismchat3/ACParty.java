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
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * A simple class that represents an AutismChat3 party
 * 
 * @author RichardB122
 * @version 4/15/17
 */
public class ACParty {

	private int id;
	private ArrayList<UUID> members;
	private Color color;
	
	public boolean needsUpdate;
	
	/**
	 * A constructor used when party data is simply being loaded from an 
	 * existing file.
	 * 
	 * @param members list of members of the party
	 * @param id the party's id
	 */
	public ACParty(List<UUID> members, int id, Color color) {
		this.members = new ArrayList<>(members);
		this.id = id;
		this.color = color;
		this.needsUpdate = true;
	}
	
	public int getId() {
		return id;
	}
	
	public ArrayList<UUID> getMembers() {
		return members;
	}
	
	public void addMember(UUID newUUID) {
		this.members.add(newUUID);
		this.needsUpdate = true;
	}
	
	public void removeMember(UUID uuid) {
		if (members.contains(uuid)) {
			members.remove(uuid);
		}		
		this.needsUpdate = true;
	}
	
	public void setColor(Color color) {
		this.color = color;
		this.needsUpdate = true;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	/**
	 * Attempts to save a copy of the ACParty to disk. Returns true if 
	 * successful and false if the operation failed.
	 * 
	 * @param plugin a reference to the AutismChat3 plugin
	 * @return whether the operation was successful or not
	 */
	public boolean save(AutismChat3 plugin) {
		// automatically delete party if it is empty
		if (members.size() == 0) {
			plugin.deleteParty(id);
			return true;
		}
		
		try {
			// load or create new file
			FileConfiguration partyFile = new YamlConfiguration();
			File file = new File(plugin.getDataFolder().toString() + File.separator + "parties" + File.separator + id + ".yml");
			if (!file.exists()) {
				boolean result = file.createNewFile();
				if (!result) return false;
			}
			partyFile.load(file);
			
			// save data
			partyFile.set("members", Utils.convertListToString(members));
			partyFile.set("color", Color.toString(color));
			partyFile.save(file);
			
			this.needsUpdate = false;			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
}
