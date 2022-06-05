package dev.kyro.despair.threads;

import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.DespairUser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class KOSChannelThread extends Thread {

	public TextChannel channel;
	public Member member;
	public DespairUser despairUser;

	public KOSChannelThread(TextChannel channel, Member member, DespairUser despairUser) {
		this.channel = channel;
		this.member = member;
		this.despairUser = despairUser;
	}

	@Override
	public void run() {
		despairUser.kosChannel = channel;
		despairUser.kosChannelID = channel.getIdLong();
		despairUser.save(true);

		channel.upsertPermissionOverride(member).setAllowed(Permission.VIEW_CHANNEL).queue();
		channel.sendMessage("<@" + member.getIdLong() + "> welcome! Get started using `" + Config.INSTANCE.PREFIX + "help`").queue();
	}
}
