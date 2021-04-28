package com.catandmouse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.os.AsyncTask;

import com.google.myjson.Gson;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.resource.User;

public class ServerUtil {
	public static final String VERSION="1";
	public static final String MAIN_URL_SECURE="https://"+VERSION+".catandmouseapp.appspot.com";
	public static final String MAIN_URL=MAIN_URL_SECURE;//"http://"+VERSION+".catandmouseapp.appspot.com";
	public static final String MAIN_URL_UNSECURE="http://"+VERSION+".catandmouseapp.appspot.com";
	public static final String LOGIN_URL=MAIN_URL+"/login";
	public static final String LOGOUT_URL=MAIN_URL+"/logout";
	public static final String UPDATE_LOCATION_URL=MAIN_URL+"/updatelocation";
	public static final String CAUGHT_PLAYER_URL=MAIN_URL+"/caughtplayer";
	public static final String NOTIFICATION_URL=MAIN_URL+"/getnotifications";
	public static final String GET_GAMES_URL=MAIN_URL+"/getgames";
	public static final String JOIN_GAME_URL=MAIN_URL+"/joingame";
	public static final String QUIT_GAME_URL=MAIN_URL+"/quitgame";
	public static final String CREATE_GAME_URL=MAIN_URL_SECURE+"/creategame";
	public static final String PLAYER_LOCATIONS_URL=MAIN_URL+"/playerlocations";
	public static final String HELP_URL=MAIN_URL_UNSECURE+"/help.htm";
	public static final String SUPPORT_URL=MAIN_URL_UNSECURE+"/support.htm";
	public static final String CHECK_GAME_URL=MAIN_URL+"/checkgame";
	public static final String CHECK_JOINED_GAMES_URL=MAIN_URL+"/checkjoinedgames";

	public static int doLogin(User user, String macAddress, double latitude, double longitude) {
		// Get feint id
		String playerId = user.userID();
		if (playerId!=null && playerId.length()>0) {
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, playerId));
			vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_NAME, user.name));
			vars.add(new BasicNameValuePair(CMConstants.PARM_MAC_ADDR, macAddress));
			vars.add(new BasicNameValuePair(CMConstants.PARM_LATITUDE, Float.toString((float)latitude)));
			vars.add(new BasicNameValuePair(CMConstants.PARM_LONGITUDE, Float.toString((float)longitude)));
			String url = LOGIN_URL;//+"?"+URLEncodedUtils.format(vars, null);
			HttpPost request = new HttpPost(url);
			HttpClient client = ServerUtil.getHttpClient();
			try {
				request.setEntity(new UrlEncodedFormEntity(vars));
				HttpResponse resp = client.execute(request);
				return resp.getStatusLine().getStatusCode();
			} catch (Exception e) {
				LogUtil.error("Exception from doLogin: "+e.toString());
				return -1000;
			}
		}
		else
			return CMConstants.ERR_BAD_PARMS;
	}
	
	public static void doLogout(User user) {
		// Get feint id
		String playerId = user.userID();
		if (playerId!=null && playerId.length()>0) {
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, playerId));
			String url = LOGOUT_URL;//+"?"+URLEncodedUtils.format(vars, null);
			HttpPost request = new HttpPost(url);
			HttpClient client = ServerUtil.getHttpClient();
			try {
				request.setEntity(new UrlEncodedFormEntity(vars));
				client.execute(request);
				LogUtil.info("Logout processed successfully");
			} catch (Exception e) {
				LogUtil.error("Exception from doLogout: "+e.toString());
			}
		}
	}
	
	public static int createGame(GameBean gb) {
		int rc = -1000;
		
		Gson gson = new Gson();
		String url = CREATE_GAME_URL;
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
	    post.setHeader("Content-type", "application/json");
		
		HttpClient client = ServerUtil.getHttpClient();
		try {
			post.setEntity(new StringEntity(gson.toJson(gb)));
			HttpResponse resp = client.execute(post);
			rc = resp.getStatusLine().getStatusCode();
		} catch (Exception e) {
			LogUtil.error("Exception from createGame: "+e.toString());
		}
		
		return rc;
	}
	
	public static int joinGame(String playerId, int gameNumber, String password) {
		int rc = -1000;
		Vector<NameValuePair> vars = new Vector<NameValuePair>();
		vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, playerId));
		vars.add(new BasicNameValuePair(CMConstants.PARM_GAME_NUMBER, Integer.toString(gameNumber)));
		if (password!=null)
			vars.add(new BasicNameValuePair(CMConstants.PARM_PASSWORD, password));
		String url = JOIN_GAME_URL;
		HttpPost request = new HttpPost(url);
		HttpClient client = ServerUtil.getHttpClient();
		try {
			request.setEntity(new UrlEncodedFormEntity(vars));
			HttpResponse resp = client.execute(request);
			rc = resp.getStatusLine().getStatusCode();
			LogUtil.info("rc from JoinGame is "+rc);
		} catch (Exception e) {
			LogUtil.error("Exception from joinGame: "+e.toString());
		}
		
		return rc;
	}
	
	public static int checkGameName(String gameName) {
		int rc = -1000;
		Vector<NameValuePair> vars = new Vector<NameValuePair>();
		vars.add(new BasicNameValuePair(CMConstants.PARM_GAME_NAME, gameName));
		String url = CHECK_GAME_URL+"?"+URLEncodedUtils.format(vars, null);
		HttpGet request = new HttpGet(url);
		HttpClient client = ServerUtil.getHttpClient();
		try {
			HttpResponse resp = client.execute(request);
			rc = resp.getStatusLine().getStatusCode();
		} catch (Exception e) {
			LogUtil.error("Exception from checkGameName: "+e.toString());
		}
		
		return rc;
	}
	
	public static int quitGame(String playerId, int gameNumber) {
		int rc = -1000;
		Vector<NameValuePair> vars = new Vector<NameValuePair>();
		vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, playerId));
		vars.add(new BasicNameValuePair(CMConstants.PARM_GAME_NUMBER, Integer.toString(gameNumber)));
		String url = QUIT_GAME_URL;
		HttpPost request = new HttpPost(url);
		HttpClient client = ServerUtil.getHttpClient();
		try {
			request.setEntity(new UrlEncodedFormEntity(vars));
			HttpResponse resp = client.execute(request);
			rc = resp.getStatusLine().getStatusCode();
		} catch (Exception e) {
			LogUtil.error("Exception from quitGame: "+e.toString());
		}
		
		return rc;
		
	}
	
	private static class LocationUpdateThread extends Thread {
		String playerId;
		double latitude;
		double longitude;
		public LocationUpdateThread(String playerId, double latitude, double longitude) {
			this.playerId = playerId;
			this.latitude = latitude;
			this.longitude = longitude;
		}
		public void run() {
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, playerId));
			vars.add(new BasicNameValuePair(CMConstants.PARM_LATITUDE, Double.toString(latitude)));
			vars.add(new BasicNameValuePair(CMConstants.PARM_LONGITUDE, Double.toString(longitude)));
			String url = UPDATE_LOCATION_URL;//+"?"+URLEncodedUtils.format(vars, null);
			HttpPost request = new HttpPost(url);
			HttpClient client = ServerUtil.getHttpClient();
			try {
				request.setEntity(new UrlEncodedFormEntity(vars));
				HttpResponse resp = client.execute(request);
			} catch (Exception e) {
				LogUtil.error("Exception from LocationUpdateThread: "+e.toString());
			}
		}
		
		
	}
	
	public static void doLocationUpdate(String playerId, double latitude, double longitude) {
		// update location in an async task
		@SuppressWarnings("unchecked")
		LocationUpdateThread someThread = new LocationUpdateThread(playerId, latitude, longitude);
		someThread.start();
	}			
	
	public static HttpClient getHttpClient() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		 
		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);

		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		 
		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(cm, params);		
	}

}
