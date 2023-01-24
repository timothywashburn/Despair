package dev.kyro.despair.controllers;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.enums.Configurable;
import dev.kyro.despair.misc.Variables;

public class Config {
	@Exclude
	public static Config INSTANCE;
	@Exclude
	public boolean onSaveCooldown = false;
	@Exclude
	public boolean saveQueued = false;

	public String PREFIX = ".";
	public String API_KEY = "";
	public int MAX_PLAYERS = 20;
	public long GUILD_ID;
	public long DISPLAY_CHANNEL_ID;
	public long DISPLAY_MESSAGE_ID;
	public long MEMBER_ROLE_ID;
	public long ADMIN_ROLE_ID;

	public Config() {
		INSTANCE = this;
	}

	public Config(String PREFIX, String API_KEY, int MAX_PLAYERS, long GUILD_ID, long DISPLAY_CHANNEL_ID,
				  long DISPLAY_MESSAGE_ID, long MEMBER_ROLE_ID, long ADMIN_ROLE_ID) {
		this.PREFIX = PREFIX;
		this.API_KEY = API_KEY;
		this.GUILD_ID = GUILD_ID;
		this.MAX_PLAYERS = MAX_PLAYERS;
		this.DISPLAY_CHANNEL_ID = DISPLAY_CHANNEL_ID;
		this.DISPLAY_MESSAGE_ID = DISPLAY_MESSAGE_ID;
		this.MEMBER_ROLE_ID = MEMBER_ROLE_ID;
		this.ADMIN_ROLE_ID = ADMIN_ROLE_ID;
	}

	@Exclude
	public void set(Configurable configurable, String value) {

		switch(configurable) {
			case PREFIX:
				PREFIX = value;
				return;
			case API_KEY:
				API_KEY = value;
				return;
			case MAX_PLAYERS:
				MAX_PLAYERS = Integer.parseInt(value);
				return;
			case GUILD_ID:
				GUILD_ID = Long.parseLong(value);
				return;
			case DISPLAY_CHANNEL_ID:
				DISPLAY_CHANNEL_ID = Long.parseLong(value);
				return;
			case DISPLAY_MESSAGE_ID:
				DISPLAY_MESSAGE_ID = Long.parseLong(value);
				return;
			case MEMBER_ROLE_ID:
				MEMBER_ROLE_ID = Long.parseLong(value);
				return;
			case ADMIN_ROLE_ID:
				ADMIN_ROLE_ID = Long.parseLong(value);
		}
	}

	@Exclude
	public String get(Configurable configurable) {

		switch(configurable) {
			case PREFIX:
				return PREFIX;
			case API_KEY:
				return API_KEY;
			case MAX_PLAYERS:
				return MAX_PLAYERS + "";
			case GUILD_ID:
				return GUILD_ID + "";
			case DISPLAY_CHANNEL_ID:
				return DISPLAY_CHANNEL_ID + "";
			case DISPLAY_MESSAGE_ID:
				return DISPLAY_MESSAGE_ID + "";
			case MEMBER_ROLE_ID:
				return MEMBER_ROLE_ID + "";
			case ADMIN_ROLE_ID:
				return ADMIN_ROLE_ID + "";
		}
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
			Despair.FIRESTORE.collection(Variables.COLLECTION).document("config").set(this);
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
}
