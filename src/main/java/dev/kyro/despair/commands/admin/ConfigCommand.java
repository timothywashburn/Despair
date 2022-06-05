package dev.kyro.despair.commands.admin;

import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.threads.ConfigThread;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ConfigCommand extends DiscordCommand {
	public ConfigCommand() {
		super("config", "configuration", "setting", "settings", "c", "s", "set");
		adminCommand = true;
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		new ConfigThread(event.getTextChannel(), event.getAuthor()).start();
	}
}
