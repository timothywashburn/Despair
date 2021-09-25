package dev.kyro.despair.controllers;

import dev.kyro.despair.Despair;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlayerTracker extends Thread {
	public static int count = 0;
	public static ArrayList<KOS.KOSPlayer> playerIteration = new ArrayList<>();
	public static long lastIteration;

	@Override
	public void run() {
		while(true) {
//			System.out.println(Despair.KOS.kosList.size() + " count: " + count);
			if(Despair.KOS.kosList.isEmpty()) {
				sleepThread();
				continue;
			}

			if(playerIteration.isEmpty() || count == playerIteration.size()) {
				int playersExtra = getMaxPlayers() - count;
				if(playersExtra < 0) playersExtra = 0;
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
				sleepThread(playersExtra * (APIKeys.getAPIKey() == null ? 500L / Config.INSTANCE.KEY_PROXY_LIST.size() : 500));
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
					} else if(exception instanceof AuthenticationException) {
						System.out.println("Invalid proxy");
					} else if(exception instanceof HttpHostConnectException) {
						System.out.println("Connection exception");
					}
					return;
				}
				if(requestData == null) {
					String pattern = "HH:mm:ss"; SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
					System.out.println(dateFormat.format(new Date()) + " Error fetching data for uuid: " + hypixelPlayer.UUID + " name: " + hypixelPlayer.name);
					if(!hypixelPlayer.recentKills.isEmpty()) hypixelPlayer.recentKills.remove(0);

					return;
				}

				boolean wasOnline = hypixelPlayer.isOnline;
				hypixelPlayer.update(requestData);

				Guild guild = DiscordManager.JDA.getGuildById(Config.INSTANCE.GUILD_ID);
				if(guild != null) {
					TextChannel notifyChannel = guild.getTextChannelById(Config.INSTANCE.NOTIFY_CHANNEL_ID);
					if(notifyChannel != null) {
						if(!wasOnline && hypixelPlayer.isOnline) notifyChannel.sendMessage("Login: `" + hypixelPlayer.name + "`").queue();
						if(wasOnline && !hypixelPlayer.isOnline) notifyChannel.sendMessage("Logout: `" + hypixelPlayer.name + "`").queue();
					}
				}
			}).start();

			count++;
			sleepThread();
		}
	}

	public static int getMaxPlayers() {
		return APIKeys.getAPIKey() == null ? 20 * Config.INSTANCE.KEY_PROXY_LIST.size() : 20;
	}

	public void sleepThread() {
		int dir = APIKeys.getAPIKey() == null ? 500 / (Config.INSTANCE.KEY_PROXY_LIST.size() == 0 ? 1 : Config.INSTANCE.KEY_PROXY_LIST.size()) : 500;
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
