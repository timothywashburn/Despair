package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Objects;

public class NotifyCommand extends DiscordCommand {
	public NotifyCommand() {
		super("notify", "pingme", "tag", "tags");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		boolean isAdmin = Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) || event.getMember().isOwner();
		for(Role role : event.getMember().getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.MEMBER_ROLE_ID) continue;
			isAdmin = true;
			break;
		}
		if(!isAdmin) {
			event.getChannel().sendMessage("You need to have member access to do this").queue();
			return;
		}

		if(args.isEmpty()) {

			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify <add/remove/list>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		Users.DiscordUser discordUser = Users.INSTANCE.getUser(event.getAuthor().getIdLong());
		if(subCommand.equals("add")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos add <tag>`").queue();
				return;
			}

			String tag = args.get(1).toLowerCase();
			if(discordUser.tags.contains(tag)) {
				event.getChannel().sendMessage("You already have that tag added").queue();
				return;
			}

			discordUser.tags.add(tag);
			discordUser.save();
			event.getChannel().sendMessage("Added the tag: `" + tag + "`").queue();
		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos remove <tag>`").queue();
				return;
			}

			String tag = args.get(1).toLowerCase();
			if(!discordUser.tags.contains(tag)) {
				event.getChannel().sendMessage("You don't have that tag added").queue();
				return;
			}

			discordUser.tags.remove(tag);
			discordUser.save();
			event.getChannel().sendMessage("Removed the tag: `" + tag + "`").queue();
		} else if(subCommand.equals("list")) {
			String message = "TAGS (" + discordUser.tags.size() + ")";
			for(String tag : discordUser.tags) message += "\n> `" + tag + "`";
			event.getChannel().sendMessage(message).queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "notify <add/remove/list>`").queue();
		}
	}
}
