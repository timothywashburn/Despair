package dev.kyro.despair.commands.admin;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.controllers.objects.KOS;
import dev.kyro.despair.threads.KOSDisplayThread;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ExitCommand extends DiscordCommand {
	public ExitCommand() {
		super("exit", "stop", "shutdown");
		adminCommand = true;
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		event.getChannel().sendMessage("Turning off processes and saving data").queue();

		KOSDisplayThread.running = false;
		for(DespairUser despairUser : UserManager.users) {
			if(despairUser.kosChannel == null) continue;
			despairUser.kosChannel.retrieveMessageById(despairUser.kosMessageID).queue((message) -> {
				message.editMessage("DESPAIR KOS BOT (Offline)").queue();
			}, failure -> {});
		}

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
}
