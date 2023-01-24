package dev.kyro.despair.firestore;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.enums.Configurable;
import dev.kyro.despair.misc.Variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	public String RAW_TRUCE_CATEGORIES = "";
	public long GUILD_ID;
	public long KOS_DISPLAY_CHANNEL_ID;
	public long KOS_DISPLAY_MESSAGE_ID;
	public long TRUCE_DISPLAY_CHANNEL_ID;
	public long TRUCE_DISPLAY_MESSAGE_ID;
	public long MEMBER_ROLE_ID;
	public long ADMIN_ROLE_ID;

	public Config() {
		INSTANCE = this;
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
			case TRUCE_CATEGORIES:
				RAW_TRUCE_CATEGORIES = value;
				return;
			case GUILD_ID:
				GUILD_ID = Long.parseLong(value);
				return;
			case KOS_DISPLAY_CHANNEL_ID:
				KOS_DISPLAY_CHANNEL_ID = Long.parseLong(value);
				return;
			case KOS_DISPLAY_MESSAGE_ID:
				KOS_DISPLAY_MESSAGE_ID = Long.parseLong(value);
				return;
			case TRUCE_DISPLAY_CHANNEL_ID:
				TRUCE_DISPLAY_CHANNEL_ID = Long.parseLong(value);
				return;
			case TRUCE_DISPLAY_MESSAGE_ID:
				TRUCE_DISPLAY_MESSAGE_ID = Long.parseLong(value);
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
			case TRUCE_CATEGORIES:
				return RAW_TRUCE_CATEGORIES;
			case GUILD_ID:
				return GUILD_ID + "";
			case KOS_DISPLAY_CHANNEL_ID:
				return KOS_DISPLAY_CHANNEL_ID + "";
			case KOS_DISPLAY_MESSAGE_ID:
				return KOS_DISPLAY_MESSAGE_ID + "";
			case TRUCE_DISPLAY_CHANNEL_ID:
				return TRUCE_DISPLAY_CHANNEL_ID + "";
			case TRUCE_DISPLAY_MESSAGE_ID:
				return TRUCE_DISPLAY_MESSAGE_ID + "";
			case MEMBER_ROLE_ID:
				return MEMBER_ROLE_ID + "";
			case ADMIN_ROLE_ID:
				return ADMIN_ROLE_ID + "";
		}
		return null;
	}

	public List<String> getTruceListCategories() {
		if(RAW_TRUCE_CATEGORIES.isEmpty()) return new ArrayList<>();
		return Arrays.asList(RAW_TRUCE_CATEGORIES.split(","));
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
