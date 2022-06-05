package dev.kyro.despair.controllers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.objects.DespairUser;
import dev.kyro.despair.misc.Constants;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserManager {
	public static List<DespairUser> users = new ArrayList<>();

	public static void init() {

		ApiFuture<QuerySnapshot> future = Despair.FIRESTORE.collection(Constants.COLLECTION + "-users").get();
		List<QueryDocumentSnapshot> documents = null;
		try {
			documents = future.get().getDocuments();
		} catch(InterruptedException | ExecutionException exception) {
			exception.printStackTrace();
		}
		assert documents != null;

		documents.forEach(document -> {
			DespairUser user = document.toObject(DespairUser.class);
			user.discordId = Long.parseLong(document.getId());
			addUser(user);
			user.init();
		});

		for(Member member : DiscordManager.getGuild().loadMembers().get()) {
			if(member.getUser().isBot()) continue;
			UserManager.getUser(member);
		}
	}

	public static void onJoin(GuildMemberJoinEvent event) {
		if(event.getUser().isBot()) return;
		Member discordMember = event.getMember();
		UserManager.getUser(discordMember);
	}

	public static void onLeave(GuildMemberRemoveEvent event) {
		if(event.getUser().isBot()) return;
		User discordUser = event.getUser();
		for(DespairUser user : UserManager.users) {
			if(user.discordId != discordUser.getIdLong()) continue;
			user.remove();
			break;
		}
	}

	public static void addUser(DespairUser user) {
		users.add(user);
	}

	public static void deleteUser(DespairUser user) {
		if(user.kosChannel != null) user.kosChannel.delete().queue();
		Despair.FIRESTORE.collection(Constants.COLLECTION + "-users").document(user.discordId + "").delete();
		users.remove(user);
	}

	public static DespairUser getUser(Member member) {
		for(DespairUser user : users) {
			if(user.discordId == member.getIdLong()) return user;
		}
		DespairUser despairUser = new DespairUser(member);
		addUser(despairUser);
		return despairUser;
	}
}
