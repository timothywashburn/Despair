package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class TruceCommand extends DiscordCommand {
	public TruceCommand() {
		super("truce");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "modify & view the truce list");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
//		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
//			event.getChannel().sendMessage("You need to have administrator access to do this").queue();
//			return;
//		}
//
//		if(args.isEmpty()) {
//			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce <add/extend/remove/list>`").queue();
//			return;
//		}
//
//		String subCommand = args.get(0).toLowerCase();
//		if(subCommand.equals("add")) {
//			if(args.size() < 4) {
//				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce add <uuid/name> <" +
//						String.join("/", Config.INSTANCE.getTruceListCategories()) + "> <duration/perm>`").queue();
//				return;
//			}
//
//			String category = args.get(2).toLowerCase();
//			if(!Config.INSTANCE.getTruceListCategories().contains(category)) {
//				event.getChannel().sendMessage("Not a valid category: " +
//						String.join(", ", Config.INSTANCE.getTruceListCategories()) + "").queue();
//				return;
//			}
//
//			Duration duration;
//			if(args.get(3).equalsIgnoreCase("perm") || args.get(3).equalsIgnoreCase("permanent")) {
//				duration = null;
//			} else {
//				String durationString = String.join(" ", args.subList(3, args.size()));
//				try {
//					duration = Misc.parseDuration(durationString);
//				} catch(Exception exception) {
//					exception.printStackTrace();
//					event.getChannel().sendMessage("Invalid time provided").queue();
//					return;
//				}
//			}
//
//			String playerIdentifier = args.get(1);
//			JSONObject requestData;
//			HypixelPlayer hypixelPlayer;
//			try {
//				if(Misc.isUUID(playerIdentifier)) {
//					requestData = HypixelAPIManager.request(UUID.fromString(playerIdentifier));
//				} else {
//					requestData = HypixelAPIManager.request(playerIdentifier);
//				}
//			} catch(Exception exception) {
//				if(exception instanceof NoAPIKeyException) {
//					event.getChannel().sendMessage("No api key set").queue();
//				} else if(exception instanceof InvalidAPIKeyException) {
//					event.getChannel().sendMessage("Invalid api key").queue();
//				} else if(exception instanceof LookedUpNameRecentlyException) {
//					event.getChannel().sendMessage("That name was already looked up recently. Use the player's uuid instead or wait a minute").queue();
//				}
//				return;
//			}
//			hypixelPlayer = new HypixelPlayer(requestData);
//
//			if(KOS.INSTANCE.truceContainsPlayer(hypixelPlayer.UUID)) {
//				event.getChannel().sendMessage(hypixelPlayer.name + " is already on the truce list").queue();
//				return;
//			}
//
//			KOS.TrucePlayer trucePlayer = new KOS.TrucePlayer(hypixelPlayer.name, hypixelPlayer.UUID.toString(), category, duration);
//			trucePlayer.hypixelPlayer = hypixelPlayer;
//			KOS.INSTANCE.addTrucePlayer(trucePlayer, true);
//			if(duration == null) {
//				event.getChannel().sendMessage("Permanently added `" + hypixelPlayer.name + "` to truce").queue();
//			} else {
//				event.getChannel().sendMessage("Added `" + hypixelPlayer.name + "` to truce for " + Misc.humanReadableFormat(duration)).queue();
//			}
//
//		} else if(subCommand.equals("extend")) {
//			if(args.size() < 3) {
//				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce extend <uuid/name> <duration>`").queue();
//				return;
//			}
//
//			String playerIdentifier = args.get(1);
//			KOS.TrucePlayer extendPlayer = null;
//			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
//				if(!trucePlayer.uuid.equals(playerIdentifier) && !trucePlayer.name.equalsIgnoreCase(playerIdentifier))
//					continue;
//				extendPlayer = trucePlayer;
//				break;
//			}
//			if(extendPlayer == null) {
//				if(Misc.isUUID(playerIdentifier)) {
//					event.getChannel().sendMessage("Couldn't find that player").queue();
//				} else {
//					event.getChannel().sendMessage("Couldn't find that player (They may have changed their name; try removing them with their uuid)").queue();
//				}
//				return;
//			}
//
//			if(extendPlayer.trucedUntil == null) {
//				event.getChannel().sendMessage("That player already has a permanent truce").queue();
//				return;
//			}
//
//			Duration duration;
//			if(args.get(2).equalsIgnoreCase("perm") || args.get(2).equalsIgnoreCase("permanent")) {
//				duration = null;
//			} else {
//				String durationString = String.join(" ", args.subList(2, args.size()));
//				try {
//					duration = Misc.parseDuration(durationString);
//				} catch(Exception exception) {
//					exception.printStackTrace();
//					event.getChannel().sendMessage("Invalid time provided").queue();
//					return;
//				}
//			}
//
//			if(extendPlayer.trucedUntil.getTime() < new Date().getTime()) extendPlayer.trucedUntil = new Date();
//			extendPlayer.extendTruce(duration);
//			KOS.INSTANCE.save();
//			if(duration == null) {
//				event.getChannel().sendMessage("Changed truce for `" + extendPlayer.name + "` to be permanent").queue();
//			} else {
//				event.getChannel().sendMessage("Extended truce for `" + extendPlayer.name + "` by " + Misc.humanReadableFormat(duration)).queue();
//			}
//		} else if(subCommand.equals("remove") || subCommand.equals("delete")) {
//			if(args.size() < 2) {
//				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce remove <uuid/name>`").queue();
//				return;
//			}
//			String playerIdentifier = args.get(1);
//			KOS.TrucePlayer removePlayer = null;
//			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
//				if(!trucePlayer.uuid.equals(playerIdentifier) && !trucePlayer.name.equalsIgnoreCase(playerIdentifier))
//					continue;
//				removePlayer = trucePlayer;
//				break;
//			}
//			if(removePlayer == null) {
//				if(Misc.isUUID(playerIdentifier)) {
//					event.getChannel().sendMessage("Couldn't find that player").queue();
//				} else {
//					event.getChannel().sendMessage("Couldn't find that player (They may have changed their name; try removing them with their uuid)").queue();
//				}
//				return;
//			}
//			if(removePlayer.hypixelPlayer == null) {
//				event.getChannel().sendMessage("Something went wrong while attempting to remove player. Please report this").queue();
//				return;
//			}
//
//			KOS.INSTANCE.removeTrucePlayer(removePlayer, true);
//			event.getChannel().sendMessage("Removed `" + removePlayer.name + "` from the truce list").queue();
//
//		} else if(subCommand.equals("list")) {
//			String message = "TRUCED PLAYERS (" + KOS.INSTANCE.getTruceList().size() + ")";
//			for(KOS.TrucePlayer trucePlayer : KOS.INSTANCE.getTruceList()) {
//				message += "\n> `" + (trucePlayer.name != null ? trucePlayer.name : trucePlayer.uuid) + "` - `" + trucePlayer.getTruceStatus() + "`";
//			}
//			event.getChannel().sendMessage(message).queue();
//		} else {
//			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "truce <add/remove/list>`").queue();
//		}
	}
}
