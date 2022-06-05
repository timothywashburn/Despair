package dev.kyro.despair.controllers.objects;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public abstract class DiscordCommand {

	public String command;
	public List<String> aliases;
	public boolean adminCommand = false;

	public DiscordCommand(String command, String... args) {
		this.command = command;
		this.aliases = Arrays.asList(args);
	}

	public abstract void execute(MessageReceivedEvent event, List<String> args);
}
