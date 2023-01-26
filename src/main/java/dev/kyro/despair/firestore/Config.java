package dev.kyro.despair.firestore;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.enums.Configurable;
import dev.kyro.despair.misc.Variables;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

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
	public String GUILD_ID = "0";
	public String KOS_DISPLAY_CHANNEL_ID = "0";
	public String KOS_DISPLAY_MESSAGE_ID = "0";
	public String TRUCE_DISPLAY_CHANNEL_ID = "0";
	public String TRUCE_DISPLAY_MESSAGE_ID = "0";
	public String TRIAL_ROLE_ID = "0";
	public String MEMBER_ROLE_ID = "0";
	public String ADMIN_ROLE_ID = "0";

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
				GUILD_ID = value;
				return;
			case KOS_DISPLAY_CHANNEL_ID:
				KOS_DISPLAY_CHANNEL_ID = value;
				return;
			case KOS_DISPLAY_MESSAGE_ID:
				KOS_DISPLAY_MESSAGE_ID = value;
				return;
			case TRUCE_DISPLAY_CHANNEL_ID:
				TRUCE_DISPLAY_CHANNEL_ID = value;
				return;
			case TRUCE_DISPLAY_MESSAGE_ID:
				TRUCE_DISPLAY_MESSAGE_ID = value;
				return;
			case TRIAL_ROLE_ID:
				TRIAL_ROLE_ID = value;
				return;
			case MEMBER_ROLE_ID:
				MEMBER_ROLE_ID = value;
				return;
			case ADMIN_ROLE_ID:
				ADMIN_ROLE_ID = value;
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
				Guild guild = DiscordManager.JDA.getGuildById(GUILD_ID);
				return guild != null ? guild.getName() : "Not Found";
			case KOS_DISPLAY_CHANNEL_ID:
				TextChannel kosDisplay = DiscordManager.JDA.getTextChannelById(KOS_DISPLAY_CHANNEL_ID);
				return kosDisplay != null ? kosDisplay.getName() : "Not Found";
			case KOS_DISPLAY_MESSAGE_ID:
				return KOS_DISPLAY_MESSAGE_ID + "";
			case TRUCE_DISPLAY_CHANNEL_ID:
				TextChannel truceDisplay = DiscordManager.JDA.getTextChannelById(TRUCE_DISPLAY_CHANNEL_ID);
				return truceDisplay != null ? truceDisplay.getName() : "Not Found";
			case TRUCE_DISPLAY_MESSAGE_ID:
				return TRUCE_DISPLAY_MESSAGE_ID + "";
			case TRIAL_ROLE_ID:
				Role trialRole = DiscordManager.JDA.getRoleById(TRIAL_ROLE_ID);
				return trialRole != null ? trialRole.getName() : "Not Found";
			case MEMBER_ROLE_ID:
				Role memberRole = DiscordManager.JDA.getRoleById(MEMBER_ROLE_ID);
				return memberRole != null ? memberRole.getName() : "Not Found";
			case ADMIN_ROLE_ID:
				Role adminRole = DiscordManager.JDA.getRoleById(ADMIN_ROLE_ID);
				return adminRole != null ? adminRole.getName() : "Not Found";
		}
		return null;
	}

	@Exclude
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
