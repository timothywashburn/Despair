package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class BalanceCommand extends DiscordCommand {
	public BalanceCommand() {
		super("balance", "bal");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		DespairUser despairUser = UserManager.getUser(event.getMember());
		if(event.getChannel().getIdLong() != despairUser.kosChannelID) {
			event.getChannel().sendMessage("This command can only be used in your channel (<#" + despairUser.kosChannelID + ">)").queue();
			return;
		}

		event.getChannel().sendMessage("You have `" + despairUser.priorityBoost + "` credit" + (despairUser.priorityBoost == 1 ? "" : "s") +
				". Use `" + Config.INSTANCE.PREFIX + "bump <player> <amount>` to increase their priority on the KOS").queue();
	}
}
