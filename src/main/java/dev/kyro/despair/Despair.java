package dev.kyro.despair;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import dev.kyro.despair.commands.KOSCommand;
import dev.kyro.despair.commands.PingCommand;
import dev.kyro.despair.commands.SettingsCommand;
import dev.kyro.despair.controllers.*;
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
	public static Config CONFIG;

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);

		APIKeys.init();
		APIKeys.updateAPIKeys();

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

//		KOS = new KOS();
//		KOS.save();
//		CONFIG = new Config();
//		CONFIG.save();
		try {
			KOS = FIRESTORE.collection("despair").document("kos").get().get().toObject(KOS.class);
			CONFIG = FIRESTORE.collection("despair").document("config").get().get().toObject(Config.class);
		} catch(InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		new DiscordManager().start();
		registerCommands();

		new PlayerTracker().start();
	}

	public static void registerCommands() {

		DiscordManager.registerCommand(new PingCommand());
		DiscordManager.registerCommand(new KOSCommand());
		DiscordManager.registerCommand(new SettingsCommand());
	}
}
