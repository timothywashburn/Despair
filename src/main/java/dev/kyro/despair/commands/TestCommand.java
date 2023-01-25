package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.SlashCommand;
import dev.kyro.despair.firestore.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;
import java.util.Objects;

public class TestCommand extends DiscordCommand implements SlashCommand {
	public TestCommand() {
		super("test");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		boolean hasPermission = Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) || event.getMember().isOwner();
		for(Role role : event.getMember().getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.ADMIN_ROLE_ID) continue;
			hasPermission = true;
			break;
		}
		if(!hasPermission) return;

		event.getChannel().sendMessage("hi").queue();
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(command, "Development command");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.reply("working").queue();
	}
}
