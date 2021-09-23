package dev.kyro.despair.enums;

import java.util.Arrays;
import java.util.List;

public enum Configurable {

	PREFIX("Bot Prefix", ConfigType.STRING, "prefix"),
	API_KEY("Api Key", ConfigType.STRING, "api"),
	GUILD_ID("Server", ConfigType.SNOWFLAKE, "server"),
	DISPLAY_CHANNEL_ID("Display Channel", ConfigType.SNOWFLAKE, "display"),
	DISPLAY_MESSAGE_ID("Display Message", ConfigType.SNOWFLAKE, "message"),
	NOTIFY_CHANNEL_ID("Notification Channel", ConfigType.SNOWFLAKE, "notif"),
	MEMBER_ROLE_ID("Member Role", ConfigType.SNOWFLAKE, "member"),
	ADMIN_ROLE_ID("Admin Role", ConfigType.SNOWFLAKE, "admin");

	public String displayName;
	public List<String> keys;
	public ConfigType configType;

	Configurable(String displayName, ConfigType configType, String... keys) {
		this.displayName = displayName;
		this.configType = configType;
		this.keys = Arrays.asList(keys);
	}

	public int getNum() {
		for(int i = 0; i < values().length; i++) {
			if(values()[i] == this) return i + 1;
		}
		return -1;
	}

	public enum ConfigType {
		STRING("Input string"),
		SNOWFLAKE("Input id");

		public String instructions;

		ConfigType(String instructions) {
			this.instructions = instructions;
		}
	}
}
