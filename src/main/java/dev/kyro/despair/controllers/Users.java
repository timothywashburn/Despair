package dev.kyro.despair.controllers;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.misc.Variables;

import java.util.ArrayList;
import java.util.List;

public class Users {
	@Exclude
	public static Users INSTANCE;
	public List<DiscordUser> discordUserList = new ArrayList<>();
	@Exclude
	public boolean onSaveCooldown = false;
	@Exclude
	public boolean saveQueued = false;

	public Users() {
		INSTANCE = this;
	}

	public Users(List<DiscordUser> discordUserList) {
		INSTANCE = this;
		this.discordUserList = discordUserList;
	}

	@Exclude
	public List<DiscordUser> getUsersWithTags(HypixelPlayer hypixelPlayer, List<String> tags) {
		List<String> newTags = new ArrayList<>(tags);
		newTags.add(hypixelPlayer.UUID.toString());
		if(hypixelPlayer.megastreak.equals("Uberstreak") && PlayerTracker.isPlayerStreaking(hypixelPlayer))
			newTags.add("uber");
		return getUsersWithTags(newTags);
	}

	@Exclude
	public List<DiscordUser> getUsersWithTags(List<String> tags) {
		List<DiscordUser> usersWithTag = new ArrayList<>();
		for(DiscordUser discordUser : discordUserList) {
			for(String tag : discordUser.tags) if(tags.contains(tag)) usersWithTag.add(discordUser);
			if(!usersWithTag.contains(discordUser) && discordUser.tags.contains("all")) usersWithTag.add(discordUser);
		}
		return usersWithTag;
	}

	@Exclude
	public DiscordUser getUser(long id) {
		for(DiscordUser discordUser : discordUserList) if(discordUser.id == id) return discordUser;
		return createUser(id);
	}

	@Exclude
	public DiscordUser createUser(long id) {
		DiscordUser discordUser = new DiscordUser(id);
		discordUserList.add(discordUser);
		save();
		return discordUser;
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
			Despair.FIRESTORE.collection(Variables.COLLECTION).document("users").set(this);
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

	public static class DiscordUser {
		public long id;
		public List<String> tags = new ArrayList<>();

		public DiscordUser() {}

		public DiscordUser(long id) {
			this.id = id;
		}

		public DiscordUser(long id, List<String> tags) {
			this.id = id;
			this.tags = tags;
		}

		public void save() {
			Users.INSTANCE.save();
		}
	}
}
