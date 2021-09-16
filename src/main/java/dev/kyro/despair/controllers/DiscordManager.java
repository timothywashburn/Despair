package dev.kyro.despair.controllers;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.kyro.despair.Despair;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscordManager extends Thread implements EventListener {

	public static JDABuilder BUILDER;
	public static JDA JDA;
	public static List<DiscordCommand> commands = new ArrayList<>();
	public static EventWaiter WAITER;

	@Override
	public void run() {

		BUILDER = JDABuilder.createDefault("ODg3ODY5Nzc3NDIxNDAyMTQz.YUKbKg.yCYT56f-oFWUpj4NAYlx64ZdzBc");
		try {
			BUILDER.setMemberCachePolicy(MemberCachePolicy.ALL);
			BUILDER.enableIntents(GatewayIntent.GUILD_MEMBERS);
			BUILDER.addEventListeners(this);
			JDA = BUILDER.build();
			JDA.awaitReady();
		} catch(LoginException | InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Discord bot enabled...");
	}

	public static void registerCommand(DiscordCommand command) {

		commands.add(command);
	}

	public void onMessageReceived(MessageReceivedEvent event) {

		Message message = event.getMessage();

		if(message.getContentRaw().matches("(?i).*?\\bdad\\b.*?") && !message.getAuthor().isBot()) {
			event.getChannel().sendMessage("unable to locate dad").queue();
		}

		if(!message.getContentRaw().startsWith(Despair.CONFIG.PREFIX)) return;

		String content = message.getContentRaw().replaceFirst(Despair.CONFIG.PREFIX, "");
		List<String> args = new ArrayList<>(Arrays.asList(content.split(" ")));
		String command = args.remove(0).toLowerCase();

		for(DiscordCommand discordCommand : commands) {

			if(!discordCommand.command.equals(command) && !discordCommand.aliases.contains(command)) continue;

			discordCommand.execute(event, args);
			return;
		}
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {

		if (event instanceof ReadyEvent)
			System.out.println("API is ready!");

		if(event instanceof MessageReceivedEvent)
			onMessageReceived((MessageReceivedEvent) event);
	}
}
