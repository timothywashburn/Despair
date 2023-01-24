package dev.kyro.despair.controllers;

import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.MissingResourceException;
import java.util.UUID;

public class HypixelAPIManager {

	public static JSONObject request(String name) throws Exception {

		HttpClient client = new DefaultHttpClient();
		if(APIKeys.getAPIKey() == null) throw new NoAPIKeyException();
		HttpGet request = new HttpGet("https://api.hypixel.net/player?name=" + name + "&key=" + APIKeys.getAPIKey());
		HttpResponse response;
		String result;

		try {
			response = client.execute(request);
			HttpEntity entity = response.getEntity();
			InputStream inStream = entity.getContent();
			result = convertStreamToString(inStream);

			JSONObject playerObj = new JSONObject(result);
			if(!playerObj.getBoolean("success")) {
				if(playerObj.getString("cause").equals("You have already looked up this name recently")) throw new LookedUpNameRecentlyException();
				if(playerObj.getString("cause").equals("Invalid API key")) throw new InvalidAPIKeyException();
				return null;
			}
			return playerObj;
		} catch(Exception exception) {
			if(exception instanceof InvalidAPIKeyException) throw new InvalidAPIKeyException();
			if(exception instanceof LookedUpNameRecentlyException) throw new LookedUpNameRecentlyException();
			return null;
		}
	}

	public static JSONObject request(UUID uuid) throws Exception {

		HttpClient client = new DefaultHttpClient();
		if(APIKeys.getAPIKey() == null) throw new NoAPIKeyException();
		HttpGet request = new HttpGet("https://api.hypixel.net/player?uuid=" + uuid + "&key=" + APIKeys.getAPIKey());
		HttpResponse response;
		String result;

		try {
			response = client.execute(request);
			HttpEntity entity = response.getEntity();
			InputStream inStream = entity.getContent();
			result = convertStreamToString(inStream);

			JSONObject playerObj = new JSONObject(result);
			if(!playerObj.getBoolean("success")) {
				if(playerObj.getString("cause").equals("Invalid API key")) throw new InvalidAPIKeyException();
				return null;
			}
			return playerObj;
		} catch(Exception exception) {
			if(exception instanceof InvalidAPIKeyException) throw new InvalidAPIKeyException();
			return null;
		}
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
