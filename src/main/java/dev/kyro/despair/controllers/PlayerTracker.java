package dev.kyro.despair.controllers;

import dev.kyro.despair.Despair;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.firestore.KOS;
import dev.kyro.despair.firestore.Users;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerTracker extends Thread {
	public static int count = 0;
	public static ArrayList<KOS.KOSPlayer> playerIteration = new ArrayList<>();
	public static long lastIteration;
	public static Map<UUID, Long> notifyCooldown = new HashMap<>();

	@Override
	public void run() {
		while(true) {
//			System.out.println(Despair.KOS.kosList.size() + " count: " + count);
			if(Despair.KOS.kosList.isEmpty()) {
				sleepThread();
				continue;
			}

			if(playerIteration.isEmpty() || count == playerIteration.size() || count == getMaxPlayers()) {
				int playersExtra = Math.max(getMaxPlayers() - count, 0);
				count = 0;
				playerIteration.clear();
				playerIteration.addAll(Despair.KOS.kosList);
				if(lastIteration == 0) lastIteration = new Date().getTime();
				else {
					DecimalFormat format = new DecimalFormat("0.000");
					Date now = new Date();
//					System.out.println("Finished iteration in " + format.format((now.getTime() - lastIteration) / 1000D) + "s");
					lastIteration = now.getTime();
				}
				sleepThread(playersExtra * 500L);
			}

			KOS.KOSPlayer kosPlayer = playerIteration.get(count);
			HypixelPlayer hypixelPlayer = kosPlayer.hypixelPlayer;

			new Thread(() -> {
				JSONObject requestData;
				try {
					requestData = HypixelAPIManager.request(hypixelPlayer.UUID);
				} catch(Exception exception) {
					if(exception instanceof NoAPIKeyException) {
						System.out.println("no api key set");
					} else if(exception instanceof InvalidAPIKeyException) {
						System.out.println("Invalid api key");
					}
					return;
				}
				if(requestData == null) {
					String pattern = "HH:mm:ss";
					SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
					System.out.println(dateFormat.format(new Date()) + " Error fetching data for uuid: " + hypixelPlayer.UUID + " name: " + hypixelPlayer.name);
					if(!hypixelPlayer.recentKills.isEmpty()) hypixelPlayer.recentKills.remove(0);

					return;
				}

				boolean wasOnline = hypixelPlayer.isOnline;
				boolean wasStreaking = hypixelPlayer.getRecentKills() != 0;
				hypixelPlayer.update(requestData);
				if(!hypixelPlayer.name.equals(kosPlayer.name)) {
					kosPlayer.name = hypixelPlayer.name;
					KOS.INSTANCE.save();
				}

//				Check notify cooldown
				boolean canNotify = notifyCooldown.getOrDefault(hypixelPlayer.UUID, 0L) + 60_500 < System.currentTimeMillis();

//				Guild check
				Guild guild = DiscordManager.getMainGuild();
				if(guild != null) {

//					Display channel check
					TextChannel displayChannel = guild.getTextChannelById(Config.INSTANCE.KOS_DISPLAY_CHANNEL_ID);
					if(displayChannel != null) {

//						Thread check
						for(ThreadChannel threadChannel : displayChannel.getThreadChannels()) {
							if(!threadChannel.getId().equals(Config.INSTANCE.KOS_DISPLAY_MESSAGE_ID)) continue;
							String pingString = "";
							for(Users.DiscordUser discordUser : Users.INSTANCE.getUsersWithTags(hypixelPlayer, kosPlayer.tags)) {
								pingString += " <@" + discordUser.id + ">";
							}

							if(System.currentTimeMillis() - Despair.START_TIME - 500L > 1000L * PlayerTracker.getMaxPlayers()) {
								if(!wasOnline && hypixelPlayer.isOnline)
									threadChannel.sendMessage("Login: `" + hypixelPlayer.name + "`" + pingString).queue();
								if(wasOnline && !hypixelPlayer.isOnline)
									threadChannel.sendMessage("Logout: `" + hypixelPlayer.name + "`").queue();
							}

							if(isPlayerStreaking(hypixelPlayer)) {
								if(canNotify) {
									threadChannel.sendMessage("Streaking: `" + hypixelPlayer.name + "`" + pingString).queue();
//									Put on notify cooldown
									notifyCooldown.put(hypixelPlayer.UUID, System.currentTimeMillis());
								}
							}
						}
					}
				}
			}).start();

			count++;
			sleepThread();
		}
	}

	public static boolean isPlayerStreaking(HypixelPlayer hypixelPlayer) {
		return hypixelPlayer.recentKills.size() > 2 && hypixelPlayer.recentKills.get(hypixelPlayer.recentKills.size() - 1) -
				hypixelPlayer.recentKills.get(hypixelPlayer.recentKills.size() - 2) != 0;
	}

	public static int getMaxPlayers() {
		return Config.INSTANCE.MAX_PLAYERS;
	}

	public void sleepThread() {
		int dir = 500;
		try {
			Thread.sleep(dir);
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println("Failed to sleep: " + System.currentTimeMillis());
		}
	}

	public void sleepThread(long dir) {
		try {
			Thread.sleep(dir);
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println("Failed to sleep: " + System.currentTimeMillis());
		}
	}
}
