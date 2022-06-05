package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.HypixelAPIManager;
import dev.kyro.despair.controllers.HypixelPlayer;
import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.controllers.objects.KOS;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class KOSCommand extends DiscordCommand {
	public KOSCommand() {
		super("kos", "add", "remove", "list");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		DespairUser despairUser = UserManager.getUser(event.getMember());
		if(event.getChannel().getIdLong() != despairUser.kosChannelID) {
			event.getChannel().sendMessage("This command can only be used in your channel (<#" + despairUser.kosChannelID + ">)").queue();
			return;
		}

		if(args.isEmpty()) {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos <add/remove/list>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("add")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos add <uuid/name>`").queue();
				return;
			}
			String playerIdentifier = args.get(1);
			JSONObject requestData = null;
			HypixelPlayer hypixelPlayer;
			try {
				if(Misc.isUUID(playerIdentifier)) {
					requestData = HypixelAPIManager.request(UUID.fromString(playerIdentifier));
				} else {
					if(KOS.INSTANCE.containsPlayer(playerIdentifier)) {
						for(KOS.KOSPlayer player : KOS.INSTANCE.kosList) {
							if(!player.name.equalsIgnoreCase(playerIdentifier)) return;
							requestData = HypixelAPIManager.request(UUID.fromString(player.uuid));
							break;
						}
						assert requestData != null;
					} else {
						requestData = HypixelAPIManager.request(playerIdentifier);
					}
				}
			} catch(Exception exception) {
				if(exception instanceof NoAPIKeyException) {
					event.getChannel().sendMessage("No api key set").queue();
				} else if(exception instanceof InvalidAPIKeyException) {
					event.getChannel().sendMessage("Invalid api key").queue();
				} else if(exception instanceof LookedUpNameRecentlyException) {
					event.getChannel().sendMessage("That name was already looked up recently. Use the player's uuid instead or wait a minute").queue();
				}
				return;
			}
			hypixelPlayer = new HypixelPlayer(requestData);

			if(despairUser.kosList.contains(hypixelPlayer.UUID.toString())) {
				event.getChannel().sendMessage(hypixelPlayer.name + " is already on your kos").queue();
				return;
			}
			despairUser.kosList.add(hypixelPlayer.UUID.toString());
			despairUser.save(true);

			if(!KOS.INSTANCE.containsPlayer(hypixelPlayer.UUID)) {
				KOS.KOSPlayer kosPlayer = new KOS.KOSPlayer(hypixelPlayer.name, hypixelPlayer.UUID.toString());
				kosPlayer.hypixelPlayer = hypixelPlayer;
				KOS.INSTANCE.addPlayer(kosPlayer, true);
			}

			event.getChannel().sendMessage("Added player: " + hypixelPlayer.name).queue();

		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos remove <uuid/name>`").queue();
				return;
			}
			String playerIdentifier = args.get(1);
			KOS.KOSPlayer removePlayer = null;
			for(KOS.KOSPlayer kosPlayer : KOS.INSTANCE.kosList) {
				if(!kosPlayer.uuid.equals(playerIdentifier) && !kosPlayer.name.equalsIgnoreCase(playerIdentifier))
					continue;
				removePlayer = kosPlayer;
				break;
			}
			if(removePlayer == null) {
				if(Misc.isUUID(playerIdentifier)) {
					event.getChannel().sendMessage("Couldn't find that player").queue();
				} else {
					event.getChannel().sendMessage("Couldn't find that player (They may have changed their name; try removing them with their uuid)").queue();
				}
				return;
			}

			if(!despairUser.kosList.contains(removePlayer.uuid)) {
				event.getChannel().sendMessage("That player is not on your kos").queue();
				return;
			}
			despairUser.kosList.remove(removePlayer.uuid);
			despairUser.save(true);

			boolean shouldRemove = true;
			for(DespairUser user : UserManager.users) {
				if(!user.kosList.contains(removePlayer.uuid)) continue;
				shouldRemove = false;
				break;
			}
			if(shouldRemove) {
				KOS.INSTANCE.kosList.remove(removePlayer);
				KOS.INSTANCE.save();
			}

			event.getChannel().sendMessage("Removed player: " + removePlayer.name).queue();

		} else if(subCommand.equals("list")) {
			String message = "KOS PLAYERS (" + despairUser.kosList.size() + ")";
			for(KOS.KOSPlayer kosPlayer : KOS.INSTANCE.kosList) {
				if(!despairUser.kosList.contains(kosPlayer.uuid)) continue;
				message += "\n> `" + (kosPlayer.name != null ? kosPlayer.name : kosPlayer.uuid) + "` - [" + kosPlayer.priority + "]";
			}
			event.getChannel().sendMessage(message).queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos <add/remove/list>`").queue();
		}
	}
}
