package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PingCommand extends DiscordCommand {
	public PingCommand() {
		super("ping");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "checks to see if the bot is responding");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.reply("pong!").queue();
	}
}
