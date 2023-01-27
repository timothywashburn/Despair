package dev.kyro.despair.firestore;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.HypixelPlayer;
import dev.kyro.despair.misc.Misc;
import dev.kyro.despair.misc.Variables;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class KOS {
	@Exclude
	public static KOS INSTANCE;
	@Exclude
	public boolean onSaveCooldown = false;
	@Exclude
	public boolean saveQueued = false;

//	saveable
	public List<KOSPlayer> kosList = new ArrayList<>();
	private List<TrucePlayer> truceList = new ArrayList<>();

	public KOS() {
		INSTANCE = this;
	}

	public KOS(List<KOSPlayer> kos) {
		INSTANCE = this;
		setKosList(kos);
	}

	public void setKosList(List<KOSPlayer> kosList) {
		for(KOSPlayer player : kosList) {
			addKOSPlayer(player, false);
		}
	}

	@Exclude
	public void addKOSPlayer(KOSPlayer kosPlayer, boolean save) {
		kosList.add(kosPlayer);
		if(save) save();
	}

	@Exclude
	public void removeKOSPlayer(KOSPlayer kosPlayer, boolean save) {
		kosList.remove(kosPlayer);
		if(save) save();
	}

	@Exclude
	public boolean kosContainsPlayer(UUID uuid) {
		for(KOSPlayer kosPlayer : kosList) if(kosPlayer.uuid.equals(uuid.toString())) return true;
		return false;
	}

	@Exclude
	public void addTrucePlayer(TrucePlayer trucePlayer, boolean save) {
		truceList.add(trucePlayer);
		if(save) save();
	}

	@Exclude
	public void removeTrucePlayer(TrucePlayer trucePlayer, boolean save) {
		truceList.remove(trucePlayer);
		if(save) save();
	}

	@Exclude
	public boolean truceContainsPlayer(String name) {
		for(TrucePlayer trucePlayer : truceList) if(trucePlayer.name.equalsIgnoreCase(name)) return true;
		return false;
	}

	@Exclude
	public List<TrucePlayer> getTruceList() {
		Collections.sort(truceList);
		return truceList;
	}

	@Exclude
	public List<TrucePlayer> getPlayersInCategory(String category) {
		List<TrucePlayer> players = new ArrayList<>();
		for(TrucePlayer trucePlayer : getTruceList()) {
			if(category == null) {
				if(!Config.INSTANCE.getTruceListCategories().contains(trucePlayer.category)) players.add(trucePlayer);
				continue;
			}
			if(trucePlayer.category.equals(category)) players.add(trucePlayer);
		}
		return players;
	}

	@Exclude
	public TrucePlayer getTrucePlayer(Users.DiscordUser discordUser) {
		for(TrucePlayer trucePlayer : truceList) {
			if(trucePlayer.discordID == null) continue;
			long trucePlayerDiscord = Long.parseLong(trucePlayer.discordID);
			if(trucePlayerDiscord == discordUser.id) return trucePlayer;
		}
		return null;
	}

	@Exclude
	public TrucePlayer getTrucePlayer(String identifier) {
		for(TrucePlayer trucePlayer : truceList) if(trucePlayer.name.equalsIgnoreCase(identifier)) return trucePlayer;
		return null;
	}

	@Exclude
	public void save() {
		if(onSaveCooldown && !saveQueued) {
			saveQueued = true;
			new Thread(() -> {
				try {
					Thread.sleep(1500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				saveQueued = false;
				save();
			}).start();
		}
		if(!saveQueued && !onSaveCooldown) {
			Despair.FIRESTORE.collection(Variables.COLLECTION).document("kos").set(this);
			onSaveCooldown = true;
			new Thread(() -> {
				try {
					Thread.sleep(1500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				onSaveCooldown = false;
			}).start();
		}
	}

	public static class KOSPlayer {
		public String name;
		public String uuid;
		public List<String> tags = new ArrayList<>();

		@Exclude
		public HypixelPlayer hypixelPlayer;

		public KOSPlayer() {}

		public KOSPlayer(String name, String uuid, List<String> tags) {
			this.name = name;
			setUuid(uuid);
			this.tags = tags;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
			hypixelPlayer = new HypixelPlayer(UUID.fromString(uuid));
		}

		@Exclude
		public String getTagsAsString() {
			if(tags.isEmpty()) return "";
			String tagString = " [";
			for(int i = 0; i < tags.size(); i++) {
				if(i != 0) tagString += ", ";
				tagString += "`" + tags.get(i) + "`";
			}
			return tagString + "]";
		}
	}

	public static class TrucePlayer implements Comparable<TrucePlayer> {
		public String name;
		public Date trucedUntil;
		public String category;
		public String discordID;

		public TrucePlayer() {}

		public TrucePlayer(String name, String category, Duration truceDuration) {
			this.name = name;
			this.category = category;
			if(truceDuration != null) trucedUntil = new Date(new Date().getTime() + truceDuration.toMillis());
		}

		public void extendTruce(Duration duration) {
			if(trucedUntil == null) throw new RuntimeException();
			trucedUntil = new Date(trucedUntil.getTime() + duration.toMillis());
		}

		@Exclude
		public String getTruceStatus() {
			if(trucedUntil == null) return "PERMANENT";
			Duration duration = Duration.between(Instant.now(), trucedUntil.toInstant());
			if(trucedUntil.toInstant().isBefore(Instant.now())) return "EXPIRED (" + Misc.humanReadableFormat(duration) + ")";
			return Misc.humanReadableFormat(duration);
		}

		@Override
		public int compareTo(@NotNull KOS.TrucePlayer otherPlayer) {
			if(trucedUntil == null) {
				if(otherPlayer.trucedUntil == null) return 0;
				return 1;
			}
			if(otherPlayer.trucedUntil == null) return -1;
			return trucedUntil.toInstant().isBefore(otherPlayer.trucedUntil.toInstant()) ? -1 : 1;
		}
	}
}
