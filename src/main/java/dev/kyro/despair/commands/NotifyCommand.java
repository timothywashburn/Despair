package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

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
		return Commands.slash(name, "control streaking/login/logout notifications");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
//		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.MEMBER)) {
//			event.getChannel().sendMessage("You need to have member access to do this").queue();
//			return;
//		}
//
//		if(args.isEmpty()) {
//			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify <add/remove/clear/list>`").queue();
//			return;
//		}
//
//		String subCommand = args.get(0).toLowerCase();
//		Users.DiscordUser discordUser = Users.INSTANCE.getUser(event.getAuthor().getIdLong());
//		if(subCommand.equals("add")) {
//			if(args.size() < 2) {
//				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify add <tag>`").queue();
//				return;
//			}
//
//			String tag = args.get(1).toLowerCase();
//			String name = null;
//			boolean foundPlayer = false;
//			if(!specialTags.contains(tag)) {
//				for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
//					if(!player.name.equalsIgnoreCase(tag)) continue;
//					foundPlayer = true;
//					tag = player.uuid;
//					name = player.name;
//					break;
//				}
//				if(!foundPlayer) {
//					event.getChannel().sendMessage("That player is not on your KOS list").queue();
//					return;
//				}
//			}
//			if(discordUser.tags.contains(tag)) {
//				event.getChannel().sendMessage("You already have that tag added").queue();
//				return;
//			}
//
//			discordUser.tags.add(tag);
//			discordUser.save();
//			event.getChannel().sendMessage("Added the tag: `" + (name != null ? name : tag) + "`").queue();
//		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
//			if(args.size() < 2) {
//				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify remove <tag>`").queue();
//				return;
//			}
//
//			String tag = args.get(1).toLowerCase();
//			String name = null;
//			for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
//				if(!player.name.equalsIgnoreCase(tag)) continue;
//				tag = player.uuid;
//				name = player.uuid;
//				break;
//			}
//			if(!discordUser.tags.contains(tag)) {
//				event.getChannel().sendMessage("You don't have that tag added").queue();
//				return;
//			}
//
//			discordUser.tags.remove(tag);
//			discordUser.save();
//			event.getChannel().sendMessage("Removed the tag: `" + (name != null ? name : tag) + "`").queue();
//		} else if(subCommand.equals("list")) {
//			String message = "TAGS (" + discordUser.tags.size() + ")";
//			for(String specialTag : specialTags) {
//				if(!discordUser.tags.contains(specialTag)) continue;
//				message += "\n> `" + specialTag + "`";
//			}
//			for(String tag : discordUser.tags) {
//				if(specialTags.contains(tag)) continue;
//				boolean foundPlayer = false;
//				for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
//					if(!player.uuid.equals(tag)) continue;
//					message += "\n> `" + player.name + "`";
//					foundPlayer = true;
//					break;
//				}
//				if(foundPlayer) continue;
//				message += "\n> `" + tag + "`";
//			}
//			event.getChannel().sendMessage(message).queue();
//		} else if(subCommand.equals("clear")) {
//			discordUser.tags.clear();
//			discordUser.save();
//			event.getChannel().sendMessage("Removed all tags").queue();
//		} else {
//			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify <add/remove/clear/list>`").queue();
//		}
	}
}
