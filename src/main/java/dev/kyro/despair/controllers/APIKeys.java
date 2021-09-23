package dev.kyro.despair.controllers;

public class APIKeys {
	public static int count = 0;

	public static String getAPIKey() {

		if(Config.INSTANCE.API_KEY.equals("")) return null;
		return Config.INSTANCE.API_KEY;
	}

	public static Config.KeyAndProxy getAPIKeyProxy() {

		if(Config.INSTANCE.KEY_PROXY_LIST.isEmpty()) return null;
		return Config.INSTANCE.KEY_PROXY_LIST.get(count++ % Config.INSTANCE.KEY_PROXY_LIST.size());
	}

	public static boolean hasKeys() {

		return !Config.INSTANCE.KEY_PROXY_LIST.isEmpty();
	}
}
