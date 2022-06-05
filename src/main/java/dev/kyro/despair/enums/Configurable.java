package dev.kyro.despair.enums;

import java.util.Arrays;
import java.util.List;

public enum Configurable {

	PREFIX("Bot Prefix", ConfigType.STRING, "prefix"),
	API_KEY("Api Key", ConfigType.STRING, "api"),
	MAX_PLAYERS("Max Players (Global)", ConfigType.NUMBER, "max"),
	GUILD_ID("Server", ConfigType.SNOWFLAKE, "server"),
	KOS_CATEGORY_ID("KOS Category", ConfigType.SNOWFLAKE, "kos"),
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
		NUMBER("Input value"),
		SNOWFLAKE("Input id");

		public String instructions;

		ConfigType(String instructions) {
			this.instructions = instructions;
		}
	}
}
