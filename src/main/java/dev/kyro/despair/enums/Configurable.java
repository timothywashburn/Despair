package dev.kyro.despair.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Configurable {

	PREFIX("Bot Prefix", ConfigType.STRING, false, "prefix"),
	API_KEY("Api Key", ConfigType.STRING, false, "api"),
	MAX_PLAYERS("Max Players", ConfigType.NUMBER, false, "max"),
	TRUCE_CATEGORIES("Truce Categories", ConfigType.STRING_LOWER, false, "trucecategories"),
	GUILD_ID("Server", ConfigType.SNOWFLAKE, true, "server"),
	KOS_DISPLAY_CHANNEL_ID("KOS Display Channel", ConfigType.SNOWFLAKE, true, "kosdisplay"),
	KOS_DISPLAY_MESSAGE_ID("KOS Display Message", ConfigType.SNOWFLAKE, true, "kosmessage"),
	TRUCE_DISPLAY_CHANNEL_ID("Truce Display Channel", ConfigType.SNOWFLAKE, true, "trucedisplay"),
	TRUCE_DISPLAY_MESSAGE_ID("Truce Display Message", ConfigType.SNOWFLAKE, true, "trucemessage"),
	PURE_DISPLAY_CHANNEL_ID("Pure Display Channel", ConfigType.SNOWFLAKE, true, "puredisplay"),
	PURE_DISPLAY_MESSAGE_ID("Pure Account", ConfigType.SNOWFLAKE, true, "puremessage"),
	PURE_ALT_UUID("Pure Display Message", ConfigType.STRING, false, "purealt"),
	TRIAL_ROLE_ID("Trial Role", ConfigType.SNOWFLAKE, true, "trial"), // Same permissions as member just a second role
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
		STRING_LOWER("Input string"),
		NUMBER("Input value"),
		SNOWFLAKE("Input id (ex: 458458767634464792, not #channel-name or @user)");

		public String instructions;

		ConfigType(String instructions) {
			this.instructions = instructions;
		}
	}
}
