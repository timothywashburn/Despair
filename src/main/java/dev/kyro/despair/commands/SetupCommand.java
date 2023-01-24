package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.Config;
import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.KOSDisplay;
import dev.kyro.despair.enums.Configurable;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SetupCommand extends DiscordCommand {
	public static Map<Long, Long> confirmMap = new HashMap<>();

	public SetupCommand() {
		super("setup");
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

		String subCommand = args.isEmpty() ? "" : args.get(0).toLowerCase();
		if(!subCommand.equals("confirm")) {
			confirmMap.put(event.getMember().getIdLong(), System.currentTimeMillis());
			event.getChannel().sendMessage("Run `" + Config.INSTANCE.PREFIX + "setup confirm` to initiate the setup").queue();
			return;
		}

		if(confirmMap.getOrDefault(event.getMember().getIdLong(), 0L) + 20_000 < System.currentTimeMillis()) {
			event.getChannel().sendMessage("Please run `" + Config.INSTANCE.PREFIX + "setup` before confirming").queue();
			return;
		}
		confirmMap.remove(event.getMember().getIdLong());

		event.getChannel().sendMessage("Setting up bot... Please stand by").queue();
		Guild guild = event.getGuild();
		Config.INSTANCE.set(Configurable.GUILD_ID, guild.getId());
		guild.createCategory("KOS").queue(category -> {
			guild.createTextChannel("kos-display", category).queue(displayChannel -> {
				Config.INSTANCE.set(Configurable.DISPLAY_CHANNEL_ID, displayChannel.getId());
				displayChannel.sendMessage(KOSDisplay.createKOSMessage().replace(" ||@everyone||", "")).queue(displayMessage -> {
					Config.INSTANCE.set(Configurable.DISPLAY_MESSAGE_ID, displayMessage.getId());
					Config.INSTANCE.save();
					displayMessage.createThreadChannel("Notifications").queue();
				});
			});
		});
	}
}
