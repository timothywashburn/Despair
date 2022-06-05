package dev.kyro.despair.threads;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.DespairUser;

public class KOSDisplayThread extends Thread {
	public static boolean running = true;

	@Override
	public void run() {
		while(running) {
			for(DespairUser despairUser : UserManager.users) {
				if(despairUser.kosChannel == null) continue;
				despairUser.kosChannel.retrieveMessageById(despairUser.kosMessageID).queue((message) -> {
					message.editMessage(despairUser.createKOSMessage()).queue();
				}, failure -> {});
			}

			sleepThread();
		}
	}

	public void sleepThread() {
		try {
			Thread.sleep(10000);
		} catch(InterruptedException ignored) {}
	}
}
