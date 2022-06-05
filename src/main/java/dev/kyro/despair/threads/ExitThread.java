package dev.kyro.despair.threads;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.KOS;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ExitThread extends Thread {
	public MessageReceivedEvent event;

	public ExitThread(MessageReceivedEvent event) {
		this.event = event;
	}

	@Override
	public void run() {
		event.getChannel().sendMessage("Notifying players").queue();
		KOSDisplayThread.running = false;
		for(DespairUser despairUser : UserManager.users) {
			if(despairUser.kosChannel == null) continue;
			sleepThread(250);
			despairUser.kosChannel.retrieveMessageById(despairUser.kosMessageID).queue((message) -> {
				message.editMessage("DESPAIR KOS BOT (Offline)").queue();
			}, failure -> {});
		}

		event.getChannel().sendMessage("Saving data").queue();
		for(DespairUser despairUser : UserManager.users) despairUser.save(true);
		KOS.INSTANCE.save();

		new Thread(() -> {
			try {
				Thread.sleep(3000);
			} catch(InterruptedException ignored) {}
			event.getChannel().sendMessage("Turning off bot").queue();
			System.exit(0);
		}).start();
	}

	public void sleepThread(long dir) {
		try {
			Thread.sleep(dir);
		} catch(InterruptedException ignored) {}
	}
}
