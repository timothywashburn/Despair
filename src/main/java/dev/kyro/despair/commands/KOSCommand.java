package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.*;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class KOSCommand extends DiscordCommand {
	public KOSCommand() {
		super("kos", "add", "remove", "list");
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

			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos <add/remove/list>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("add")) {
			if(KOS.INSTANCE.kosList.size() >= PlayerTracker.getMaxPlayers()) {
				event.getChannel().sendMessage("Max amount of players reached").queue();
				return;
			}
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos add <uuid/name>`").queue();
				return;
			}
			String playerIdentifier = args.get(1);
			JSONObject requestData; HypixelPlayer hypixelPlayer;
			try {
				if(Misc.isUUID(playerIdentifier)) {
					requestData = HypixelAPIManager.request(UUID.fromString(playerIdentifier));
				} else {
					requestData = HypixelAPIManager.request(playerIdentifier);
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

			if(KOS.INSTANCE.containsPlayer(hypixelPlayer.UUID)) {
				event.getChannel().sendMessage(hypixelPlayer.name + " is already on the kos").queue();
				return;
			}

			KOS.KOSPlayer kosPlayer = new KOS.KOSPlayer(hypixelPlayer.name, hypixelPlayer.UUID.toString());
			kosPlayer.hypixelPlayer = hypixelPlayer;
			KOS.INSTANCE.addPlayer(kosPlayer, true);
			event.getChannel().sendMessage("Added player: " + hypixelPlayer.name).queue();

		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos remove <uuid/name>`").queue();
				return;
			}
			String playerIdentifier = args.get(1);
			KOS.KOSPlayer removePlayer = null;
			for(KOS.KOSPlayer kosPlayer : KOS.INSTANCE.kosList) {
				if(!kosPlayer.uuid.equals(playerIdentifier) && !kosPlayer.name.equalsIgnoreCase(playerIdentifier)) continue;
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

			KOS.INSTANCE.removePlayer(removePlayer, true);
			event.getChannel().sendMessage("Removed player: " + removePlayer.name).queue();

		} else if(subCommand.equals("list")) {
			String message = "KOS PLAYERS";
			for(KOS.KOSPlayer kosPlayer : KOS.INSTANCE.kosList) {
				message += "\n> " + (kosPlayer.name != null ? kosPlayer.name : kosPlayer.uuid);
			}
			event.getChannel().sendMessage(message).queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos <add/remove/list>`").queue();
		}
	}
}
