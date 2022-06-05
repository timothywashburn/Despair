package dev.kyro.despair.commands.admin;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class EcoCommand extends DiscordCommand {
	public EcoCommand() {
		super("eco");
		adminCommand = true;
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {

		if(args.size() < 1) {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "eco <give|take|set> <player> <amount>`").queue();
			return;
		}

		String subCommand = args.get(0).toLowerCase();
		if(subCommand.equals("give")) {
			if(args.size() < 3) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "eco give <player> <amount>`").queue();
				return;
			}

			Member target;
			try {
				target = Misc.getMember(args.get(1));
			} catch(Exception ignored) {
				event.getChannel().sendMessage("Couldn't find that member").queue();
				return;
			}
			if(target.getUser().isBot()) {
				event.getChannel().sendMessage("You cannot give credits to a bot").queue();
				return;
			}

			int amount;
			try {
				amount = Integer.parseInt(args.get(2));
				if(amount < 0) throw new Exception();
			} catch(Exception ignored) {
				event.getChannel().sendMessage("Invalid amount of credits").queue();
				return;
			}

			DespairUser despairTarget = UserManager.getUser(target);
			despairTarget.priorityBoost += amount;
			despairTarget.save(true);
			event.getChannel().sendMessage("Successfully given `" + amount + "` credit" + (amount == 1 ? "" : "s") +
					" to <@" + target.getIdLong() + ">").queue();
		} else if(subCommand.equals("take")) {
			if(args.size() < 3) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "eco take <player> <amount>`").queue();
				return;
			}

			Member target;
			try {
				target = Misc.getMember(args.get(1));
			} catch(Exception ignored) {
				event.getChannel().sendMessage("Couldn't find that member").queue();
				return;
			}
			if(target.getUser().isBot()) {
				event.getChannel().sendMessage("You cannot take credits from a bot").queue();
				return;
			}

			int amount;
			try {
				amount = Integer.parseInt(args.get(2));
				if(amount < 0) throw new Exception();
			} catch(Exception ignored) {
				event.getChannel().sendMessage("Invalid amount of credits").queue();
				return;
			}

			DespairUser despairTarget = UserManager.getUser(target);
			if(despairTarget.priorityBoost < amount) {
				event.getChannel().sendMessage("<@" + target.getIdLong() + "> only has `" +
						despairTarget.priorityBoost + "` credit" + (amount == 1 ? "" : "s")).queue();
				return;
			}

			despairTarget.priorityBoost -= amount;
			despairTarget.save(true);
			event.getChannel().sendMessage("Successfully taken `" + amount + "` credit" + (amount == 1 ? "" : "s") +
					" from <@" + target.getIdLong() + ">").queue();
		} else if(subCommand.equals("set")) {
			if(args.size() < 3) {
				event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "eco set <player> <amount>`").queue();
				return;
			}

			Member target;
			try {
				target = Misc.getMember(args.get(1));
			} catch(Exception ignored) {
				event.getChannel().sendMessage("Couldn't find that member").queue();
				return;
			}
			if(target.getUser().isBot()) {
				event.getChannel().sendMessage("You cannot set the credits of a bot").queue();
				return;
			}

			int amount;
			try {
				amount = Integer.parseInt(args.get(2));
				if(amount < 0) throw new Exception();
			} catch(Exception ignored) {
				event.getChannel().sendMessage("Invalid amount of credits").queue();
				return;
			}

			DespairUser despairTarget = UserManager.getUser(target);
			despairTarget.priorityBoost = amount;
			despairTarget.save(true);
			event.getChannel().sendMessage("Successfully set <@" + target.getIdLong() + ">'s credits to `" + amount + "`").queue();
		} else {
			event.getChannel().sendMessage("Usage: `" + Config.INSTANCE.PREFIX + "eco <give|take|set> <player> <amount>`").queue();
		}
	}
}
