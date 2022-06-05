package dev.kyro.despair.controllers.objects;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.despair.Despair;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.controllers.PlayerTracker;
import dev.kyro.despair.controllers.UserManager;
import dev.kyro.despair.misc.Constants;
import dev.kyro.despair.threads.KOSChannelThread;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DespairUser {
	@Exclude
	public static int MAX_CREDITS = 20;

	@Exclude
	public long discordId;

	@Exclude
	public boolean onSaveCooldown = false;
	@Exclude
	public boolean saveQueued = false;

	public long kosChannelID = 0;
	public long kosMessageID = 0;
	public int priorityBoost = MAX_CREDITS;
	public List<String> kosList = new ArrayList<>();
	public List<String> tags = new ArrayList<>();

	@Exclude
	public Member member;
	@Exclude
	public TextChannel kosChannel;

	public DespairUser() {
	}

	//	This user is new to the discord and is just having a user generated for them for the first time
	public DespairUser(Member member) {
		this.discordId = member.getIdLong();

		this.member = member;
		createKOSChannel();
		save(true);
	}

	public DespairUser(long kosChannelID, long kosMessageID, int priorityBoost, List<String> kosList, List<String> tags) {
		this.kosChannelID = kosChannelID;
		this.kosMessageID = kosMessageID;
		this.priorityBoost = priorityBoost;
		this.kosList = kosList;
		this.tags = tags;
	}

	public void init() {
		boolean remove = false;
		try {
			member = DiscordManager.getGuild().retrieveMemberById(discordId).complete();
		} catch(ErrorResponseException ignored) {
			remove = true;
		}

		kosChannel = DiscordManager.getGuild().getTextChannelById(kosChannelID);
		if(remove) {
			remove();
			return;
		}
		if(kosChannel == null) {
			createKOSChannel();
		}
	}

	public String createKOSMessage() {
		String display = "DESPAIR KOS BOT ||@everyone||";
		String online = "\n\nONLINE";
		String offline = "\n\nOFFLINE";
		String untracked = "";

		int kosSize = 0;
		int onlinePlayerCount = 0;
		int offlinePlayerCount = 0;
		int untrackedPlayerCount = 0;

		List<KOS.KOSPlayer> kosList = new ArrayList<>();
		List<KOS.KOSPlayer> sortedKOS = PlayerTracker.getSortedKOS();
		for(int i = 0; i < sortedKOS.size(); i++) {
			KOS.KOSPlayer player = sortedKOS.get(i);
			if(!this.kosList.contains(player.hypixelPlayer.UUID.toString())) continue;
			kosSize++;
			if(i < PlayerTracker.getMaxPlayers()) {
				kosList.add(player);
			} else {
				untrackedPlayerCount++;
				untracked += "\n> `" + player.name + "`";
			}
		}

		for(KOS.KOSPlayer player : kosList) {
			if(player.hypixelPlayer.isOnline || player.hypixelPlayer.isOnlineWithApiDisabled()) onlinePlayerCount++;
			else offlinePlayerCount++;
		}
		online += " (" + onlinePlayerCount + "/" + kosSize + ")";
		offline += " (" + offlinePlayerCount + "/" + kosSize + ")";
		DecimalFormat decimalFormat = new DecimalFormat("0.##");
		untracked = "\n\nUNTRACKED (" + untrackedPlayerCount + "/" + kosSize + ")" + " - Priority > " +
				decimalFormat.format(sortedKOS.get(Math.min(sortedKOS.size() - 1, PlayerTracker.getMaxPlayers())).priority) + " needed" + untracked;

		for(KOS.KOSPlayer player : kosList) {
			if(player.hypixelPlayer.lastLogin == 0) continue;
			if(player.hypixelPlayer.isOnline) {
				online += "\n> `" + player.name + "` - `" + player.hypixelPlayer.megastreak + "` [" + player.hypixelPlayer.getRecentKills() + "]";
			} else if(player.hypixelPlayer.isOnlineWithApiDisabled()) {
				online += "\n> *`" + player.name + "` - `" + player.hypixelPlayer.megastreak + "` [" + player.hypixelPlayer.getRecentKills() + "]";
			} else {
				offline += "\n> ";
				if(player.hypixelPlayer.apiDisabled) offline += "*";
				offline += "`" + player.name + "` - " + player.hypixelPlayer.getTimeOffline();
			}
		}

		display += online;
		display += offline;
		display += untracked;

		if(untrackedPlayerCount != 0) {
			display += "\n*To track an untracked player, use `" + Config.INSTANCE.PREFIX + "bump`";
		}

		String pattern = "HH:mm:ss";
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		display += "\n\n" + dateFormat.format(new Date().getTime() + 3 * 60 * 60 * 1000) + " EST";
		return display;
	}

	public void createKOSChannel() {
		DiscordManager.getGuild().createTextChannel(member.getUser().getName(), DiscordManager.getGuild().getCategoryById(Config.INSTANCE.KOS_CATEGORY_ID)).queue(channel ->
				new KOSChannelThread(channel, member, this).start());
	}

	public void remove() {
		UserManager.deleteUser(this);
	}

	public void save(boolean block) {
		if(onSaveCooldown && !saveQueued) {
			saveQueued = true;
			new Thread(() -> {
				try {
					Thread.sleep(1500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				saveQueued = false;
				save(block);
			}).start();
		}
		if(!saveQueued && !onSaveCooldown) {
			if(block) {
				try {
					Despair.FIRESTORE.collection(Constants.COLLECTION + "-users").document(discordId + "").set(this).get();
				} catch(InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			} else Despair.FIRESTORE.collection(Constants.COLLECTION + "-users").document(discordId + "").set(this);

			onSaveCooldown = true;
			new Thread(() -> {
				try {
					Thread.sleep(1500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				onSaveCooldown = false;
			}).start();
		}
	}
}
