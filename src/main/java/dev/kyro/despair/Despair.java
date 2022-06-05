package dev.kyro.despair;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import dev.kyro.despair.commands.*;
import dev.kyro.despair.commands.admin.Blacklist;
import dev.kyro.despair.commands.admin.ConfigCommand;
import dev.kyro.despair.commands.admin.EcoCommand;
import dev.kyro.despair.commands.admin.ExitCommand;
import dev.kyro.despair.controllers.APIKeys;
import dev.kyro.despair.controllers.DiscordManager;
import dev.kyro.despair.controllers.objects.Config;
import dev.kyro.despair.controllers.objects.KOS;
import dev.kyro.despair.misc.Constants;
import dev.kyro.despair.misc.FileResourcesUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Despair {
	public static Firestore FIRESTORE;
	public static KOS KOS;
	public static Config CONFIG;

	public static long START_TIME = System.currentTimeMillis();

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);

		try {
			InputStream serviceAccount = new FileResourcesUtils().getFileFromResourceAsStream("google-key.json");
			GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(credentials)
					.build();
			FirebaseApp.initializeApp(options);

			FIRESTORE = FirestoreClient.getFirestore();
			System.out.println("Firestore enabled...");
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			if(!FIRESTORE.collection(Constants.COLLECTION).document("kos").get().get().exists()) {
				KOS = new KOS();
				KOS.save();
			}
			if(!FIRESTORE.collection(Constants.COLLECTION).document("config").get().get().exists()) {
				CONFIG = new Config();
				CONFIG.save();
			}

			KOS = FIRESTORE.collection(Constants.COLLECTION).document("kos").get().get().toObject(KOS.class);
			CONFIG = FIRESTORE.collection(Constants.COLLECTION).document("config").get().get().toObject(Config.class);
		} catch(InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		APIKeys.init();
		APIKeys.updateAPIKeys();

		new DiscordManager().start();
		registerCommands();
	}

	public static boolean isAdmin(Member member) {
		if(Objects.requireNonNull(member).hasPermission(Permission.ADMINISTRATOR) || member.isOwner()) return true;
		for(Role role : member.getRoles()) {
			if(role.getIdLong() != Config.INSTANCE.ADMIN_ROLE_ID) continue;
			return true;
		}
		return false;
	}

	public static void registerCommands() {

		DiscordManager.registerCommand(new ConfigCommand());
		DiscordManager.registerCommand(new EcoCommand());
		DiscordManager.registerCommand(new ExitCommand());
		DiscordManager.registerCommand(new Blacklist());

		DiscordManager.registerCommand(new HelpCommand());
		DiscordManager.registerCommand(new PingCommand());
		DiscordManager.registerCommand(new KOSCommand());
		DiscordManager.registerCommand(new ViewCommand());
		DiscordManager.registerCommand(new BalanceCommand());
		DiscordManager.registerCommand(new BumpCommand());
		DiscordManager.registerCommand(new NotifyCommand());
	}
}
