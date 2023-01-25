package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.firestore.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends DiscordCommand {
	public HelpCommand() {
		super("help");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "help for Despair discord bot");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.MEMBER)) {
			event.getChannel().sendMessage("You need to have member access to do this").queue();
			return;
		}

		List<String> helpMessage = new ArrayList<>();

		String prefix = Config.INSTANCE.PREFIX;
		helpMessage.add("> `" + prefix + "kos` - Modify the kos list");
		helpMessage.add("> `" + prefix + "truce` - Modify the truce list");
		helpMessage.add("> `" + prefix + "ping` - Visual check to see if the bot is responsive");
		helpMessage.add("> `" + prefix + "config` - Modify config values");
		helpMessage.add("> `" + prefix + "setup` - Set up all channels for the bot");
		helpMessage.add("> `" + prefix + "notify` - Notifies you when certain players begin streaking (tags can be either added to player on .kos add <name> [tag-1] [tag-2]..." +
				", be the players name, or be \"all\" or \"uber\")");
		helpMessage.add("> The `*` character displayed next to a name on the kos signifies as an API disabled player." +
				" Such players can only be detected by whether or not they are streaking, and their online/offline status is an assumption based off of that data");

		String sendMessage = "HELP";
		for(String line : helpMessage) {
			sendMessage += "\n" + line;
		}
		event.getChannel().sendMessage(sendMessage).queue();
	}
}
