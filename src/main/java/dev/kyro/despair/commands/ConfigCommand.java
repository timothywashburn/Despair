package dev.kyro.despair.commands;

import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.threads.ConfigThread;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Objects;

public class ConfigCommand extends DiscordCommand {
	public ConfigCommand() {
		super("config", "setting", "settings", "c", "s", "set");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		boolean isMember = Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) || event.getMember().isOwner();
		for(Role role : event.getMember().getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.ADMIN_ROLE_ID) continue;
			isMember = true;
			break;
		}
		if(!isMember) {
			event.getChannel().sendMessage("You need to have administrative access to do this").queue();
			return;
		}

		new ConfigThread(event.getTextChannel(), event.getAuthor()).start();
	}
}
