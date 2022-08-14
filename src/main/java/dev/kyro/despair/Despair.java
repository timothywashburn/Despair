package dev.kyro.despair;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import dev.kyro.despair.commands.*;
import dev.kyro.despair.controllers.*;
import dev.kyro.despair.misc.Variables;
import dev.kyro.despair.misc.FileResourcesUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class Despair {
	public static Firestore FIRESTORE;
	public static KOS KOS;
	public static Users USERS;
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
			if(!FIRESTORE.collection(Variables.COLLECTION).document("kos").get().get().exists()) {
				KOS = new KOS();
				KOS.save();
			}
			if(!FIRESTORE.collection(Variables.COLLECTION).document("users").get().get().exists()) {
				USERS = new Users();
				USERS.save();
			}
			if(!FIRESTORE.collection(Variables.COLLECTION).document("config").get().get().exists()) {
				CONFIG = new Config();
				CONFIG.save();
			}

			KOS = FIRESTORE.collection(Variables.COLLECTION).document("kos").get().get().toObject(KOS.class);
			USERS = FIRESTORE.collection(Variables.COLLECTION).document("users").get().get().toObject(Users.class);
			CONFIG = FIRESTORE.collection(Variables.COLLECTION).document("config").get().get().toObject(Config.class);
		} catch(InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		APIKeys.init();
		APIKeys.updateAPIKeys();

		new DiscordManager().start();
		registerCommands();
	}

	public static void registerCommands() {

		DiscordManager.registerCommand(new HelpCommand());
		DiscordManager.registerCommand(new PingCommand());
		DiscordManager.registerCommand(new KOSCommand());
		DiscordManager.registerCommand(new ConfigCommand());
		DiscordManager.registerCommand(new SetupCommand());
		DiscordManager.registerCommand(new NotifyCommand());
	}
}
