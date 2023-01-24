package dev.kyro.despair.controllers;

import dev.kyro.despair.firestore.Config;
import dev.kyro.despair.misc.FileResourcesUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class APIKeys {

	public static File keysFile;
//	public static List<String> apiKeys = new ArrayList<>();

	public static int count = 0;

	public static void init() {

		try {
			keysFile = new FileResourcesUtils().getFileFromResource("api-keys.txt");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String getAPIKey() {

//		if(apiKeys.size() == 0) return null;
//		return apiKeys.get(count++ % apiKeys.size());

		if(Config.INSTANCE.API_KEY.equals("")) return null;
		return Config.INSTANCE.API_KEY;
	}

	public static void updateAPIKeys() {

		List<String> keys = new ArrayList<>();

		if(keysFile == null) {
//			keys.add("f8bfe958-03a4-4c10-8f90-d939263c28ef");
//			keys.add("2793f54d-ee43-4e4c-8d9d-cf3c8262b2bb");
//			keys.add("2c4c2634-84cc-4abb-a46a-c5f7ef05dadc");
//			keys.add("59c73a33-8190-41a9-acc3-21bebb3d1b10");
//			apiKeys = keys;
			return;
		}

		Scanner fileReader;
		try {
			fileReader = new Scanner(keysFile);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		while(fileReader.hasNextLine()) {

			String key = fileReader.nextLine();
			keys.add(key);
		}

//		apiKeys = keys;
	}
}
