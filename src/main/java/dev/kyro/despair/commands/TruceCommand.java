package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.controllers.HypixelAPIManager;
import dev.kyro.despair.controllers.HypixelPlayer;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.firestore.KOS;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TruceCommand extends DiscordCommand {
	public TruceCommand() {
		super("truce");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "modify & view the truce list").addSubcommands(
				new SubcommandData("add", "add a player to the truce list")
						.addOption(OptionType.STRING, "player", "the name or uuid of the player", true)
						.addOption(OptionType.STRING, "category", "the type of truce", true, true)
						.addOption(OptionType.STRING, "duration", "duration in format 'Xw Xd Xh Xm' or 'perm'", true),
				new SubcommandData("extend", "extend the truce of a player")
						.addOption(OptionType.STRING, "player", "the name or uuid of the player", true, true)
						.addOption(OptionType.STRING, "duration", "duration in format 'Xw Xd Xh Xm' or 'perm'", true),
				new SubcommandData("remove", "remove a player from the truce list")
						.addOption(OptionType.STRING, "player", "the name or uuid of the player", true, true),
				new SubcommandData("list", "list the players on the truce list")
		);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
			event.reply("You need to have administrator access to do this").setEphemeral(true).queue();
			return;
		}

		String subCommand = event.getSubcommandName();
		if(subCommand == null) {
			event.reply("Please run a sub command").queue();
			return;
		}
		if(subCommand.equals("add")) {
			String category = event.getOption("category").getAsString();
			if(!Config.INSTANCE.getTruceListCategories().contains(category)) {
				event.reply("Not a valid category: " +
						String.join(", ", Config.INSTANCE.getTruceListCategories()) + "").setEphemeral(true).queue();
				return;
			}

			Duration duration;
			String durationString = event.getOption("duration").getAsString();
			if(durationString.equalsIgnoreCase("perm") || durationString.equalsIgnoreCase("permanent")) {
				duration = null;
			} else {
				try {
					duration = Misc.parseDuration(durationString);
				} catch(Exception exception) {
					exception.printStackTrace();
					event.reply("Invalid time provided").setEphemeral(true).queue();
					return;
				}
			}

			String playerIdentifier = event.getOption("player").getAsString();
			JSONObject requestData;
			HypixelPlayer hypixelPlayer;
			try {
				if(Misc.isUUID(playerIdentifier)) {
					requestData = HypixelAPIManager.request(UUID.fromString(playerIdentifier));
				} else {
					requestData = HypixelAPIManager.request(playerIdentifier);
				}
			} catch(Exception exception) {
				if(exception instanceof NoAPIKeyException) {
					event.reply("No api key set").setEphemeral(true).queue();
				} else if(exception instanceof InvalidAPIKeyException) {
					event.reply("Invalid api key").setEphemeral(true).queue();
				} else if(exception instanceof LookedUpNameRecentlyException) {
					event.reply("That name was already looked up recently. Use the player's uuid instead or wait a minute").setEphemeral(true).queue();
				}
				return;
			}
			try {
				hypixelPlayer = new HypixelPlayer(requestData);
			} catch(JSONException ignored) {
				event.reply("Invalid player").setEphemeral(true).queue();
				return;
			}

			if(KOS.INSTANCE.truceContainsPlayer(hypixelPlayer.UUID)) {
				event.reply(hypixelPlayer.name + " is already on the truce list").setEphemeral(true).queue();
				return;
			}

			KOS.TrucePlayer trucePlayer = new KOS.TrucePlayer(hypixelPlayer.name, hypixelPlayer.UUID.toString(), category, duration);
			trucePlayer.hypixelPlayer = hypixelPlayer;
			KOS.INSTANCE.addTrucePlayer(trucePlayer, true);
			if(duration == null) {
				event.reply("Permanently added `" + hypixelPlayer.name + "` to truce").queue();
			} else {
				event.reply("Added `" + hypixelPlayer.name + "` to truce for " + Misc.humanReadableFormat(duration)).queue();
			}

		} else if(subCommand.equals("extend")) {
			String playerIdentifier = event.getOption("player").getAsString();
			KOS.TrucePlayer extendPlayer = null;
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
				if(!trucePlayer.uuid.equals(playerIdentifier) && !trucePlayer.name.equalsIgnoreCase(playerIdentifier))
					continue;
				extendPlayer = trucePlayer;
				break;
			}
			if(extendPlayer == null) {
				if(Misc.isUUID(playerIdentifier)) {
					event.reply("Couldn't find that player").setEphemeral(true).queue();
				} else {
					event.reply("Couldn't find that player (They may have changed their name; try removing them with their uuid)").setEphemeral(true).queue();
				}
				return;
			}

			if(extendPlayer.trucedUntil == null) {
				event.reply("That player already has a permanent truce").setEphemeral(true).queue();
				return;
			}

			Duration duration;
			String durationString = event.getOption("duration").getAsString();
			if(durationString.equalsIgnoreCase("perm") || durationString.equalsIgnoreCase("permanent")) {
				duration = null;
			} else {
				try {
					duration = Misc.parseDuration(durationString);
				} catch(Exception exception) {
					exception.printStackTrace();
					event.reply("Invalid time provided").setEphemeral(true).queue();
					return;
				}
			}

			if(extendPlayer.trucedUntil.getTime() < new Date().getTime()) extendPlayer.trucedUntil = new Date();
			extendPlayer.extendTruce(duration);
			KOS.INSTANCE.save();
			if(duration == null) {
				event.reply("Changed truce for `" + extendPlayer.name + "` to be permanent").queue();
			} else {
				event.reply("Extended truce for `" + extendPlayer.name + "` by " + Misc.humanReadableFormat(duration)).queue();
			}
		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			String playerIdentifier = event.getOption("player").getAsString();
			KOS.TrucePlayer removePlayer = null;
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
				if(!trucePlayer.uuid.equals(playerIdentifier) && !trucePlayer.name.equalsIgnoreCase(playerIdentifier))
					continue;
				removePlayer = trucePlayer;
				break;
			}
			if(removePlayer == null) {
				if(Misc.isUUID(playerIdentifier)) {
					event.reply("Couldn't find that player").setEphemeral(true).queue();
				} else {
					event.reply("Couldn't find that player (They may have changed their name; try removing them with their uuid)").setEphemeral(true).queue();
				}
				return;
			}
			if(removePlayer.hypixelPlayer == null) {
				event.reply("Something went wrong while attempting to remove player. Please report this").setEphemeral(true).queue();
				return;
			}

			KOS.INSTANCE.removeTrucePlayer(removePlayer, true);
			event.reply("Removed `" + removePlayer.name + "` from the truce list").queue();

		} else if(subCommand.equals("list")) {
			String message = "TRUCED PLAYERS (" + KOS.INSTANCE.getTruceList().size() + ")";
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
				message += "\n> `" + (trucePlayer.name != null ? trucePlayer.name : trucePlayer.uuid) + "` - `" + trucePlayer.getTruceStatus() + "`";
			}
			event.reply(message).queue();
		} else {
			event.reply("Usage: `" + Config.INSTANCE.PREFIX + "truce <add/remove/list>`").queue();
		}
	}

	@Override
	public List<Command.Choice> autoComplete(CommandAutoCompleteInteractionEvent event, String currentOption, String currentValue) {
		List<Command.Choice> choices = new ArrayList<>();
		String subCommand = event.getSubcommandName();

		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) return choices;

		if((subCommand.equals("remove") || subCommand.equals("extend")) && currentOption.equals("player")) {
			for(KOS.TrucePlayer trucePlayer: KOS.INSTANCE.getTruceList()) {
				Command.Choice choice = new Command.Choice(trucePlayer.name, trucePlayer.name);
				if(currentValue.isEmpty()) {
					choices.add(choice);
				} else if(trucePlayer.name.toLowerCase().startsWith(currentValue)) choices.add(choice);
			}
		}
		if(currentOption.equals("category")) {
			for(String category : Config.INSTANCE.getTruceListCategories()) {
				Command.Choice choice = new Command.Choice(category, category);
				if(currentValue.isEmpty()) {
					choices.add(choice);
				} else if(category.startsWith(currentValue)) choices.add(choice);
			}
		}
		return choices;
	}
}
