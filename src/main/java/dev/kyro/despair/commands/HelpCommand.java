package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.Config;
import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends DiscordCommand {
	public HelpCommand() {
		super("help");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		List<String> helpMessage = new ArrayList<>();

		String prefix = Config.INSTANCE.PREFIX;
		helpMessage.add("> `" + prefix + "kos` - Modify the kos");
		helpMessage.add("> `" + prefix + "ping` - Visual check to see if the bot is responsive");
		helpMessage.add("> `" + prefix + "config` - Modify config values");
		helpMessage.add("> `" + prefix + "setup` - Command to easily set up the display message (way easier than trying to use the config)");
		helpMessage.add("> `" + prefix + "key` - Modify api keys and their respective proxies");

		String sendMessage = "HELP";
		for(String line : helpMessage) {
			sendMessage += "\n" + line;
		}
		event.getChannel().sendMessage(sendMessage).queue();
	}
}
