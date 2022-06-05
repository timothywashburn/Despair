package dev.kyro.despair.controllers.objects;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.HypixelPlayer;
import dev.kyro.despair.misc.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KOS {
	@Exclude
	public static KOS INSTANCE;
	public List<KOSPlayer> kosList = new ArrayList<>();
	@Exclude
	public boolean onSaveCooldown = false;
	@Exclude
	public boolean saveQueued = false;

	public KOS() {
		INSTANCE = this;
	}

	public KOS(List<KOSPlayer> kos) {
		INSTANCE = this;
		setKosList(kos);
	}

	public void setKosList(List<KOSPlayer> kosList) {
		for(KOSPlayer player : kosList) {
			addPlayer(player, false);
		}
	}

	public void addPlayer(KOSPlayer kosPlayer, boolean save) {
		kosList.add(kosPlayer);
		if(save) save();
	}

	public void removePlayer(KOSPlayer kosPlayer, boolean save) {
		kosList.remove(kosPlayer);
		if(save) save();
	}

	public boolean containsPlayer(UUID uuid) {
		for(KOSPlayer kosPlayer : kosList) if(kosPlayer.uuid.equals(uuid.toString())) return true;
		return false;
	}

	public boolean containsPlayer(String name) {
		for(KOSPlayer kosPlayer : kosList) if(kosPlayer.name.equalsIgnoreCase(name.toLowerCase())) return true;
		return false;
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
			Despair.FIRESTORE.collection(Constants.COLLECTION).document("kos").set(this);
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
		public double priority = 0;

		@Exclude
		public HypixelPlayer hypixelPlayer;

		public KOSPlayer() {}

		public void setUuid(String uuid) {
			this.uuid = uuid;
			hypixelPlayer = new HypixelPlayer(UUID.fromString(uuid));
		}

		public KOSPlayer(String name, String uuid) {
			this.name = name;
			setUuid(uuid);
		}

		public KOSPlayer(String name, String uuid, double priority) {
			this.name = name;
			setUuid(uuid);
			this.priority = priority;
		}
	}
}
