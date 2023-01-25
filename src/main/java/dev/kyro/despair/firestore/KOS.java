package dev.kyro.despair.firestore;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.HypixelPlayer;
import dev.kyro.despair.misc.Variables;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KOS {
	@Exclude
	public static KOS INSTANCE;
	@Exclude
	public boolean onSaveCooldown = false;
	@Exclude
	public boolean saveQueued = false;

//	saveable
	public List<KOSPlayer> kosList = new ArrayList<>();
	public List<TrucePlayer> truceList = new ArrayList<>();

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

	public void addKOSPlayer(KOSPlayer kosPlayer, boolean save) {
		kosList.add(kosPlayer);
		if(save) save();
	}

	public void removeKOSPlayer(KOSPlayer kosPlayer, boolean save) {
		kosList.remove(kosPlayer);
		if(save) save();
	}

	public boolean kosContainsPlayer(UUID uuid) {
		for(KOSPlayer kosPlayer : kosList) if(kosPlayer.uuid.equals(uuid.toString())) return true;
		return false;
	}

	public void addTrucePlayer(TrucePlayer trucePlayer, boolean save) {
		truceList.add(trucePlayer);
		if(save) save();
	}

	public void removeTrucePlayer(TrucePlayer trucePlayer, boolean save) {
		truceList.remove(trucePlayer);
		if(save) save();
	}

	public boolean truceContainsPlayer(UUID uuid) {
		for(TrucePlayer trucePlayer : truceList) if(trucePlayer.uuid.equals(uuid.toString())) return true;
		return false;
	}

	public List<TrucePlayer> getPlayersInCategory(String category) {
		List<TrucePlayer> players = new ArrayList<>();
		for(TrucePlayer trucePlayer : truceList) {
			if(category == null) {
				if(!Config.INSTANCE.getTruceListCategories().contains(trucePlayer.category)) players.add(trucePlayer);
				continue;
			}
			if(trucePlayer.category.equals(category)) players.add(trucePlayer);
		}
		return players;
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

	public static class TrucePlayer {
		public String name;
		public String uuid;
		public Instant trucedUntil = Instant.now();
		public String category;
		public List<String> altUUIDs = new ArrayList<>();

		@Exclude
		public HypixelPlayer hypixelPlayer;

		public TrucePlayer() {}

		public TrucePlayer(String name, String uuid, String category, Duration truceDuration) {
			this.name = name;
			setUuid(uuid);
			this.category = category;
			extendTruce(truceDuration);
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
			hypixelPlayer = new HypixelPlayer(UUID.fromString(uuid));
		}

		public void extendTruce(Duration duration) {
			trucedUntil = trucedUntil.plus(duration);
		}

		public String getTruceStatus() {
			if(trucedUntil.isBefore(Instant.now())) return "EXPIRED";
			Duration duration = Duration.between(trucedUntil, trucedUntil);
//			TODO
			return "";
		}
	}
}
