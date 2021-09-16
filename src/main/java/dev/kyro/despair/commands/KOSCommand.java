package dev.kyro.despair.commands;

import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.*;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class KOSCommand extends DiscordCommand {
	public KOSCommand() {
		super("kos", "add", "remove", "list");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		if(args.isEmpty()) {

			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos <add/remove/list>`").queue();
			return;
		}

		String subcommand = args.get(0).toLowerCase();
		if(subcommand.equals("add")) {
			if(args.size() < 2) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "kos add <uuid/name>`").queue();
				return;
			}
			String playerIdentifier = args.get(1);
			JSONObject requestData; HypixelPlayer tempHypixelPlayer;
			if(Misc.isUUID(playerIdentifier)) {
				requestData = HypixelAPIManager.request(UUID.fromString(playerIdentifier));
			} else {
				requestData = HypixelAPIManager.request(playerIdentifier);
			}
			tempHypixelPlayer = new HypixelPlayer(requestData);
			KOS.INSTANCE.addPlayer(new KOS.KOSPlayer(tempHypixelPlayer.name, tempHypixelPlayer.UUID.toString()), true);
			event.getChannel().sendMessage("Added player: " + tempHypixelPlayer.name).queue();

		} else if(subcommand.equals("remove") || subcommand.equals("delete")) {
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

		} else if(subcommand.equals("list")) {
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
