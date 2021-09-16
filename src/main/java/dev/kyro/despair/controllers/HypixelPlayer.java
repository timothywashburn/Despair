package dev.kyro.despair.controllers;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class HypixelPlayer {
	private JSONObject playerObj;

	public UUID UUID;
	public String name;

	public int kills;
	public String megastreak;
	public boolean isOnline;

	public List<Integer> recentKills = new ArrayList<>();

	public HypixelPlayer(UUID UUID) {

		this.UUID = UUID;
	}

	public HypixelPlayer(JSONObject playerObj) {

		update(playerObj);
	}

	public void update(JSONObject playerObj) {

		this.playerObj = playerObj.getJSONObject("player");
		getStats();
	}

	public void getStats() {

		JSONObject achievements = playerObj.getJSONObject("achievements");
		JSONObject pitData = playerObj.getJSONObject("stats").getJSONObject("Pit").getJSONObject("profile");

		UUID = getUUID(playerObj.getString("uuid"));
		name = playerObj.getString("displayname");

		kills = achievements.getInt("pit_kills");
		try {
			megastreak = pitData.getString("selected_killstreak_0");
		} catch(Exception ignored) {
			megastreak = "none";
		}

		isOnline = playerObj.getLong("lastLogout") < playerObj.getLong("lastLogin");

		recentKills.add(kills);
		if(recentKills.size() > 7) recentKills.remove(0);
	}

	public JSONObject getPlayerObj() {
		return playerObj;
	}

	public int getRecentKills() {

		if(recentKills.isEmpty()) return 0;
		return recentKills.get(recentKills.size() - 1) - recentKills.get(0);
	}

	private UUID getUUID(String unformattedUUID) {

		return java.util.UUID.fromString(
				unformattedUUID
						.replaceFirst(
								"(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
						)
		);
	}
}
