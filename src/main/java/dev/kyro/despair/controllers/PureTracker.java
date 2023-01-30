package dev.kyro.despair.controllers;

import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.firestore.Config;
import org.json.JSONObject;

import java.util.UUID;

public class PureTracker extends Thread {
	public static HypixelPlayer hypixelPlayer;
	public static long lastUpdate;

	@Override
	public void run() {
		while(true) {
			if(Config.INSTANCE.PURE_ALT_UUID.isEmpty()) {
				sleepThread();
				continue;
			}

			JSONObject requestData;
			try {
				requestData = HypixelAPIManager.request(UUID.fromString(Config.INSTANCE.PURE_ALT_UUID));
			} catch(Exception exception) {
				if(exception instanceof NoAPIKeyException) {
					System.out.println("[Pure Tracker] No api key set");
				} else if(exception instanceof InvalidAPIKeyException) {
					System.out.println("[Pure Tracker] Invalid api key");
				}
				return;
			}
			try {
				hypixelPlayer = new HypixelPlayer(requestData);
				lastUpdate = System.currentTimeMillis();
			} catch(Exception ignored) {
				System.out.println("[Pure Tracker] Invalid player");
				return;
			}

			hypixelPlayer.loadPureMap();
			sleepThread();
		}
	}

	public void sleepThread() {
		int dir = 10_000;
		try {
			Thread.sleep(dir);
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println("Failed to sleep: " + System.currentTimeMillis());
		}
	}
}
