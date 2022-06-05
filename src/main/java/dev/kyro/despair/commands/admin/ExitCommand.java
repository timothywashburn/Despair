package dev.kyro.despair.commands.admin;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.controllers.objects.KOS;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ExitCommand extends DiscordCommand {
	public ExitCommand() {
		super("exit", "stop", "shutdown");
		adminCommand = true;
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
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
}
