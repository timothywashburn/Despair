package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public class PingCommand extends DiscordCommand implements SlashCommand {
	public PingCommand() {
		super("ping");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		event.getChannel().sendMessage("pong!").queue();
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(command, "Checks to see if the bot is responding");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.reply("pong!").queue();
	}
}
