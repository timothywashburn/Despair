package dev.kyro.despair.controllers;

import dev.kyro.despair.Despair;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.firestore.KOS;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisplayManager extends Thread {

	@Override
	public void run() {
		int count = 0;
		while(true) {
			Guild guild = DiscordManager.getMainGuild();
			if(guild == null || KOS.INSTANCE.kosList.isEmpty()) {
				sleepThread();
				continue;
			}

			TextChannel kosDisplayChannel = guild.getTextChannelById(Config.INSTANCE.KOS_DISPLAY_CHANNEL_ID);
			if(kosDisplayChannel != null) {
				String display = createKOSMessage();
				kosDisplayChannel.retrieveMessageById(Config.INSTANCE.KOS_DISPLAY_MESSAGE_ID).queue((message) -> {
					message.editMessage(display).queue();
				}, failure -> {});
			}

			TextChannel truceDisplayChannel = guild.getTextChannelById(Config.INSTANCE.TRUCE_DISPLAY_CHANNEL_ID);
			if(truceDisplayChannel != null) {
				String display = createTruceMessage();
				truceDisplayChannel.retrieveMessageById(Config.INSTANCE.TRUCE_DISPLAY_MESSAGE_ID).queue((message) -> {
					message.editMessage(display).queue();
				}, failure -> {});
			}

//			if(count % 6 == 0) {
				TextChannel pureDisplayChannel = guild.getTextChannelById(Config.INSTANCE.PURE_DISPLAY_CHANNEL_ID);
				if(pureDisplayChannel != null) {
					String display = createPureMessage();
					pureDisplayChannel.retrieveMessageById(Config.INSTANCE.PURE_DISPLAY_MESSAGE_ID).queue((message) -> {
						message.editMessage(display).queue();
					}, failure -> {});
				}
//			}

			sleepThread();
			count++;
		}
	}

	public static String createCurrentlyTracking() {
		double seconds = PlayerTracker.getMaxPlayers() / 2.0;
		DecimalFormat decimalFormat = new DecimalFormat("0.0");
		return "checking up to **" + PlayerTracker.getMaxPlayers() + "** player" + (PlayerTracker.getMaxPlayers() == 1 ? "" : "s") +
				" every **" + decimalFormat.format(seconds) + "** seconds";
	}

	public static String createKOSMessage() {
		String display = "DESPAIR KOS LIST ||@everyone||\nCurrently *" + createCurrentlyTracking() + "*";
		String online = "\n\nONLINE";
		String offline = "\n\nOFFLINE";

		int onlinePlayerCount = 0;
		int offlinePlayerCount = 0;
		for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
			if(player.hypixelPlayer.isOnline) onlinePlayerCount++;
			else offlinePlayerCount++;
		}
		online += " (" + onlinePlayerCount + "/" + KOS.INSTANCE.kosList.size() + ")";
		offline += " (" + offlinePlayerCount + "/" + KOS.INSTANCE.kosList.size() + ")";

		for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
			if(player.hypixelPlayer.lastLogin == 0) continue;
			if(player.hypixelPlayer.isOnline) {
				String recentKills = "";
				if(player.hypixelPlayer.recentKills.size() < 2) {
					recentKills += 0;
				} else {
					for(int j = 1; j < player.hypixelPlayer.recentKills.size(); j++)
						recentKills += player.hypixelPlayer.recentKills.get(j) - player.hypixelPlayer.recentKills.get(j - 1) + " ";
				}
				recentKills = recentKills.trim();
				online += "\n> `" + player.name + "` - `" + player.hypixelPlayer.megastreak + "` [" + player.hypixelPlayer.getRecentKills() + "] ||[" + recentKills + "]||";
			} else if(player.hypixelPlayer.isOnlineWithApiDisabled()) {
				String recentKills = "";
				if(player.hypixelPlayer.recentKills.size() < 2) {
					recentKills += 0;
				} else {
					for(int j = 1; j < player.hypixelPlayer.recentKills.size(); j++)
						recentKills += player.hypixelPlayer.recentKills.get(j) - player.hypixelPlayer.recentKills.get(j - 1) + " ";
				}
				recentKills = recentKills.trim();
				online += "\n> *`" + player.name + "` - `" + player.hypixelPlayer.megastreak + "` [" + player.hypixelPlayer.getRecentKills() + "] ||[" + recentKills + "]||";
			} else {
				offline += "\n> ";
				if(player.hypixelPlayer.apiDisabled) offline += "*";
				offline += "`" + player.name + "` - " + player.hypixelPlayer.getTimeOffline();
			}
		}

		display += online;
		display += offline;

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
		display += "\n\n" + dateFormat.format(OffsetDateTime.now(Despair.TIME_ZONE)) + " EST";
		return display;
	}

	public static String createTruceMessage() {
		String display = "DESPAIR TRUCE LIST ||@everyone||";

		List<String> categories = new ArrayList<>(Config.INSTANCE.getTruceListCategories());
		categories.add(null);
		for(String category : categories) {
			List<KOS.TrucePlayer> players = KOS.INSTANCE.getPlayersInCategory(category);
			if(players.isEmpty()) continue;
			display += "\n\n" + (category == null ? "INVALID CATEGORY" : category.toUpperCase()) + " (" + players.size() + ")";
			for(KOS.TrucePlayer player : players) display += "\n> `" + player.name + "` - `" + player.getTruceStatus() + "`";
		}

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
		display += "\n\n" + dateFormat.format(OffsetDateTime.now(Despair.TIME_ZONE)) + " EST";
		return display;
	}

	public static String createPureMessage() {
		String display = "DESPAIR PURE TRACKER ||@everyone||\n\n";

		if(PureTracker.hypixelPlayer == null) {
			display += "NO ACCOUNT FOUND";
		} else {
			display += "TRACKING PURE: `" + PureTracker.hypixelPlayer.name + "`";
			if(PureTracker.lastUpdate + 60 * 60 * 1000 > System.currentTimeMillis()) {
				for(Map.Entry<HypixelPlayer.PureType, Integer> entry : PureTracker.hypixelPlayer.pureMap.entrySet())
					display += "\n> " + entry.getValue() + "x " + entry.getKey().displayName;
			} else {
				display += "\nno recent data";
			}
		}

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
		display += "\n\n" + dateFormat.format(OffsetDateTime.now(Despair.TIME_ZONE)) + " EST";
		return display;
	}

	public void sleepThread() {
		try {
			Thread.sleep(10000);
		} catch(InterruptedException exception) {
			exception.printStackTrace();
			System.out.println(System.currentTimeMillis());
		}
	}
}
