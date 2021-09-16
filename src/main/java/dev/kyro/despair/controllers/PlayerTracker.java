package dev.kyro.despair.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.type.Decimal;
import dev.kyro.despair.Despair;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;

import java.sql.Array;
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
			System.out.println(Despair.KOS.kosList.size() + " count: " + count);
			if(Despair.KOS.kosList.isEmpty()) {
				sleepThread();
				continue;
			}

			if(playerIteration.isEmpty() || count == playerIteration.size()) {
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
			}

			KOS.KOSPlayer kosPlayer = playerIteration.get(count);
			HypixelPlayer hypixelPlayer = kosPlayer.hypixelPlayer;

			new Thread(() -> {
				JSONObject requestData = HypixelAPIManager.request(hypixelPlayer.UUID);
				if(requestData == null) {
					String pattern = "HH:mm:ss"; SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
					System.out.println(dateFormat.format(new Date()) + " Error fetching data for uuid: " + hypixelPlayer.UUID + " name: " + hypixelPlayer.name);
					if(!hypixelPlayer.recentKills.isEmpty()) hypixelPlayer.recentKills.remove(0);
					return;
				}

				boolean wasOnline = hypixelPlayer.isOnline;
				hypixelPlayer.update(requestData);

//				TextChannel notifChannel = guild.getTextChannelById("859302194348294204");
//				if(notifChannel == null) {
//					System.out.println("Someone deleted the god dang notif channel");
//					return;
//				}
//
//				if(!wasOnline && hypixelPlayer.isOnline) notifChannel.sendMessage("Login: " + hypixelPlayer.name).queue();
//				if(Math.random() < (1D / 6D) && hypixelPlayer.megastreak.equalsIgnoreCase("uberstreak") && hypixelPlayer.getRecentKills() != 0)
//					notifChannel.sendMessage("\uD83D\uDCB8 Uber Alert: " + hypixelPlayer.name).queue();
			}).start();

			count++;
			sleepThread();
		}
	}

	public void sleepThread() {
		int dir = 500 / APIKeys.apiKeys.size();
		try {
			Thread.sleep(dir);
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println("Failed to sleep: " + System.currentTimeMillis());
		}
	}
}
