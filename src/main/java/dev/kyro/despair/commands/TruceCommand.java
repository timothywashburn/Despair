package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.firestore.KOS;
import dev.kyro.despair.firestore.Users;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
						.addOption(OptionType.STRING, "player", "the identifier of the player", true, true),
				new SubcommandData("list", "list the players on the truce list"),
				new SubcommandData("status", "view your truce status")
						.addOption(OptionType.USER, "member", "the truced discord user", false),
				new SubcommandData("assign", "assign a discord user to their associated truce")
						.addOption(OptionType.USER, "member", "the discord user to assign the truce to", true)
						.addOption(OptionType.STRING, "player", "the player on the truce list to assign the discord user to " +
								"(none to remove)",true, true)
		);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		String subCommand = event.getSubcommandName();
		if(subCommand == null) {
			event.reply("Please run a sub command").queue();
			return;
		}

		if(subCommand.equals("status")) {
			Member targetMember = event.getMember();
			if(event.getOption("member") != null) targetMember = event.getOption("member").getAsMember();
			if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.MEMBER) && targetMember != event.getMember()) {
				event.reply("As a non-member you can only view your own truce status").setEphemeral(true).queue();
				return;
			}
			if(targetMember == null) {
				event.reply("That user is not in the discord").setEphemeral(true).queue();
				return;
			}

			Users.DiscordUser discordTarget = Users.INSTANCE.getUser(targetMember.getIdLong());
			KOS.TrucePlayer trucePlayer = KOS.INSTANCE.getTrucePlayer(discordTarget);

			if(trucePlayer == null) {
				if(targetMember == event.getMember()) {
					event.reply("You are not on the truce list").setEphemeral(true).queue();
				} else {
					event.reply("That player isn't on truce (if they are, attach their discord with `/truce assign`)").setEphemeral(true).queue();
				}
				return;
			}

			String targetName = targetMember == event.getMember() ? "Your" : targetMember.getEffectiveName() + "'s";
			event.reply(targetName + " truce status is: `" + trucePlayer.getTruceStatus() + "`").queue();
			return;
		}

		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
			event.reply("You need to have administrator access to do this").setEphemeral(true).queue();
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

			String identifier = event.getOption("player").getAsString();
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
				if(!trucePlayer.name.equalsIgnoreCase(identifier)) continue;
				event.reply("That player is already on the truce list").setEphemeral(true).queue();
				return;
			}
			KOS.TrucePlayer trucePlayer = new KOS.TrucePlayer(identifier, category, duration);

			KOS.INSTANCE.addTrucePlayer(trucePlayer, true);
			if(duration == null) {
				event.reply("Permanently added `" + identifier + "` to truce").queue();
			} else {
				event.reply("Added `" + identifier + "` to truce for " + Misc.humanReadableFormat(duration)).queue();
			}

		} else if(subCommand.equals("extend")) {
			String identifier = event.getOption("player").getAsString();
			KOS.TrucePlayer extendPlayer = null;
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
				if(!trucePlayer.name.equalsIgnoreCase(identifier)) continue;
				extendPlayer = trucePlayer;
				break;
			}
			if(extendPlayer == null) {
				if(Misc.isUUID(identifier)) {
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
			String identifier = event.getOption("player").getAsString();
			KOS.TrucePlayer removePlayer = KOS.INSTANCE.getTrucePlayer(identifier);
			if(removePlayer == null) {
				event.reply("Couldn't find that player").setEphemeral(true).queue();
				return;
			}

			KOS.INSTANCE.removeTrucePlayer(removePlayer, true);
			event.reply("Removed `" + removePlayer.name + "` from the truce list").queue();

		} else if(subCommand.equals("list")) {
			String message = "TRUCED PLAYERS (" + KOS.INSTANCE.getTruceList().size() + ")";
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
				message += "\n> `" + trucePlayer.name + "` - `" + trucePlayer.getTruceStatus() + "`";
			}
			event.reply(message).queue();
		} else if(subCommand.equals("assign")) {
			Member targetMember = event.getOption("member").getAsMember();
			if(targetMember == null) {
				event.reply("That user is not in the discord").setEphemeral(true).queue();
				return;
			}

			String identifier = event.getOption("player").getAsString();
			if(identifier.equalsIgnoreCase("none")) {
				for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
					if(trucePlayer.discordID == null || !trucePlayer.discordID.equals(targetMember.getId())) continue;
					trucePlayer.discordID = null;
					KOS.INSTANCE.save();
					event.reply("Removed truce assignment from `" + targetMember.getEffectiveName() + "`").queue();
					return;
				}
				event.reply(targetMember.getEffectiveName() + " is not associated to an account on the truce list").setEphemeral(true).queue();
			} else {
				KOS.TrucePlayer trucePlayer = KOS.INSTANCE.getTrucePlayer(identifier);
				if(trucePlayer == null) {
					event.reply("Couldn't find that player").setEphemeral(true).queue();
					return;
				}
				trucePlayer.discordID = targetMember.getId();
				KOS.INSTANCE.save();
				event.reply("Assigned `" + identifier + "` to `" + targetMember.getEffectiveName() + "`").queue();
			}
		}
	}

	@Override
	public List<Command.Choice> autoComplete(CommandAutoCompleteInteractionEvent event, String currentOption, String currentValue) {
		List<Command.Choice> choices = new ArrayList<>();
		String subCommand = event.getSubcommandName();

		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) return choices;

		if((subCommand.equals("remove") || subCommand.equals("extend") || subCommand.equals("assign")) && currentOption.equals("player")) {
			for(KOS.TrucePlayer trucePlayer: KOS.INSTANCE.getTruceList()) {
				Command.Choice choice = new Command.Choice(trucePlayer.name, trucePlayer.name);
				if(currentValue.isEmpty()) {
					choices.add(choice);
				} else{
					for(String account : trucePlayer.name.toLowerCase().split("/")) {
						if(account.startsWith(currentValue)) choices.add(choice);
					}
				}
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
