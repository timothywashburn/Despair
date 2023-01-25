package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.threads.ConfigThread;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

public class ConfigCommand extends DiscordCommand {
	public ConfigCommand() {
		super("config");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "open up the configuration panel");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {

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

		new ConfigThread(event.getTextChannel(), event.getMember()).start();
	}
}
