package dev.kyro.despair.misc;

import java.util.UUID;

public class Misc {

	public static boolean isUUID(String uuid) {
		try {
			UUID.fromString(uuid);
			return true;
		} catch(Exception ignored) {
			return false;
		}
	}
}
