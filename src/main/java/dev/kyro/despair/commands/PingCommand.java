package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.objects.DiscordCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class PingCommand extends DiscordCommand {
	public PingCommand() {
		super("status", "online");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		event.getChannel().sendMessage("The bot is currently online!").queue();
	}
}
