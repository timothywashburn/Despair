package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.threads.ConfigThread;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

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
		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
			event.reply("You need to have administrative access to do this").setEphemeral(true).queue();
			return;
		}

		event.reply("opening config").setEphemeral(true).queue();
		new ConfigThread(event.getTextChannel(), event.getMember()).start();
	}
}
