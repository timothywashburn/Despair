package dev.kyro.despair.commands;

import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DiscordCommand;
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
		helpMessage.add("> `" + prefix + "help` - View this message");
		helpMessage.add("> `" + prefix + "kos` - Modify the KOS list");
		helpMessage.add("> `" + prefix + "view` - View the KOS list");
		helpMessage.add("> `" + prefix + "notify/tags/pings` - You will be pinged for notifications of players on this list");
		helpMessage.add("> `" + prefix + "status` - Visual check to see if the bot is responsive");
		helpMessage.add("> `" + prefix + "bal` - Displays the number of credits you have");
		helpMessage.add("> `" + prefix + "bump` - Increase the priority of an account on the KOS list");
		helpMessage.add("> The [x] display where x is a number on the `.view` of the KOS " +
				"list shows how many kills (approximately) the player has gotten in the last minute");
		helpMessage.add("> The [x] display where x is a number on `.kos list` shows how many credits are assigned to a player");
		helpMessage.add("> Credits can be given to players to boost them out of the \"UNTRACKED\" section");
		helpMessage.add("> Notifications are displayed for login, logout, and when someone has > 0 kills in the last minute");
		helpMessage.add("> The `*` character displayed next to a name on the KOS list signifies as an API disabled player. " +
				"Such players can only be detected by whether or not they are streaking, and their online/offline status is an assumption based off of that data");

		List<String> adminHelp = new ArrayList<>();
		adminHelp.add("\nADMIN COMMANDS");
		adminHelp.add("> `" + prefix + "config` - Modify the bot's config");
		adminHelp.add("> `" + prefix + "eco` - Add, remove, or set a given players credits");
		adminHelp.add("> `" + prefix + "exit` - Stop the bot and save all unsaved data");

		String sendMessage = "HELP";
		for(String line : helpMessage) sendMessage += "\n" + line;
		if(Despair.isAdmin(event.getMember())) {
			for(String line : adminHelp) sendMessage += "\n" + line;
		}
		event.getChannel().sendMessage(sendMessage).queue();
	}
}
