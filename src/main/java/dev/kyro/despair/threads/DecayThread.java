package dev.kyro.despair.threads;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.KOS;

public class DecayThread extends Thread {
	public static int count = 1;

	@Override
	public void run() {
		while(true) {
			if(count % 10 == 0) {
				for(DespairUser despairUser : UserManager.users) {
					if(despairUser.priorityBoost + 1 > DespairUser.MAX_CREDITS) continue;
					despairUser.priorityBoost++;
				}
			}
			if(count % 30 == 0) {
				for(DespairUser despairUser : UserManager.users) despairUser.save(false);
			}
			if(count % 10 == 0) {
				for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
					player.priority = Math.max(player.priority - 0.01 * player.priority, 0);
				}
			}
			if(count % 30 == 0) {
				KOS.INSTANCE.save();
			}
			count++;
			sleepThread();
		}
	}

	public void sleepThread() {
		try {
			Thread.sleep(1000 * 60);
		} catch(InterruptedException ignored) {}
	}
}
