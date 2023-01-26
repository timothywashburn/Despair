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
		guild.createCategory("KOS").queue(category -> {
			category.upsertPermissionOverride(guild.getPublicRole()).setDenied(Permission.VIEW_CHANNEL).queue(override1 -> {
				if(trialRole != null) {
					category.upsertPermissionOverride(trialRole).setAllowed(Permission.VIEW_CHANNEL).queue(override2 -> {
						if(memberRole != null) {
							category.upsertPermissionOverride(memberRole).setAllowed(Permission.VIEW_CHANNEL).queue(override3 ->
									createChannels(guild, category));
						} else createChannels(guild, category);
					});
				} else {
					if(memberRole != null) {
						category.upsertPermissionOverride(memberRole).setAllowed(Permission.VIEW_CHANNEL).queue(override3 ->
								createChannels(guild, category));
					} else createChannels(guild, category);
				}
			});
		});
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
