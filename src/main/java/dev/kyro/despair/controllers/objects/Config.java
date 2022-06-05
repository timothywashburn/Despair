package dev.kyro.despair.controllers.objects;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.enums.Configurable;
import dev.kyro.despair.misc.Constants;

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
	public long KOS_CATEGORY_ID;
	public long ADMIN_ROLE_ID;

	public Config() {
		INSTANCE = this;
	}

	public Config(String PREFIX, String API_KEY, int MAX_PLAYERS, long GUILD_ID, long KOS_CATEGORY_ID, long ADMIN_ROLE_ID) {
		this.PREFIX = PREFIX;
		this.API_KEY = API_KEY;
		this.MAX_PLAYERS = MAX_PLAYERS;
		this.GUILD_ID = GUILD_ID;
		this.KOS_CATEGORY_ID = KOS_CATEGORY_ID;
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
			case KOS_CATEGORY_ID:
				KOS_CATEGORY_ID = Long.parseLong(value);
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
			case KOS_CATEGORY_ID:
				return KOS_CATEGORY_ID + "";
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
			Despair.FIRESTORE.collection(Constants.COLLECTION).document("config").set(this);
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
