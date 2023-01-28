package dev.kyro.despair.controllers;

import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.firestore.Config;
import org.json.JSONException;
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
					System.out.println("No api key set");
				} else if(exception instanceof InvalidAPIKeyException) {
					System.out.println("Invalid api key");
				} else if(exception instanceof LookedUpNameRecentlyException) {
					System.out.println("That name was already looked up recently. Use the player's uuid instead or wait a minute");
				}
				return;
			}
			try {
				hypixelPlayer = new HypixelPlayer(requestData);
				lastUpdate = System.currentTimeMillis();
			} catch(JSONException ignored) {
				System.out.println("Invalid player");
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
