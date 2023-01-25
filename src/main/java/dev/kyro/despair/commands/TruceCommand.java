package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.HypixelAPIManager;
import dev.kyro.despair.controllers.HypixelPlayer;
import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.firestore.KOS;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TruceCommand extends DiscordCommand {
	public TruceCommand() {
		super("truce");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		boolean hasPermission = Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) || event.getMember().isOwner();
		for(Role role : event.getMember().getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.ADMIN_ROLE_ID) continue;
			hasPermission = true;
			break;
		}
		if(!hasPermission) {
			event.getChannel().sendMessage("You need to have admin access to do this").queue();
			return;
		}

		if(args.isEmpty()) {

			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce <add/extend/remove/list>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("add")) {
			if(args.size() < 4) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce add <uuid/name> <" +
						String.join("/", Config.INSTANCE.getTruceListCategories()) + "> <duration>`").queue();
				return;
			}

			String category = args.get(2).toLowerCase();
			if(!Config.INSTANCE.getTruceListCategories().contains(category)) {
				event.getChannel().sendMessage("Please input a valid category: " +
						String.join(", ", Config.INSTANCE.getTruceListCategories()) + "> <duration>").queue();
				return;
			}

			String durationString = String.join(" ", args.subList(3, args.size()));
			Duration duration;
			try {
				duration = Duration.parse(durationString);
			} catch(DateTimeParseException exception) {
				event.getChannel().sendMessage("Invalid time provided").queue();
				return;
			}

			String playerIdentifier = args.get(1);
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
					event.getChannel().sendMessage("No api key set").queue();
				} else if(exception instanceof InvalidAPIKeyException) {
					event.getChannel().sendMessage("Invalid api key").queue();
				} else if(exception instanceof LookedUpNameRecentlyException) {
					event.getChannel().sendMessage("That name was already looked up recently. Use the player's uuid instead or wait a minute").queue();
				}
				return;
			}
			hypixelPlayer = new HypixelPlayer(requestData);

			if(KOS.INSTANCE.truceContainsPlayer(hypixelPlayer.UUID)) {
				event.getChannel().sendMessage(hypixelPlayer.name + " is already on the truce list").queue();
				return;
			}

			KOS.TrucePlayer trucePlayer = new KOS.TrucePlayer(hypixelPlayer.name, hypixelPlayer.UUID.toString(), category, duration);
			trucePlayer.hypixelPlayer = hypixelPlayer;
			KOS.INSTANCE.addTrucePlayer(trucePlayer, true);
			event.getChannel().sendMessage("Added `" + hypixelPlayer.name + "` to truce for " + Misc.humanReadableFormat(duration)).queue();

		} else if(subCommand.equals("extend")) {
			if(args.size() < 3) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce extend <uuid/name> <duration>`").queue();
				return;
			}

			String playerIdentifier = args.get(1);
			KOS.TrucePlayer extendPlayer = null;
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.truceList) {
				if(!trucePlayer.uuid.equals(playerIdentifier) && !trucePlayer.name.equalsIgnoreCase(playerIdentifier))
					continue;
				extendPlayer = trucePlayer;
				break;
			}
			if(extendPlayer == null) {
				if(Misc.isUUID(playerIdentifier)) {
					event.getChannel().sendMessage("Couldn't find that player").queue();
				} else {
					event.getChannel().sendMessage("Couldn't find that player (They may have changed their name; try removing them with their uuid)").queue();
				}
				return;
			}

			String durationString = String.join(" ", args.subList(2, args.size()));
			Duration duration;
			try {
				duration = Duration.parse(durationString);
			} catch(DateTimeParseException exception) {
				event.getChannel().sendMessage("Invalid time provided").queue();
				return;
			}

			extendPlayer.extendTruce(duration);
			Config.INSTANCE.save();
			event.getChannel().sendMessage("Extended truce for `" + extendPlayer.name + "` by " + Misc.humanReadableFormat(duration)).queue();
		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce remove <uuid/name>`").queue();
				return;
			}
			String playerIdentifier = args.get(1);
			KOS.TrucePlayer removePlayer = null;
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.truceList) {
				if(!trucePlayer.uuid.equals(playerIdentifier) && !trucePlayer.name.equalsIgnoreCase(playerIdentifier))
					continue;
				removePlayer = trucePlayer;
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
			if(removePlayer.hypixelPlayer == null) {
				event.getChannel().sendMessage("Something went wrong while attempting to remove player. Please report this").queue();
				return;
			}

			KOS.INSTANCE.removeTrucePlayer(removePlayer, true);
			event.getChannel().sendMessage("Removed `" + removePlayer.name + "` from the truce list").queue();

		} else if(subCommand.equals("list")) {
			String message = "TRUCED PLAYERS (" + KOS.INSTANCE.truceList.size() + ")";
			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.truceList) {
				message += "\n> " + (trucePlayer.name != null ? trucePlayer.name : trucePlayer.uuid);
			}
			event.getChannel().sendMessage(message).queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce <add/remove/list>`").queue();
		}
	}
}