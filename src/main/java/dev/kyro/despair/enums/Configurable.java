package dev.kyro.despair.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Configurable {

	PREFIX("Bot Prefix", ConfigType.STRING, false, "prefix"),
	API_KEY("Api Key", ConfigType.STRING, false, "api"),
	MAX_PLAYERS("Max Players", ConfigType.NUMBER, false, "max"),
	GUILD_ID("Server", ConfigType.SNOWFLAKE, true, "server"),
	DISPLAY_CHANNEL_ID("Display Channel", ConfigType.SNOWFLAKE, true, "display"),
	DISPLAY_MESSAGE_ID("Display Message", ConfigType.SNOWFLAKE, true, "message"),
	NOTIFY_CHANNEL_ID("Notification Channel", ConfigType.SNOWFLAKE, true, "notif"),
	MEMBER_ROLE_ID("Member Role", ConfigType.SNOWFLAKE, false, "member"),
	ADMIN_ROLE_ID("Admin Role", ConfigType.SNOWFLAKE, false, "admin");

	public String displayName;
	public ConfigType configType;
	public boolean isHidden;
	public List<String> keys;

	Configurable(String displayName, ConfigType configType, boolean isHidden, String... keys) {
		this.displayName = displayName;
		this.configType = configType;
		this.isHidden = isHidden;
		this.keys = Arrays.asList(keys);
	}

	public int getNum() {
		for(int i = 0; i < values().length; i++) {
			if(values()[i] == this) return i + 1;
		}
		return -1;
	}

	public static Configurable[] getConfigurables() {
		List<Configurable> configurables = new ArrayList<>();
		for(Configurable value : values()) {
			if(value.isHidden) continue;
			configurables.add(value);
		}
		return configurables.toArray(new Configurable[0]);
	}

	public enum ConfigType {
		STRING("Input string"),
		NUMBER("Input value"),
		SNOWFLAKE("Input id");

		public String instructions;

		ConfigType(String instructions) {
			this.instructions = instructions;
		}
	}
}
