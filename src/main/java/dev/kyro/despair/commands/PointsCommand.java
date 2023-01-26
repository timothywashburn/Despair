package dev.kyro.despair.commands;

import dev.kyro.despair.controllers.DiscordCommand;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.enums.PermissionLevel;
import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.firestore.Users;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;
import java.util.stream.Collectors;

public class PointsCommand extends DiscordCommand {
	public PointsCommand() {
		super("points");
	}

	@Override
	public SlashCommandData getCommandStructure() {
		return Commands.slash(name, "modify & view the truce list").addSubcommands(
				new SubcommandData("balance", "check your points balance")
						.addOption(OptionType.USER, "member", "the discord user to view the balance of"),
				new SubcommandData("add", "add points to a player")
						.addOption(OptionType.USER, "member", "the discord user to add points to", true)
						.addOption(OptionType.INTEGER, "amount", "amount of points to add", true),
				new SubcommandData("cashout", "for players cashing in points")
						.addOption(OptionType.USER, "member", "the discord user cashing in points", true)
						.addOption(OptionType.INTEGER, "amount", "amount of points to add", true),
				new SubcommandData("leaderboard", "displays a leaderboard of points")
		);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.MEMBER)) {
			event.reply("You need to have member access to do this").setEphemeral(true).queue();
			return;
		}

		Role trialRole = DiscordManager.getMainGuild().getRoleById(Config.INSTANCE.TRIAL_ROLE_ID);
		Role memberRole = DiscordManager.getMainGuild().getRoleById(Config.INSTANCE.MEMBER_ROLE_ID);

		String subCommand = event.getSubcommandName();
		if(subCommand == null) {
			event.reply("Please run a sub command").queue();
			return;
		}
		if(subCommand.equals("balance")) {
			Member targetMember = event.getMember();
			if(event.getOption("member") != null) targetMember = event.getOption("member").getAsMember();
			Users.DiscordUser discordTarget = Users.INSTANCE.getUser(targetMember.getIdLong());

			if(!DiscordManager.hasPermission(targetMember, PermissionLevel.MEMBER)) {
				event.reply("The target needs to have member access to do this").queue();
				return;
			}

			if(targetMember == event.getMember()) {
				event.reply("You have `" + discordTarget.points + "` point" + (discordTarget.points == 1 ? "" : "s")).setEphemeral(true).queue();
			} else {
				event.reply("`" + targetMember.getEffectiveName() + "` has `" + discordTarget.points + "` point" +
						(discordTarget.points == 1 ? "" : "s")).setEphemeral(true).queue();
			}
		} else if(subCommand.equals("add")) {
			if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
				event.reply("You need to have administrator access to do this").setEphemeral(true).queue();
				return;
			}

			Member targetMember = event.getOption("member").getAsMember();
			Users.DiscordUser discordTarget = Users.INSTANCE.getUser(targetMember.getIdLong());

			if(!DiscordManager.hasPermission(targetMember, PermissionLevel.MEMBER)) {
				event.reply("The target needs to have member access to do this").queue();
				return;
			}

			int amount = event.getOption("amount").getAsInt();
			if(amount <= 0) {
				event.reply("Invalid amount").queue();
				return;
			}

			discordTarget.points += amount;
			discordTarget.save();
			event.reply("You gave <@" + discordTarget.id + "> `" + amount + "` point" + (amount == 1 ? "" : "s")).queue();
		} else if(subCommand.equals("cashout")) {
			if(!DiscordManager.hasPermission(event.getMember(), PermissionLevel.ADMINISTRATOR)) {
				event.reply("You need to have administrator access to do this").setEphemeral(true).queue();
				return;
			}

			Member targetMember = event.getOption("member").getAsMember();
			Users.DiscordUser discordTarget = Users.INSTANCE.getUser(targetMember.getIdLong());

			if(!DiscordManager.hasPermission(targetMember, PermissionLevel.MEMBER)) {
				event.reply("The target needs to have member access to do this").queue();
				return;
			}

			int amount = event.getOption("amount").getAsInt();
			if(amount <= 0) {
				event.reply("Invalid amount").queue();
				return;
			}

			if(amount > discordTarget.points) {
				event.reply("That discord user only has `" + discordTarget.points + "` point" + (discordTarget.points == 1 ? "" : "s")).queue();
				return;
			}

			discordTarget.points -= amount;
			discordTarget.save();
			event.reply("<@" + discordTarget.id + "> cashed out `" + amount + "` point" + (amount == 1 ? "" : "s")).queue();
		} else if(subCommand.equals("leaderboard")) {
			Users.DiscordUser discordUser = Users.INSTANCE.getUser(event.getMember().getIdLong());

			String message = "TOP POINTS";
			List<Users.DiscordUser> sortedUsers = Users.INSTANCE.getUsersWithMember();
			List<Users.DiscordUser> leaderboardUsers = sortedUsers.stream().limit(10).collect(Collectors.toList());
			for(int i = 0; i < leaderboardUsers.size(); i++) {
				Users.DiscordUser loopUser = leaderboardUsers.get(i);
				Member loopMember = DiscordManager.getMainGuild().getMemberById(loopUser.id);
				message += "\n> " + (i + 1) + ") `" + loopMember.getEffectiveName() + "` - `" + loopUser.points + " point" + (loopUser.points == 1 ? "" : "s") + "`";
			}
			if(!leaderboardUsers.contains(discordUser)) {
				message += "\n> \n> " + (sortedUsers.indexOf(discordUser) + 1) + ") `" + event.getMember().getEffectiveName() +
						"` - `" + discordUser.points + " point" + (discordUser.points == 1 ? "" : "s") + "`";
			}
			event.reply(message).queue();
		}
	}
}
