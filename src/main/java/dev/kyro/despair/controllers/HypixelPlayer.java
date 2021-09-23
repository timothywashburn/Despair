package dev.kyro.despair.controllers;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class HypixelPlayer {
	private JSONObject playerObj;

	public UUID UUID;
	public String name;

	public int kills;
	public String megastreak;
	public boolean isOnline;
	public long lastLogout;
	public long lastLogin;

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

		lastLogout = playerObj.getLong("lastLogout");
		lastLogin = playerObj.getLong("lastLogin");
		isOnline = lastLogout < lastLogin;

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

	public String getTimeOffline() {
		if(isOnline || lastLogout == 0 || lastLogin == 0) return "";
		long millisOffline = new Date().getTime() - Math.max(lastLogin, lastLogout);
		double minutesOffline = millisOffline / 1000D / 60D;

		DecimalFormat decimalFormat = new DecimalFormat("0.0");

		if(minutesOffline < 60) return "`" + decimalFormat.format(minutesOffline) + " minutes`";
		if(minutesOffline < 24 * 60) return "`" + decimalFormat.format(minutesOffline / 60D) + " hours`";
		return "`" + decimalFormat.format(minutesOffline / 60D / 24D) + " days`";
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
