package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.controllers.objects.DiscordCommand;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ViewCommand extends DiscordCommand {
	public ViewCommand() {
		super("view");
	}

	@Override
	public void execute(MessageReceivedEvent event, List<String> args) {
		DespairUser despairUser = UserManager.getUser(event.getMember());
		if(event.getChannel().getIdLong() != despairUser.kosChannelID) {
			event.getChannel().sendMessage("This command can only be used in your channel (<#" + despairUser.kosChannelID + ">)").queue();
			return;
		}

		if(despairUser.kosChannel == null) {
			event.getChannel().sendMessage("There was an error locating your private channel. <@458458767634464792>").queue();
			return;
		}

		try {
			for(ThreadChannel threadChannel : despairUser.kosChannel.getThreadChannels()) {
				if(threadChannel.getIdLong() != despairUser.kosMessageID) continue;
				threadChannel.delete().queue();
				break;
			}
			despairUser.kosChannel.deleteMessageById(despairUser.kosMessageID).queueAfter(1, TimeUnit.SECONDS, unused -> {}, throwable -> {});
		} catch(Exception ignored) {}
		despairUser.kosChannel.sendMessage(despairUser.createKOSMessage().replace(" ||@everyone||", "")).queue(message -> {
			despairUser.kosMessageID = message.getIdLong();
			despairUser.save(true);
			message.createThreadChannel("Notifications").queue();
		});
	}
}
