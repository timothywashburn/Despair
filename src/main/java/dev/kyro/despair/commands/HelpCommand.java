package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.Config;
import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HelpCommand extends DiscordCommand {
	public HelpCommand() {
		super("help");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		boolean isAdmin = Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) || event.getMember().isOwner();
		for(Role role : event.getMember().getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.MEMBER_ROLE_ID) continue;
			isAdmin = true;
			break;
		}
		if(!isAdmin) {
			event.getChannel().sendMessage("You need to have member access to do this").queue();
			return;
		}

		List<String> helpMessage = new ArrayList<>();

		String prefix = Config.INSTANCE.PREFIX;
		helpMessage.add("> `" + prefix + "kos` - Modify the kos");
		helpMessage.add("> `" + prefix + "ping` - Visual check to see if the bot is responsive");
		helpMessage.add("> `" + prefix + "config` - Modify config values");
		helpMessage.add("> `" + prefix + "setup` - Command to easily set up the display message (way easier than trying to use the config)");
		helpMessage.add("> `" + prefix + "key` - Modify api keys and their respective proxies");
		helpMessage.add("> `" + prefix + "notify` - Notifies you when certain players begin streaking (tags can be either added to player on .kos add <name> [tag-1] [tag-2]..." +
				", be the players name, or be \"all\")");
		helpMessage.add("> The `*` character displayed next to a name on the kos signifies as an API disabled player." +
				" Such players can only be detected by whether or not they are streaking, and their online/offline status is an assumption based off of that data");

		String sendMessage = "HELP";
		for(String line : helpMessage) {
			sendMessage += "\n" + line;
		}
		event.getChannel().sendMessage(sendMessage).queue();
	}
}