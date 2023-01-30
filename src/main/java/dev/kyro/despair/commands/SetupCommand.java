package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.controllers.DisplayManager;
import dev.kyro.despair.enums.Configurable;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.firestore.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

public class SetupCommand extends DiscordCommand {

	public SetupCommand() {
		super("setup");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "sets up the channels and messages for the bot");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
			event.reply("You need to have administrator access to do this").setEphemeral(true).queue();
			return;
		}

		event.reply("Setting up bot... Please stand by").setEphemeral(true).queue();
		Guild guild = event.getGuild();
		Config.INSTANCE.set(Configurable.GUILD_ID, guild.getId());

		Role trialRole = guild.getRoleById(Config.INSTANCE.TRIAL_ROLE_ID);
		Role memberRole = guild.getRoleById(Config.INSTANCE.MEMBER_ROLE_ID);
		List<Role> roles = new ArrayList<>();
		if(trialRole != null) roles.add(trialRole);
		if(memberRole != null) roles.add(memberRole);

		Category category = guild.createCategory("KOS").complete();
		category.upsertPermissionOverride(guild.getPublicRole()).setDenied(Permission.VIEW_CHANNEL).complete();
		for(Role role : roles) {
			category.upsertPermissionOverride(role).setAllowed(Permission.VIEW_CHANNEL).complete();
			category.upsertPermissionOverride(role).setDenied(Permission.MESSAGE_SEND).complete();
		}
		createChannels(guild, category);
	}

	public void createChannels(Guild guild, Category category) {
		guild.createTextChannel("kos-list", category).queue(displayChannel -> {
			Config.INSTANCE.set(Configurable.KOS_DISPLAY_CHANNEL_ID, displayChannel.getId());
			displayChannel.sendMessage(DisplayManager.createKOSMessage().replace(" ||@everyone||", "")).queue(displayMessage -> {
				Config.INSTANCE.set(Configurable.KOS_DISPLAY_MESSAGE_ID, displayMessage.getId());
				Config.INSTANCE.save();
				displayMessage.createThreadChannel("Notifications").queue();
			});
		});
		guild.createTextChannel("truce-list", category).queue(displayChannel -> {
			Config.INSTANCE.set(Configurable.TRUCE_DISPLAY_CHANNEL_ID, displayChannel.getId());
			displayChannel.sendMessage(DisplayManager.createTruceMessage().replace(" ||@everyone||", "")).queue(displayMessage -> {
				Config.INSTANCE.set(Configurable.TRUCE_DISPLAY_MESSAGE_ID, displayMessage.getId());
				Config.INSTANCE.save();
				displayMessage.createThreadChannel("Notifications").queue();
			});
		});
	}
}
