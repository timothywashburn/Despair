package dev.kyro.despair.controllers;

import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.KOS;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ThreadChannel;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerTracker extends Thread {
	public static int count = 0;
	public static ArrayList<KOS.KOSPlayer> playerIteration = new ArrayList<>();

	@Override
	public void run() {
		while(true) {
//			System.out.println(Despair.KOS.kosList.size() + " count: " + count);
			if(Despair.KOS.kosList.isEmpty()) {
				sleepThread();
				continue;
			}

			if(playerIteration.isEmpty() || count == playerIteration.size()) {
				int playersExtra = Math.max(getMaxPlayers() - count, 0);
				count = 0;
				playerIteration.clear();
				List<KOS.KOSPlayer> newKOS = getSortedKOS().stream().limit(20).collect(Collectors.toList());
				playerIteration.addAll(newKOS);

				List<KOS.KOSPlayer> toRemove = new ArrayList<>(getSortedKOS());
				toRemove.removeAll(newKOS);
				for(KOS.KOSPlayer kosPlayer : toRemove) kosPlayer.hypixelPlayer.clear();

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

				Guild guild = DiscordManager.getGuild();
				if(guild != null) {

					if(System.currentTimeMillis() - Despair.START_TIME > 1000 * 60) {
						if(!wasOnline && hypixelPlayer.isOnline)
							sendNotification(kosPlayer, "Login: `" + hypixelPlayer.name + "`");
						if(wasOnline && !hypixelPlayer.isOnline)
							sendNotification(kosPlayer, "Logout: `" + hypixelPlayer.name + "`");
					}

					if(hypixelPlayer.recentKills.size() > 2 && hypixelPlayer.recentKills.get(hypixelPlayer.recentKills.size() - 1) -
							hypixelPlayer.recentKills.get(hypixelPlayer.recentKills.size() - 2) != 0) {
						sendNotification(kosPlayer, "Streaking: `" + hypixelPlayer.name + "`");
					}
				}
			}).start();

			count++;
			sleepThread();
		}
	}

	public static void sendNotification(KOS.KOSPlayer kosPlayer, String notification) {
		for(DespairUser despairUser : UserManager.users) {
			if(!despairUser.kosList.contains(kosPlayer.hypixelPlayer.UUID.toString())) continue;
			if(despairUser.kosChannel == null) continue;

			for(ThreadChannel threadChannel : despairUser.kosChannel.getThreadChannels()) {
				if(threadChannel.getIdLong() != despairUser.kosMessageID) continue;
				threadChannel.sendMessage(notification).queue();
			}
		}
	}

	public static List<KOS.KOSPlayer> getSortedKOS() {
		List<KOS.KOSPlayer> tempList = new ArrayList<>(Despair.KOS.kosList);
		List<KOS.KOSPlayer> sortedList = new ArrayList<>();
		for(KOS.KOSPlayer kosPlayer : tempList) {
			if(sortedList.isEmpty()) {
				sortedList.add(kosPlayer);
				continue;
			}
			for(int i = 0; i < sortedList.size(); i++) {
				KOS.KOSPlayer testPlayer = sortedList.get(i);
				if(testPlayer.priority >= kosPlayer.priority) continue;
				sortedList.add(i, kosPlayer);
				break;
			}
			sortedList.add(kosPlayer);
		}
		return sortedList;
	}

	public static int getMaxPlayers() {
		return Config.INSTANCE.MAX_PLAYERS;
	}

	public void sleepThread() {
		int dir = 500 / 1;
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
