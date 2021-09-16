package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SettingsCommand extends DiscordCommand {
	public SettingsCommand() {
		super("settings", "setting", "config", "c", "s", "set");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		event.getChannel().sendMessage("settings").queue();
	}
}
