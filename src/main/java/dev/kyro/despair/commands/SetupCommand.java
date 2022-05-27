package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.Config;
import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.enums.Configurable;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SetupCommand extends DiscordCommand {
	public SetupCommand() {
		super("setup");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		boolean isAdmin = Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) || event.getMember().isOwner();
		for(Role role : event.getMember().getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.ADMIN_ROLE_ID) continue;
			isAdmin = true;
			break;
		}
		if(!isAdmin) {
			event.getChannel().sendMessage("You need to have administrative access to do this").queue();
			return;
		}

		if(args.isEmpty()) {

			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "setup <display>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("display")) {
			if(event.getGuild().getIdLong() != Config.INSTANCE.GUILD_ID) {
				event.getChannel().sendMessage("Guild ID does not match").queue();
				return;
			}

			Config.INSTANCE.set(Configurable.DISPLAY_CHANNEL_ID, event.getChannel().getId());
			event.getChannel().sendMessage("Setting up display message").queue(message -> {
				Config.INSTANCE.set(Configurable.DISPLAY_MESSAGE_ID, message.getId());
				Config.INSTANCE.save();
				event.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);
			});
		}
	}
}
