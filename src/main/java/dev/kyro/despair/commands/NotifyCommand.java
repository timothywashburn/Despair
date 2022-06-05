package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.controllers.objects.KOS;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class NotifyCommand extends DiscordCommand {
	public NotifyCommand() {
		super("notify", "ping", "pings", "tag", "tags");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		DespairUser despairUser = UserManager.getUser(event.getMember());
		if(event.getChannel().getIdLong() != despairUser.kosChannelID) {
			event.getChannel().sendMessage("This command can only be used in your channel (<#" + despairUser.kosChannelID + ">)").queue();
			return;
		}

		if(args.isEmpty()) {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify <add/remove/list>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("add")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify add <tag>`").queue();
				return;
			}

			String tag = args.get(1);
			String uuid = null;
			boolean foundPlayer = false;
			for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
				if(!despairUser.kosList.contains(player.uuid)) continue;
				if(!player.name.equalsIgnoreCase(tag)) continue;
				foundPlayer = true;
				tag = player.name;
				uuid = player.uuid;
				break;
			}
			if(despairUser.tags.contains(tag)) {
				event.getChannel().sendMessage("You already have that tag added").queue();
				return;
			}
			if(!foundPlayer) {
				event.getChannel().sendMessage("That player is not on your KOS list").queue();
				return;
			}

			despairUser.tags.add(uuid);
			despairUser.save(true);
			event.getChannel().sendMessage("Added the tag: `" + tag + "`").queue();
		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify remove <tag>`").queue();
				return;
			}

			String tag = args.get(1);
			String uuid = null;
			for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
				if(!despairUser.kosList.contains(player.uuid)) continue;
				if(!player.name.equalsIgnoreCase(tag)) continue;
				tag = player.name;
				uuid = player.uuid;
				break;
			}
			if(!despairUser.tags.contains(uuid)) {
				event.getChannel().sendMessage("You don't have that tag added").queue();
				return;
			}

			despairUser.tags.remove(uuid);
			despairUser.save(true);
			event.getChannel().sendMessage("Removed the tag: `" + tag + "`").queue();
		} else if(subCommand.equals("list")) {
			String message = "TAGS (" + despairUser.tags.size() + ")";
			for(String tag : despairUser.tags) {
				for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
					if(!player.uuid.equals(tag)) continue;
					message += "\n> `" + player.name + "`";
				}
			}
			event.getChannel().sendMessage(message).queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify <add/remove/list>`").queue();
		}
	}
}
