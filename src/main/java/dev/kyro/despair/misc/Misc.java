package dev.kyro.despair.misc;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Misc {
	private static final Pattern periodPattern = Pattern.compile("(\\d+)([mhdw])");

	public static String obfuscateUUID(UUID uuid) {
		String uuidString = uuid.toString();
		Pattern pattern = Pattern.compile("^([\\w\\d]+)-([\\w\\d-]+)-([\\w\\d]+)$");
		Matcher matcher = pattern.matcher(uuidString);
		if (matcher.find()) {
			String firstPart = matcher.group(1);
			String lastPart = matcher.group(3);
			return firstPart + "..." + lastPart;
		}
		return uuidString;
	}

	public static Duration parseDuration(String durationString) throws Exception {
		durationString = durationString.toLowerCase().replaceAll(" ", "");
		Matcher matcher = periodPattern.matcher(durationString);
		Duration duration = Duration.ZERO;
		while(matcher.find()) {
			int num = Integer.parseInt(matcher.group(1));
			String typ = matcher.group(2);
			switch(typ) {
				case "m":
					duration = duration.plus(Duration.ofMinutes(num));
					break;
				case "h":
					duration = duration.plus(Duration.ofHours(num));
					break;
				case "d":
					duration = duration.plus(Duration.ofDays(num));
					break;
				case "w":
					duration = duration.plus(Duration.ofDays(num).multipliedBy(7));
			}
		}
		if(duration.equals(Duration.ZERO)) throw new Exception();
		return duration;
	}

	public static String humanReadableFormat(Duration duration) {
		long millis = duration.toMillis();
		long days = millis / (24 * 60 * 60 * 1000);
		millis %= (24 * 60 * 60 * 1000);
		long hours = millis / (60 * 60 * 1000);
		millis %= (60 * 60 * 1000);
		long minutes = millis / (60 * 1000);
		millis %= (60 * 1000);
		long seconds = millis / 1000;

		String displayName = "";
		if(days != 0) displayName += days + "d ";
		if(hours != 0) displayName += hours + "h ";
		if(minutes != 0) displayName += minutes + "m ";
		if(seconds != 0) displayName += seconds + "s";
		return displayName;
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
