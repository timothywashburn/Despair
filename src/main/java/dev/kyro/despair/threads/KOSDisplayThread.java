package dev.kyro.despair.threads;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.DespairUser;

import java.util.ArrayList;
import java.util.List;

public class KOSDisplayThread extends Thread {
	public static boolean running = true;

	public static List<DespairUser> editQueue = new ArrayList<>();
	public static long sleepForAfter = 0;
	public static final long sleepForInBetween = 500;

	@Override
	public void run() {
		while(running) {
			if(editQueue.isEmpty()) {
				sleepThread(sleepForAfter);
				editQueue.addAll(UserManager.users);
				sleepForAfter = Math.max(10_000 - sleepForInBetween * editQueue.size(), 0);
			}

			DespairUser despairUser = editQueue.remove(0);
			if(despairUser.kosChannel == null) continue;
			despairUser.kosChannel.retrieveMessageById(despairUser.kosMessageID).queue((message) -> {
				message.editMessage(despairUser.createKOSMessage()).queue();
			}, failure -> {});

			sleepThread(sleepForInBetween);
		}
	}

	public void sleepThread(long dir) {
		try {
			Thread.sleep(dir);
		} catch(InterruptedException ignored) {}
	}
}
