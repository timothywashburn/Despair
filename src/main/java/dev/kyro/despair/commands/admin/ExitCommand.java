package dev.kyro.despair.commands.admin;

import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.threads.ExitThread;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ExitCommand extends DiscordCommand {
	public ExitCommand() {
		super("exit", "stop", "shutdown");
		adminCommand = true;
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		new ExitThread(event).start();
	}
}
