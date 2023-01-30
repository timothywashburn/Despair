package dev.kyro.despair.controllers;

import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.*;

public class HypixelPlayer {
	private JSONObject playerObj;

	public UUID UUID;
	public String name;

	public int kills;
	public String megastreak;
	public boolean isOnline;
	public boolean apiDisabled;
	public long lastLogout;
	public long lastLogin;

	public List<Integer> recentKills = new ArrayList<>();
	public List<Integer> apiDisabledKillTracker = new ArrayList<>();

	public Map<PureType, Integer> pureMap = new LinkedHashMap<>();

	public HypixelPlayer(UUID UUID) {
		this.UUID = UUID;
		init();
	}

	public HypixelPlayer(JSONObject playerObj) {
		update(playerObj);
		init();
	}

	public void init() {
		for(PureType pureType : PureType.values()) pureMap.put(pureType, 0);
	}

	public void update(JSONObject playerObj) {
		this.playerObj = playerObj.getJSONObject("player");
		getStats();
	}

	public void loadPureMap() {
		JSONObject pitData = playerObj.getJSONObject("stats").getJSONObject("Pit").getJSONObject("profile");
		loadDataSection(pitData, "inv_contents");
		loadDataSection(pitData, "inv_enderchest");
		loadDataSection(pitData, "inv_armor");
		loadDataSection(pitData, "item_stash");
	}

	public void getStats() {
		JSONObject achievements = playerObj.getJSONObject("achievements");
		JSONObject pitData = playerObj.getJSONObject("stats").getJSONObject("Pit").getJSONObject("profile");

		UUID = getUUID(playerObj.getString("uuid"));
		name = playerObj.getString("displayname");

		kills = achievements.getInt("pit_kills");
		try {
			megastreak = pitData.getString("selected_killstreak_0");
			if(megastreak.equals("overdrive")) megastreak = "Overdrive";
			if(megastreak.equals("beastmode")) megastreak = "Beastmode";
			if(megastreak.equals("hermit")) megastreak = "Hermit";
			if(megastreak.equals("highlander")) megastreak = "Highlander";
			if(megastreak.equals("grand_finale")) megastreak = "Magnum Opus";
			if(megastreak.equals("to_the_moon")) megastreak = "To The Moon";
			if(megastreak.equals("uberstreak")) megastreak = "Uberstreak";
		} catch(Exception ignored) {
			megastreak = "No Megastreak";
		}

		lastLogout = playerObj.has("lastLogout") ? playerObj.getLong("lastLogout") : -100;
		lastLogin = playerObj.has("lastLogin") ? playerObj.getLong("lastLogin") : -100;
		isOnline = lastLogout < lastLogin;
		apiDisabled = lastLogout == -100 || lastLogin == -100;

		recentKills.add(kills);
		if(recentKills.size() > Math.round(120.0 / PlayerTracker.getMaxPlayers() + 1)) recentKills.remove(0);
		apiDisabledKillTracker.add(kills);
		if(apiDisabledKillTracker.size() > Math.round(600.0 / PlayerTracker.getMaxPlayers() + 1))
			apiDisabledKillTracker.remove(0);
	}

	public void loadDataSection(JSONObject pitData, String section) {
		if(!pitData.has(section)) return;
		try {
			JSONArray encodedInv = pitData.getJSONObject(section).getJSONArray("data");
			String[] stringArrInInv = encodedInv.toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
			byte[] byteArrInv = new byte[stringArrInInv.length];
			for(int j = 0; j < stringArrInInv.length; j++) {
				String string = stringArrInInv[j];
				byteArrInv[j] = Byte.parseByte(string);
			}
			NBTList nbtListInv = NBTReader.read(new ByteArrayInputStream(byteArrInv)).getList("i");
			final int[] j = {-1};
			nbtListInv.forEachCompound(compound -> {
				j[0]++;
				if(compound.isEmpty()) return;

				try {
					int id = compound.getInt("id", -1);
					int count = compound.getInt("Count", -1);
					String name = compound.getCompound("tag").getCompound("display").getString("Name", "");
					PureType pureType = getPureType(id, name);
					if(pureType == null) return;
					pureMap.putIfAbsent(pureType, 0);
					pureMap.put(pureType, pureMap.get(pureType) + count);
				} catch(Exception ignored) {}
			});
		} catch(Exception ignored) {}
	}

	public JSONObject getPlayerObj() {
		return playerObj;
	}

	public int getRecentKills() {

		if(recentKills.isEmpty()) return 0;
		return recentKills.get(recentKills.size() - 1) - recentKills.get(0);
	}

	public boolean isOnlineWithApiDisabled() {

		return apiDisabled && (apiDisabledKillTracker.get(apiDisabledKillTracker.size() - 1) - apiDisabledKillTracker.get(0) != 0);
	}

	public String getTimeOffline() {
		if(apiDisabled) return "API Disabled";

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

	public enum PureType {
		PHILO("Philosopher's Cactus"),
		FEATHER("Funky Feather"),
		VILE("Chunk of Vile"),
		PB("Pants Bundle"),
		GEM("Totally Legit Gem");

		public String displayName;

		PureType(String displayName) {
			this.displayName = displayName;
		}
	}

	public static PureType getPureType(int id, String name) {
		name = name.toLowerCase();
		if(id == 81 && name.contains("philosopher")) return PureType.PHILO;
		if(id == 288 && name.contains("funky")) return PureType.FEATHER;
		if(id == 263 && name.contains("vile")) return PureType.VILE;
		if(id == 342 && name.contains("pants bundle")) return PureType.PB;
		if(id == 388 && name.contains("gem")) return PureType.GEM;
		return null;
	}
}
