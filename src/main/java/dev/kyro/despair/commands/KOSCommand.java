package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.*;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.firestore.KOS;
import dev.kyro.despair.firestore.Users;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KOSCommand extends DiscordCommand {
	public KOSCommand() {
		super("kos");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "Modify & view the kos list")
				.addSubcommands(
						new SubcommandData("add", "add a player to the kos list")
								.addOption(OptionType.STRING, "player", "the name or uuid of the player", true),
						new SubcommandData("remove", "remove a player from the kos list")
								.addOption(OptionType.STRING, "player", "the name or uuid of the player", true, true),
						new SubcommandData("list", "view the kos list")
				);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.MEMBER)) {
			event.reply("You need to have member access to do this").setEphemeral(true).queue();
			return;
		}

		String subCommand = event.getSubcommandName();
		if(subCommand.equals("add")) {
			if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
				event.reply("You need to have administrator access to do this").setEphemeral(true).queue();
				return;
			}
			if(KOS.INSTANCE.kosList.size() >= PlayerTracker.getMaxPlayers()) {
				event.reply("Max amount of players reached").setEphemeral(true).queue();
				return;
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

			if(KOS.INSTANCE.kosContainsPlayer(hypixelPlayer.UUID)) {
				event.reply(hypixelPlayer.name + " is already on the kos").setEphemeral(true).queue();
				return;
			}

			List<String> tags = new ArrayList<>();
//			TODO: Fix
//			for(int i = 2; i < args.size(); i++) tags.add(args.get(i));

			KOS.KOSPlayer kosPlayer = new KOS.KOSPlayer(hypixelPlayer.name, hypixelPlayer.UUID.toString(), tags);
			kosPlayer.hypixelPlayer = hypixelPlayer;
			KOS.INSTANCE.addKOSPlayer(kosPlayer, true);
			event.reply("Added `" + hypixelPlayer.name + "` to the kos list").queue();

		} else if(subCommand.equals("remove")) {
			if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
				event.reply("You need to have administrator access to do this").setEphemeral(true).queue();
				return;
			}
			String playerIdentifier = event.getOption("player").getAsString();
			KOS.KOSPlayer removePlayer = null;
			for(KOS.KOSPlayer kosPlayer : KOS.INSTANCE.kosList) {
				if(!kosPlayer.uuid.equals(playerIdentifier) && !kosPlayer.name.equalsIgnoreCase(playerIdentifier))
					continue;
				removePlayer = kosPlayer;
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

			for(Users.DiscordUser users : Users.INSTANCE.getUsersWithTags(removePlayer.hypixelPlayer, removePlayer.tags)) {
				users.tags.remove(removePlayer.uuid);
			}
			Users.INSTANCE.save();

			KOS.INSTANCE.removeKOSPlayer(removePlayer, true);
			event.reply("Removed `" + removePlayer.name + "` from the kos list").setEphemeral(true).queue();

		} else if(subCommand.equals("list")) {
			String message = "KOS PLAYERS";
			for(KOS.KOSPlayer kosPlayer : KOS.INSTANCE.kosList) {
				message += "\n> `" + (kosPlayer.name != null ? kosPlayer.name : kosPlayer.uuid) + "`" + kosPlayer.getTagsAsString();
			}
			event.reply(message).queue();
		}
	}

	@Override
	public List<Command.Choice> autoComplete(CommandAutoCompleteInteractionEvent event, String currentOption, String currentValue) {
		List<Command.Choice> choices = new ArrayList<>();
		String subCommand = event.getSubcommandName();

		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.MEMBER)) return choices;

		if(subCommand.equals("remove") && currentOption.equals("player")) {
			if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) return choices;
			for(KOS.KOSPlayer kosPlayer: KOS.INSTANCE.kosList) {
				Command.Choice choice = new Command.Choice(kosPlayer.name, kosPlayer.name);
				if(currentValue.isEmpty()) {
					choices.add(choice);
				} else if(kosPlayer.name.toLowerCase().startsWith(currentValue)) choices.add(choice);
			}
		}
		return choices;
	}
}
