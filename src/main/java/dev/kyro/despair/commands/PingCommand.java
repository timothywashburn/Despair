package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.Config;
import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class PingCommand extends DiscordCommand {
	public PingCommand() {
		super("ping");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		event.getChannel().sendMessage("Pong!").queue();
	}
}
