package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.Config;
import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Objects;

public class KeyCommand extends DiscordCommand {
	public KeyCommand() {
		super("key", "api", "proxy");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		boolean isAdmin = Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) || event.getMember().isOwner();
		for(Role role : event.getMember().getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.ADMIN_ROLE_ID) continue;
			isAdmin = true;
			break;
		}
		if(!isAdmin) {
			event.getChannel().sendMessage("You need to have administrative access to do this").queue();
			return;
		}

		if(args.isEmpty()) {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "key <add/remove/list>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("add")) {
			if(Config.INSTANCE.KEY_PROXY_LIST.size() >= 3) {
				event.getChannel().sendMessage("Yeah, your not adding more than three keys that's legit absurd. Go look for your parents instead, hm?").queue();
				return;
			}

			if(args.size() < 4) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "key add <apikey> <proxy-ip:port> <proxy-username:pass>`").queue();
				return;
			}

			try {
				String key = args.get(1);
				String proxyIp = args.get(2).split(":")[0];
				int proxyPort = Integer.parseInt(args.get(2).split(":")[1]);
				String proxyUsername = args.get(3).split(":")[0];
				String proxyPassword = args.get(3).split(":")[1];

				for(Config.KeyAndProxy keyAndProxy : Config.INSTANCE.KEY_PROXY_LIST) {
					if(keyAndProxy.key.equalsIgnoreCase(key)) {
						event.getChannel().sendMessage("That key has already been added").queue();
						return;
					} else if(keyAndProxy.proxyIp.equalsIgnoreCase(proxyIp)) {
						event.getChannel().sendMessage("That proxy has already been added").queue();
						return;
					}
				}

				Config.KeyAndProxy keyAndProxy = new Config.KeyAndProxy(key, proxyIp, proxyPort, proxyUsername, proxyPassword);
				Config.INSTANCE.KEY_PROXY_LIST.add(keyAndProxy);
				Config.INSTANCE.save();

				event.getChannel().sendMessage("Added key `" + key + "` with proxy `" + proxyIp + ":" + proxyPort + "`").queue();

			} catch(Exception ignored) {
				ignored.printStackTrace();
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "key add <apikey> <proxy-ip:port> <proxy-username:pass>`").queue();
			}
		} else if(subCommand.equals("remove")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "key remove <apikey>`").queue();
				return;
			}

			String key = args.get(1);
			for(Config.KeyAndProxy keyAndProxy : Config.INSTANCE.KEY_PROXY_LIST) {
				if(!keyAndProxy.key.equalsIgnoreCase(key)) continue;
				Config.INSTANCE.KEY_PROXY_LIST.remove(keyAndProxy);
				Config.INSTANCE.save();
				event.getChannel().sendMessage("removed key `" + keyAndProxy.key + "` with proxy `" + keyAndProxy.proxyIp + ":" + keyAndProxy.proxyPort + "`").queue();
				return;
			}

			event.getChannel().sendMessage("Could not find the api key`" + key + "`").queue();
		} else if(subCommand.equals("list")) {
			if(Config.INSTANCE.KEY_PROXY_LIST.isEmpty()) {
				event.getChannel().sendMessage("No api keys. Add one with " + Config.INSTANCE.PREFIX + "key add").queue();
				return;
			}
			String message = "API KEYS";
			for(Config.KeyAndProxy keyAndProxy : Config.INSTANCE.KEY_PROXY_LIST) {
				message += "\n> `" + keyAndProxy.key + "` - `" + keyAndProxy.proxyIp + ":" + keyAndProxy.proxyPort + "`";
			}
			event.getChannel().sendMessage(message).queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "key <add/remove/list>`").queue();
		}
	}
}
