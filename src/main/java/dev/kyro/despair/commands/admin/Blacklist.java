package dev.kyro.despair.commands.admin;

import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.controllers.objects.KOS;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.UUID;

public class Blacklist extends DiscordCommand {
	public Blacklist() {
		super("blacklist");
		adminCommand = true;
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		if(args.isEmpty()) {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "blacklist <add/remove/list>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("add")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "blacklist add <uuid>`").queue();
				return;
			}

			UUID uuid;
			try {
				uuid = UUID.fromString(args.get(1));
			} catch(Exception ignored) {
				event.getChannel().sendMessage("That is not a valid uuid").queue();
				return;
			}

			if(KOS.INSTANCE.blacklist.contains(uuid.toString())) {
				event.getChannel().sendMessage("That uuid is already on the blacklist").queue();
				return;
			}

			KOS.INSTANCE.blacklist.add(uuid.toString());
			KOS.INSTANCE.save();
			event.getChannel().sendMessage("Added uuid: " + uuid).queue();
		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "blacklist remove <uuid>`").queue();
				return;
			}

			UUID uuid;
			try {
				uuid = UUID.fromString(args.get(1));
			} catch(Exception ignored) {
				event.getChannel().sendMessage("That is not a valid uuid").queue();
				return;
			}

			if(!KOS.INSTANCE.blacklist.contains(uuid.toString())) {
				event.getChannel().sendMessage("That uuid is not on the blacklist").queue();
				return;
			}

			KOS.INSTANCE.blacklist.add(uuid.toString());
			KOS.INSTANCE.save();
			event.getChannel().sendMessage("Removed uuid: " + uuid).queue();
		} else if(subCommand.equals("list")) {
			String message = "BLACKLISTED UUIDS (" + KOS.INSTANCE.blacklist.size() + ")";
			for(String uuid : KOS.INSTANCE.blacklist) {
				message += "\n> `" + uuid + "`";
			}
			event.getChannel().sendMessage(message).queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "blacklist <add/remove/list>`").queue();
		}
	}
}
