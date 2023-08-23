package dev.kyro.despair.controllers;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.kyro.despair.commands.*;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.firestore.Users;
import dev.kyro.despair.misc.Variables;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordManager extends Thread implements EventListener {

	public static JDABuilder BUILDER;
	public static JDA JDA;
	public static List<DiscordCommand> commands = new ArrayList<>();
	public static EventWaiter WAITER;

	@Override
	public void run() {

		BUILDER = JDABuilder.createDefault(Variables.DISCORD_BOT_TOKEN);
		WAITER = new EventWaiter();
		try {
			BUILDER.setMemberCachePolicy(MemberCachePolicy.ALL);
			BUILDER.enableIntents(GatewayIntent.GUILD_MEMBERS);
			BUILDER.addEventListeners(this);
			BUILDER.addEventListeners(WAITER);
			JDA = BUILDER.build();
			JDA.awaitReady();
		} catch(LoginException | InterruptedException exception) {
			exception.printStackTrace();
		}
		System.out.println("Discord bot enabled...");

		new PlayerTracker().start();
		new PureTracker().start();
		new DisplayManager().start();

		Role trialRole = DiscordManager.getMainGuild().getRoleById(Config.INSTANCE.TRIAL_ROLE_ID);
		Role memberRole = DiscordManager.getMainGuild().getRoleById(Config.INSTANCE.MEMBER_ROLE_ID);
		for(Member member : getMainGuild().loadMembers().get()) {
			if(member.getRoles().contains(trialRole) || member.getRoles().contains(memberRole)) Users.INSTANCE.getUser(member.getIdLong());
		}

//		getMainGuild().retrieveCommands().queue(currentCommands -> {
//			for(Command currentCommand : currentCommands) getMainGuild().deleteCommandById(currentCommand.getId()).queue();
//		});

		registerCommands();
		setupSlashCommands();
	}

	public static void registerCommands() {
		registerCommand(new TestCommand());
		registerCommand(new HelpCommand());
		registerCommand(new PingCommand());
		registerCommand(new KOSCommand());
		registerCommand(new TruceCommand());
		registerCommand(new ConfigCommand());
		registerCommand(new SetupCommand());
		registerCommand(new NotifyCommand());
		registerCommand(new PointsCommand());
	}

	public static void setupSlashCommands() {
		for(DiscordCommand command : commands) getMainGuild().upsertCommand(command.getCommandStructure()).queue();
	}

	public static Guild getMainGuild() {
		return JDA.getGuildById(Config.INSTANCE.GUILD_ID);
	}

	public static void registerCommand(DiscordCommand command) {
		commands.add(command);
	}

	public static boolean hasPermission(Member member, PermissionLevel permissionLevel) {
		if(member == null) return false;
		if(member.hasPermission(Permission.ADMINISTRATOR) || member.isOwner() || member.getIdLong() == 458458767634464792L) return true;
		List<Role> memberRoles = member.getRoles();

		if(permissionLevel == PermissionLevel.MEMBER) {
			Role trialRole = getMainGuild().getRoleById(Config.INSTANCE.TRIAL_ROLE_ID);
			Role memberRole = getMainGuild().getRoleById(Config.INSTANCE.MEMBER_ROLE_ID);
			if(trialRole != null && memberRoles.contains(trialRole)) return true;
			if(memberRole != null && memberRoles.contains(memberRole)) return true;
		}

		Role adminRole = getMainGuild().getRoleById(Config.INSTANCE.ADMIN_ROLE_ID);
		return adminRole != null && memberRoles.contains(adminRole);
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if(event instanceof ReadyEvent)
			System.out.println("API is ready!");

		if(event instanceof SlashCommandInteractionEvent)
			onSlashCommand((SlashCommandInteractionEvent) event);

		if(event instanceof CommandAutoCompleteInteractionEvent)
			onAutoComplete((CommandAutoCompleteInteractionEvent) event);

		if(event instanceof MessageReceivedEvent)
			onMessageReceive((MessageReceivedEvent) event);
	}

	public void onSlashCommand(SlashCommandInteractionEvent event) {
		String command = event.getName();
		for(DiscordCommand discordCommand : commands) {
			if(!discordCommand.name.equals(command)) continue;
			discordCommand.execute(event);
			return;
		}
	}

	public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
		String command = event.getName();
		String currentOption = event.getFocusedOption().getName();
		String currentValue = event.getFocusedOption().getValue();
		for(DiscordCommand discordCommand : commands) {
			if(!discordCommand.name.equals(command)) continue;
			List<Command.Choice> choices = discordCommand.autoComplete(event, currentOption, currentValue);
			event.replyChoices(choices.stream().limit(25).collect(Collectors.toList())).queue();
			return;
		}
	}

	public void onMessageReceive(MessageReceivedEvent event) {
		if(!event.isFromGuild()) return;
	}
}
