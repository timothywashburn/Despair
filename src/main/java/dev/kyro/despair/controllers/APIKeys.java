package dev.kyro.despair.controllers;

import dev.kyro.despair.misc.FileResourcesUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class APIKeys {

	public static File keysFile;
	public static List<String> apiKeys = new ArrayList<>();

	public static int count = 0;

	public static void init() {

		try {
			keysFile = new FileResourcesUtils().getFileFromResource("api-keys.txt");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String getAPIKey() {

		if(apiKeys.size() == 0) return null;
		return apiKeys.get(count++ % apiKeys.size());
	}

	public static void updateAPIKeys() {

		List<String> keys = new ArrayList<>();

		if(keysFile == null) {
			keys.add("2793f54d-ee43-4e4c-8d9d-cf3c8262b2bb");
//			keys.add("0a032a27-f5f2-4173-ac8d-e9e1efdf0562");
//			keys.add("2c4c2634-84cc-4abb-a46a-c5f7ef05dadc");
//			keys.add("59c73a33-8190-41a9-acc3-21bebb3d1b10");
//			keys.add("");
			apiKeys = keys;
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

		apiKeys = keys;
	}
}
