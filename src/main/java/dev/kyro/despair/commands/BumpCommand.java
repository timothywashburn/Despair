package dev.kyro.despair.commands;

import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.controllers.objects.KOS;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class BumpCommand extends DiscordCommand {
	public BumpCommand() {
		super("bump");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		DespairUser despairUser = UserManager.getUser(event.getMember());
		if(event.getChannel().getIdLong() != despairUser.kosChannelID) {
			event.getChannel().sendMessage("This command can only be used in your channel (<#" + despairUser.kosChannelID + ">)").queue();
			return;
		}

		if(args.size() < 2) {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "bump <player> <amount>`").queue();
			return;
		}

		KOS.KOSPlayer kosPlayer = null;
		for(KOS.KOSPlayer testPlayer : KOS.INSTANCE.kosList) {
			if(!testPlayer.name.equalsIgnoreCase(args.get(0))) continue;
			kosPlayer = testPlayer;
			break;
		}
		if(kosPlayer == null) {
			event.getChannel().sendMessage("No player found with that name").queue();
			return;
		}

		if(!despairUser.kosList.contains(kosPlayer.uuid)) {
			event.getChannel().sendMessage("That player is not on your KOS list").queue();
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(args.get(1));
			if(amount < 0 && !Despair.isAdmin(event.getMember())) throw new Exception();
		} catch(Exception ignored) {
			event.getChannel().sendMessage("Invalid amount of credits").queue();
			return;
		}
		if(amount > despairUser.priorityBoost) {
			event.getChannel().sendMessage("You only have `" + despairUser.priorityBoost + "` credit" +
					(despairUser.priorityBoost == 1 ? "" : "s")).queue();
			return;
		}

		despairUser.priorityBoost -= amount;
		despairUser.save(true);

		kosPlayer.priority += amount;
		KOS.INSTANCE.save();

		event.getChannel().sendMessage("Increased the priority of `" + kosPlayer.name + "` by `" + amount + "`").queue();
	}
}
