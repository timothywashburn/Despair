package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class TestCommand extends DiscordCommand {
	public TestCommand() {
		super("test");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "development command");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.reply("working").queue();
	}
}
