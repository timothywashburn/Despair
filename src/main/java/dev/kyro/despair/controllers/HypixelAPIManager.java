package dev.kyro.despair.controllers;

import dev.kyro.despair.exceptions.InvalidAPIKeyException;
import dev.kyro.despair.exceptions.LookedUpNameRecentlyException;
import dev.kyro.despair.exceptions.NoAPIKeyException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.*;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class HypixelAPIManager {

	public static JSONObject request(String name) throws Exception {

		HttpClient client = new DefaultHttpClient();
		if(APIKeys.getAPIKey() == null) return requestProxy(name);
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
		if(APIKeys.getAPIKey() == null) return requestProxy(uuid);
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

	public static JSONObject requestProxy(String name) throws Exception {
		if(!APIKeys.hasKeys()) throw new NoAPIKeyException();
		Config.KeyAndProxy keyAndProxy = APIKeys.getAPIKeyProxy();

		HttpResponse response;
		String result;

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(keyAndProxy.proxyIp, keyAndProxy.proxyPort),
				new UsernamePasswordCredentials(keyAndProxy.proxyUsername, keyAndProxy.proxyPassword));
		HttpClientBuilder clientBuilder = HttpClients.custom();
		clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		CloseableHttpClient httpclient = clientBuilder.build();
		HttpHost target = new HttpHost("api.hypixel.net",
				80, "http");
		HttpHost proxy = new HttpHost(keyAndProxy.proxyIp, keyAndProxy.proxyPort, "http");
		RequestConfig.Builder reqConfigBuilder = RequestConfig.custom();
		reqConfigBuilder = reqConfigBuilder.setProxy(proxy);
		RequestConfig config = reqConfigBuilder.build();

		HttpGet httpGet = new HttpGet("/player?name=" + name + "&key=" + keyAndProxy.key);
		httpGet.setConfig(config);

		try {
			response = httpclient.execute(target, httpGet);
			HttpEntity entity = response.getEntity();
			InputStream inStream = entity.getContent();
			result = convertStreamToString(inStream);

			if(result.contains("Not authenticated or invalid authentication credentials")) throw new AuthenticationException();

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
			if(exception instanceof AuthenticationException) throw new AuthenticationException();
			if(exception instanceof HttpHostConnectException) throw exception;
			exception.printStackTrace();
			return null;
		}
	}

	public static JSONObject requestProxy(UUID uuid) throws Exception {
		if(!APIKeys.hasKeys()) throw new NoAPIKeyException();
		Config.KeyAndProxy keyAndProxy = APIKeys.getAPIKeyProxy();

		HttpResponse response;
		String result;

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(keyAndProxy.proxyIp, keyAndProxy.proxyPort),
				new UsernamePasswordCredentials(keyAndProxy.proxyUsername, keyAndProxy.proxyPassword));
		HttpClientBuilder clientBuilder = HttpClients.custom();
		clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		CloseableHttpClient httpclient = clientBuilder.build();
		HttpHost target = new HttpHost("api.hypixel.net",
				80, "http");
		HttpHost proxy = new HttpHost(keyAndProxy.proxyIp, keyAndProxy.proxyPort, "http");
		RequestConfig.Builder reqConfigBuilder = RequestConfig.custom();
		reqConfigBuilder = reqConfigBuilder.setProxy(proxy);
		RequestConfig config = reqConfigBuilder.build();

		HttpGet httpGet = new HttpGet("/player?uuid=" + uuid + "&key=" + keyAndProxy.key);
		httpGet.setConfig(config);

		try {
			response = httpclient.execute(target, httpGet);
			HttpEntity entity = response.getEntity();
			InputStream inStream = entity.getContent();
			result = convertStreamToString(inStream);

			if(result.contains("Not authenticated or invalid authentication credentials")) throw new AuthenticationException();

			JSONObject playerObj = new JSONObject(result);
			if(!playerObj.getBoolean("success")) {
				if(playerObj.getString("cause").equals("Invalid API key")) throw new InvalidAPIKeyException();
				return null;
			}
			return playerObj;
		} catch(Exception exception) {
			if(exception instanceof InvalidAPIKeyException) throw new InvalidAPIKeyException();
			if(exception instanceof AuthenticationException) throw new AuthenticationException();
			if(exception instanceof HttpHostConnectException) throw exception;
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
