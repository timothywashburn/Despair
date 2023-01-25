package dev.kyro.despair.misc;

import java.time.Duration;
import java.util.UUID;

public class Misc {
	public static String humanReadableFormat(Duration duration) {
		return duration.toString()
				.substring(2)
				.replaceAll("(\\d[HMS])(?!$)", "$1 ")
				.toLowerCase();
	}

	public static boolean isUUID(String uuid) {
		try {
			UUID.fromString(uuid);
			return true;
		} catch(Exception ignored) {
			return false;
		}
	}

	public static String getUnicodeNumber(int number) {

		String unicode = "";
		number = Math.max(Math.min(number, 10), 0);
		switch(number) {
			case 0:
				unicode = "\u0030";
				break;
			case 1:
				unicode = "\u0031";
				break;
			case 2:
				unicode = "\u0032";
				break;
			case 3:
				unicode = "\u0033";
				break;
			case 4:
				unicode = "\u0034";
				break;
			case 5:
				unicode = "\u0035";
				break;
			case 6:
				unicode = "\u0036";
				break;
			case 7:
				unicode = "\u0037";
				break;
			case 8:
				unicode = "\u0038";
				break;
			case 9:
				unicode = "\u0039";
				break;
			case 10:
				return "\uD83D\uDD1F";
		}
		return unicode + "\u20E3";
	}
}
