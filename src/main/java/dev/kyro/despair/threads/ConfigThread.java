package dev.kyro.despair.threads;

import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.enums.Configurable;
import dev.kyro.despair.misc.Misc;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConfigThread extends Thread {

	public TextChannel channel;
	public User author;
	public Configurable configurable;

	public ConfigThread(TextChannel channel, User author) {

		this.channel = channel;
		this.author = author;
	}

	@Override
	public void run() {
		promptSettings1();
	}

	public void promptSettings1() {

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("Settings")
				.setDescription("React to the corresponding number and input your new value")
				.setColor(new Color(255, 255, 255))
				.setAuthor("Kyro#2820", "https://www.youtube.com/channel/UCNI4sS-0dFyTsANtz_eFXHA",
						"https://cdn.discordapp.com/avatars/458458767634464792/5b6d2457fb627712063e1edf0d82eb71.png");

		for(int i = 0; i < Configurable.values().length; i++) {
			Configurable configurable = Configurable.values()[i];
			String currentValue = Config.INSTANCE.get(configurable);
			embedBuilder.addField((i + 1) + ". " + configurable.displayName, currentValue.equals("0") ? "None" : currentValue, true);
		}

		channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
			List<String> reactions = new ArrayList<>();
			for(int i = 0; i < Configurable.values().length; i++) {
				message.addReaction(Misc.getUnicodeNumber(i + 1)).queue();
				reactions.add(Misc.getUnicodeNumber(i + 1));
			}
//			message.addReaction("\u27a1").queue();
			DiscordManager.WAITER.waitForEvent(MessageReactionAddEvent.class,
					response -> response.getUserIdLong() == author.getIdLong() && response.getMessageIdLong() == message.getIdLong()
							&& reactions.contains(response.getReactionEmote().getName()),
					response -> {
//						if(response.getReactionEmote().getName().equals("\u27a1")) {
//							promptValue();
//							return;
//						}
						int reactionNum = Integer.parseInt(response.getReactionEmote().getName().charAt(0) + "");
						configurable = Configurable.values()[reactionNum - 1];
						promptValue();
					},
					60, TimeUnit.SECONDS, () -> {
					});
		});
	}

	public void promptValue() {

		channel.sendMessage(configurable.displayName + ": " + configurable.configType.instructions).queue();
		DiscordManager.WAITER.waitForEvent(MessageReceivedEvent.class,
				response -> response.getAuthor().equals(author) && response.getChannel() == channel,
				response -> {
					if(configurable.configType == Configurable.ConfigType.SNOWFLAKE) {
						if(response.getMessage().getContentRaw().equalsIgnoreCase("none")) {
							Config.INSTANCE.set(configurable, "0");
							Config.INSTANCE.save();
							channel.sendMessage("Cleared " + configurable.displayName).queue();
							return;
						} else {
							try {
								Long.parseLong(response.getMessage().getContentRaw());
							} catch(Exception ignored) {
								channel.sendMessage("Invalid number").queue();
								promptValue();
								return;
							}
						}
					} else if(configurable.configType == Configurable.ConfigType.STRING) {
						if(response.getMessage().getContentRaw().equalsIgnoreCase("none")) {
							Config.INSTANCE.set(configurable, "");
							Config.INSTANCE.save();
							channel.sendMessage("Cleared " + configurable.displayName).queue();
							return;
						}
					}
					Config.INSTANCE.set(configurable, response.getMessage().getContentRaw());
					Config.INSTANCE.save();
					channel.sendMessage("Updated " + configurable.displayName + " to " + Config.INSTANCE.get(configurable)).queue();
				},
				60, TimeUnit.SECONDS, this::timeExpired);
	}

	public void timeExpired() {}
}
