package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.firestore.KOS;
import dev.kyro.despair.firestore.Users;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public class NotifyCommand extends DiscordCommand {
	public static List<String> specialTags = new ArrayList<>();

	static {
		specialTags.add("all");
		specialTags.add("uber");
	}

	public NotifyCommand() {
		super("notify");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "control streaking/login/logout notifications")
				.addSubcommands(
						new SubcommandData("add", "add players to notification list")
								.addOption(OptionType.STRING, "tag", "the players/group name OR all/uber", true),
						new SubcommandData("remove", "remove players from the notification list")
								.addOption(OptionType.STRING, "player", "the players/group name OR all/uber", true),
						new SubcommandData("list", "print out your nofitication list"),
						new SubcommandData("clear", "clear the notification list")
				);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.MEMBER)) {
			event.reply("You need to have member access to do this").setEphemeral(true).queue();
			return;
		}

		Users.DiscordUser discordUser = Users.INSTANCE.getUser(event.getMember().getIdLong());

		String subCommand = event.getSubcommandName();
		if(subCommand == null) {
			event.reply("Please run a sub command").queue();
			return;
		}
		if(subCommand.equals("add")) {

			String tag = event.getOption("tag").getAsString();
			String name = null;
			boolean foundPlayer = false;
			if(!specialTags.contains(tag)) {
				for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
					if(!player.name.equalsIgnoreCase(tag)) continue;
					foundPlayer = true;
					tag = player.uuid;
					name = player.name;
					break;
				}
				if(!foundPlayer) {
					event.reply("That player is not on your KOS list").setEphemeral(true).queue();
					return;
				}
			}
			if(discordUser.tags.contains(tag)) {
				event.reply("You already have that tag added").setEphemeral(true).queue();
				return;
			}

			discordUser.tags.add(tag);
			discordUser.save();
			event.reply("Added the tag: `" + (name != null ? name : tag) + "`").queue();
		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			String tag = event.getOption("tag").getAsString();
			String name = null;
			for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
				if(!player.name.equalsIgnoreCase(tag)) continue;
				tag = player.uuid;
				name = player.uuid;
				break;
			}
			if(!discordUser.tags.contains(tag)) {
				event.reply("You don't have that tag added").setEphemeral(true).queue();
				return;
			}

			discordUser.tags.remove(tag);
			discordUser.save();
			event.reply("Removed the tag: `" + (name != null ? name : tag) + "`").queue();
		} else if(subCommand.equals("list")) {
			String message = "TAGS (" + discordUser.tags.size() + ")";
			for(String specialTag : specialTags) {
				if(!discordUser.tags.contains(specialTag)) continue;
				message += "\n> `" + specialTag + "`";
			}
			for(String tag : discordUser.tags) {
				if(specialTags.contains(tag)) continue;
				boolean foundPlayer = false;
				for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
					if(!player.uuid.equals(tag)) continue;
					message += "\n> `" + player.name + "`";
					foundPlayer = true;
					break;
				}
				if(foundPlayer) continue;
				message += "\n> `" + tag + "`";
			}
			event.reply(message).queue();
		} else if(subCommand.equals("clear")) {
			discordUser.tags.clear();
			discordUser.save();
			event.reply("Removed all tags").queue();
		}
	}
}
