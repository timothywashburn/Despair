package dev.kyro.despair.controllers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class KOSDisplay extends Thread {

	@Override
	public void run() {
		while(true) {

			Guild guild = DiscordManager.JDA.getGuildById(Config.INSTANCE.GUILD_ID);
			if(guild == null || KOS.INSTANCE.kosList.isEmpty()) {
				sleepThread();
				continue;
			}

			TextChannel displayChannel = guild.getTextChannelById(Config.INSTANCE.DISPLAY_CHANNEL_ID);
			if(displayChannel == null) {
				sleepThread();
				continue;
			}

			String display = "DESPAIR KOS BOT ||@everyone||";
			String online = "\n\nONLINE";
			String offline = "\n\nOFFLINE";

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

			String pattern = "HH:mm:ss"; SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
			display += "\n\n" + dateFormat.format(new Date().getTime() + 3 * 60 * 60 * 1000) + " EST";

			String finalDisplay = display;
			displayChannel.retrieveMessageById(Config.INSTANCE.DISPLAY_MESSAGE_ID).queue((message) -> {
				message.editMessage(finalDisplay).queue();
			}, failure -> {});

			sleepThread();
		}
	}

	public void sleepThread() {
		try {
			Thread.sleep(2500);
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println(System.currentTimeMillis());
		}
	}
}
