package dev.kyro.despair.controllers;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand {
	SlashCommandData getCommandStructure();
	void execute(SlashCommandInteractionEvent event);
}
