package com.catandmouse.service;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import pubnub.Callback;
import pubnub.Pubnub;

import com.catandmouse.CMApplication;
import com.catandmouse.CMIntentActions;
import com.catandmouse.DataHelper;
import com.catandmouse.LogUtil;
import com.catandmouse.CMNotification;
import com.catandmouse.NotificationBean;
import com.catandmouse.CMConstants;
import com.google.myjson.Gson;
import com.google.myjson.reflect.TypeToken;
import com.openfeint.api.resource.User;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

public class NotificationService extends Service {
	//private Timer notificationTimer;
	//private long lastUpdateTime;
	//Vector<Integer> vecQuitGames;
	Timer cleanupTimer;
	Map<Integer,ListenTask> mapListenThreads;
	NotificationThread nt = null;
	
	public void onCreate() {
		//this.service = this;
		//lastUpdateTime = 0;
		// Start the timer
		//notificationTimer = new Timer();
		//notificationTimer.scheduleAtFixedRate(new NotificationTimerTask(), 30000, 30000);
		//vecQuitGames = new Vector<Integer>();
		mapListenThreads = new HashMap<Integer,ListenTask>();
		
        IntentFilter filter = new IntentFilter(CMIntentActions.ACTION_QUIT_GAME);
        this.registerReceiver(mReceiver, filter);		

        filter = new IntentFilter(CMIntentActions.ACTION_GAME_OVER);
        this.registerReceiver(mReceiver, filter);	
        
        cleanupTimer = new Timer();
        cleanupTimer.scheduleAtFixedRate(new TimerTask(){

			public void run() {
				// delete any notifications older than 15 minutes
				((CMApplication)getApplicationContext()).ndh.deleteBefore(System.currentTimeMillis() - CMConstants.TIME_MILLISECONDS_5_MIN*3);
			}
        	
        }, CMConstants.TIME_MILLISECONDS_5_MIN*3, CMConstants.TIME_MILLISECONDS_5_MIN*3);
	}

	public void onDestroy() {
		//notificationTimer.cancel();
		cleanupTimer.cancel();
		this.unregisterReceiver(mReceiver);
		// delete all notifications
		((CMApplication)getApplicationContext()).ndh.deleteAll();
		if (nt!=null)
			nt.setQuit(true);
		// delete all listeners
		Collection<ListenTask> col = mapListenThreads.values();
		Iterator<ListenTask> it = col.iterator();
		while(it.hasNext()) {
			ListenTask lt = (ListenTask) it.next();
			lt.getReceiver().setQuit(true);
			it.remove();
		}
		mapListenThreads.clear();
	}
	
//	private class ListenThread extends Thread {
//		private int gameNumber;
//		Thread thisThread;
//		
//		public ListenThread(int iGameNumber) {
//			gameNumber = iGameNumber;
//			thisThread = Thread.currentThread();
//		}
//		
//		@Override
//		public void run() {
//			pn.subscribe(Integer.toString(gameNumber), new Receiver());
//		}
//		
//		public void goToSleep() {
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				thisThread.interrupt();
//			}
//		}
//	}
	
	private class ListenTask extends AsyncTask<Integer, Void, Void> {
		Pubnub pn;
		Receiver myReceiver;
		UUID uuid;

		@Override
		protected Void doInBackground(Integer... params) {
			this.uuid = UUID.randomUUID();
			myReceiver = new Receiver(params[0],uuid);
			pn = new Pubnub(CMConstants.PUBNUB_PUBLISH_KEY, CMConstants.PUBNUB_SUBSCRIBE_KEY, CMConstants.PUBNUB_SECRET_KEY, false );
			pn.subscribe(Integer.toString(params[0]), myReceiver);
			
			return null;
		}
		
		public Receiver getReceiver() {
			return myReceiver;
		}
		
		public UUID getUuid() {
			return uuid;
		}
		
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.info("Notification service started");
		
		if (nt==null) {
			nt = new NotificationThread();
			nt.start();
		}
		else {
			// a restart?
			if (nt.isQuit())
				nt.setQuit(false);
			if (!nt.isAlive())
				nt.start();
		}
		
		if (intent!=null) {
			final int iGameNumber = intent.getIntExtra(CMConstants.PACKAGE+CMConstants.PARM_GAME_NUMBER, 0);
			if (iGameNumber > 0) {
				// Create a new listen thread if one doesn't already exist
				ListenTask existingListenThread  = mapListenThreads.get(iGameNumber);
				if (existingListenThread!=null) {
					// Interesting. Well, let's see if it thinks it should be dead
					if (existingListenThread.getReceiver()==null || (existingListenThread.getReceiver().isQuit())) {
						// OK, it thinks it should be dead, but its still in the map. Remove it
						LogUtil.info("Removing a listenthread from the map that thinks its dead");
						mapListenThreads.remove(iGameNumber);
						ListenTask listenThread = new ListenTask();
						listenThread.execute(iGameNumber);
						mapListenThreads.put(iGameNumber, listenThread);
					}
				}
				else {
					ListenTask listenThread = new ListenTask();
					listenThread.execute(iGameNumber);
					mapListenThreads.put(iGameNumber, listenThread);
				}
				//vecQuitGames.remove(new Integer(iGameNumber));
			}
		}
		
		return START_STICKY;
	}
	
	private class NotificationThread extends Thread {
		HashSet<CMNotification> setNotifications;
		private Object lockObject = new Object();
		private boolean quit = false;
		
		public NotificationThread() {
			setNotifications = new HashSet<CMNotification>();
		}
		
		public void add(CMNotification n) {
			synchronized(lockObject) {
				setNotifications.add(n);
			}
		}

		public void run() {
			while(!quit) {
				// process messages
				synchronized(lockObject) {
					Iterator<CMNotification> it = setNotifications.iterator();
					while(it.hasNext()) {
						CMNotification n = (CMNotification) it.next();
						processNotification(n);
						it.remove();
					}
					setNotifications.clear();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LogUtil.error(e.toString());
				}
			}
		}

		public void setQuit(boolean quit) {
			this.quit = quit;
		}

		public boolean isQuit() {
			return quit;
		}
		
		
		
	}
	
	public void processNotification(CMNotification n) {
		User user = ((CMApplication)getApplicationContext()).user;
		if (user!=null && user.name!=null && user.name.length()>0) {
//			CMNotification n = new CMNotification(nb.getGameNumber(), nb.getGameType(), 
//					nb.getIntendedPlayerId(), nb.getCatPlayerId(), nb.getCatPlayerName(), 
//					nb.getMousePlayerId(), nb.getMousePlayerName(), 
//					nb.getNotificationType(), nb.getActivityDate());
			// Is this a dup?
//			List<CMNotification> notifications = ((CMApplication)getApplicationContext()).ndh.selectNotifications(0, nb.getGameNumber());
//			boolean bFound = false;
//			for (CMNotification notification : notifications) {
//				if (notification.equals(n)) {
//					bFound = true;
//					break;
//				}
//			}
//			if (!bFound) {
				// If this is a state change notification that affects me, need to update the db
				// here and now before blasting it off to everyone
				if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_CAT) {
					// is it me?
					if (user.name.equals(n.getCatPlayerName())) {
						// whoa, it is me!  update the db
						ContentValues cv = new ContentValues();
						cv.put(DataHelper.COLUMN_IS_CAT, true);
						((CMApplication)getApplicationContext()).dh.update(n.getGameNumber(), cv);										
					}
				}
				else if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_MOUSE) {
					// is it me?
					if (user.name.equals(n.getMousePlayerName())) {
						// whoa, it is me!  update the db
						ContentValues cv = new ContentValues();
						cv.put(DataHelper.COLUMN_IS_CAT, false);
						((CMApplication)getApplicationContext()).dh.update(n.getGameNumber(), cv);										
					}
				}
				// Put it in the notification db
				((CMApplication)getApplicationContext()).ndh.insert(n);

				Bundle bundle = new Bundle();
				bundle.putParcelable(CMConstants.NOTIFICATION_PACKAGE, n);
				String action = CMIntentActions.mapActionsToNotifications.get(n.getNotificationType());
				Intent intent = new Intent(action);
				intent.putExtras(bundle);
				sendBroadcast(intent);
//			}
		}		
		else {
			LogUtil.error("Bad user or username");
		}
		
	}
	
	class Receiver implements Callback {
		private boolean quit = false;
		private int gameNumber;
		private UUID uuid;
		
		public Receiver(int gameNumber, UUID uuid) {
			this.gameNumber = gameNumber;
			this.uuid = uuid;
		}

		public boolean execute(JSONObject message) {
			NotificationBean nb;
			try {
				// Validation check - make sure I'm the one and only listen thread
				ListenTask lt = mapListenThreads.get(gameNumber);
				// is this me
				if (!lt.getUuid().equals(uuid)) {
					// I'm a rogue; kill me!
					LogUtil.info("Killing a rogue listener");
					quit = true;
					return false;
				}
				
				String notification = (String) message.get(CMConstants.PARM_NOTIFICATION_BEAN);
				Type nbType = new TypeToken<NotificationBean>(){}.getType();
				nb = new Gson().fromJson(notification, nbType);
				if (quit) {
					// huh, we quit the game
					LogUtil.info("Notification receiver quitting");
					return false;
				}
				else {
					// process
					LogUtil.info("Processing Pubnub message: "+message.toString());
					CMNotification n = new CMNotification(nb.getGameNumber(), nb.getGameType(), 
					nb.getIntendedPlayerId(), nb.getCatPlayerId(), nb.getCatPlayerName(), 
					nb.getMousePlayerId(), nb.getMousePlayerName(), 
					nb.getNotificationType(), nb.getActivityDate());
					
					nt.add(n);
					return true;
				}
			} catch (JSONException e) {
				LogUtil.error("JsonException parsing msg: "+e.toString());
				return true;
			}
		}

		public boolean isQuit() {
			return quit;
		}

		public void setQuit(boolean quit) {
			this.quit = quit;
		}

		
	}
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action!=null) {
            	LogUtil.info("NotificationService processing broadcast: "+action);

            	if (action.equals(CMIntentActions.ACTION_QUIT_GAME) ||
            			action.equals(CMIntentActions.ACTION_GAME_OVER)	) {
            		int gameNumber = intent.getIntExtra(CMConstants.GAME_PACKAGE+CMConstants.PARM_GAME_NUMBER, 0);
            		if (gameNumber > 0) {
            			//vecQuitGames.add(gameNumber);
            			// Delete all notifications for this game from db
            			((CMApplication)getApplicationContext()).ndh.delete(gameNumber);
            			ListenTask someThread = mapListenThreads.get(gameNumber);
            			if (someThread!=null) {
            				LogUtil.info("Attempting to cancel a notification thread");
            				someThread.getReceiver().setQuit(true);
            				mapListenThreads.remove(gameNumber);
            				if (!someThread.cancel(true))
            					LogUtil.error("Could not cancel the listen task in NotificationService");
            				//someThread.interrupt();
            			}
            		}
            	}
            }
		}
    };
	
//	private class NotificationAsyncTask extends Thread {
//
//		public void run() {
//			// Query for all notifications for all games I'm in
//			List<Integer> gamesList = ((CMApplication)getApplicationContext()).dh.selectGameNumbers();
//			if (gamesList.size() > 0) {
//				LogUtil.info("NotificationService querying for notifications");
//				// get our player id
//				User user = ((CMApplication)getApplicationContext()).user;
//				if (user!=null && user.name!=null && user.name.length()>0) {
//					// Got everything we need, make our call
////					Vector<NameValuePair> vars = new Vector<NameValuePair>();
////					vars.add(new BasicNameValuePair(ServerConstants.PARM_PLAYER_ID, user.userID()));
////					//vars.add(new BasicNameValuePair(ServerConstants.PARM_GAME_NUMBER, Double.toString(latitude)));
////					vars.add(new BasicNameValuePair(ServerConstants.PARM_LAST_UPDATE_TIME, Long.toString(lastUpdateTime)));
//					String strGamesList = new String();
//					for (int i=0; i < gamesList.size(); i++) {
//						if (i>0)
//							strGamesList +=",";
//						strGamesList += Integer.toString(gamesList.get(i));
//					}
//					Vector<NameValuePair> vars = new Vector<NameValuePair>();
//					vars.add(new BasicNameValuePair(CMConstants.PARM_PLAYER_ID, user.userID()));
//					vars.add(new BasicNameValuePair(CMConstants.PARM_LAST_UPDATE_TIME, Long.toString(lastUpdateTime)));
//					vars.add(new BasicNameValuePair(CMConstants.PARM_GAME_NUMBER, strGamesList));
//					
//					String url = ServerUtil.NOTIFICATION_URL+"?"+URLEncodedUtils.format(vars, null);
//					HttpGet request = new HttpGet(url);
//					HttpClient client = new DefaultHttpClient();
//					//lastUpdateTime = System.currentTimeMillis();
//					try {
//						HttpResponse resp = client.execute(request);
//						int rc = resp.getStatusLine().getStatusCode();
//						if (rc==0) {
//							LogUtil.info("no notifications");
//						}
//						else if (rc!=HttpStatus.SC_OK) {
//							// whatever, log it
//							LogUtil.error("Error returned from get notifications. rc= "+rc);						
//						}
//						else {
//							// Process the response
//							Reader reader = new InputStreamReader(resp.getEntity().getContent());
//							Gson gson = new Gson();
//							Type listType = new TypeToken<List<NotificationBean>>(){}.getType();
//							List<NotificationBean> beanList = gson.fromJson(reader, listType);
//							// Create our parcelable notifications and send them
//							for (int i=0; beanList!=null && i < beanList.size(); i++) {
//								if (i==0)
//									LogUtil.info("Processing "+beanList.size()+" notifications");
//								NotificationBean nb = beanList.get(i);
//								lastUpdateTime = nb.getActivityDate();
//								CMNotification n = new CMNotification(nb.getGameNumber(), nb.getGameType(), 
//										nb.getIntendedPlayerId(), nb.getCatPlayerName(), nb.getMousePlayerName(), 
//										nb.getNotificationType(), nb.getActivityDate());
//								// If this is a state change notification that affects me, need to update the db
//								// here and now before blasting it off to everyone
//								if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_CAT) {
//									// is it me?
//									if (user.name.equals(n.getCatPlayerName())) {
//										// whoa, it is me!  update the db
//										ContentValues cv = new ContentValues();
//										cv.put(DataHelper.COLUMN_IS_CAT, true);
//										((CMApplication)getApplicationContext()).dh.update(n.getGameNumber(), cv);										
//									}
//								}
//								else if (n.getNotificationType()==CMConstants.NOTIFICATION_STATE_MOUSE) {
//									// is it me?
//									if (user.name.equals(n.getMousePlayerName())) {
//										// whoa, it is me!  update the db
//										ContentValues cv = new ContentValues();
//										cv.put(DataHelper.COLUMN_IS_CAT, false);
//										((CMApplication)getApplicationContext()).dh.update(n.getGameNumber(), cv);										
//									}
//								}
//								Bundle bundle = new Bundle();
//								bundle.putParcelable(CMConstants.NOTIFICATION_PACKAGE, n);
//								String action = CMIntentActions.mapActionsToNotifications.get(n.getNotificationType());
//								Intent intent = new Intent(action);
//								intent.putExtras(bundle);
//								sendBroadcast(intent);
//							}
//						}
//					} catch (ClientProtocolException e) {
//						LogUtil.error(e.toString());
//					} catch (IOException e) {
//						LogUtil.error(e.toString());
//					}		
//				}
//				else {
//					LogUtil.error("Bad user or username");
//				}
//				
//			}
//		}
//		
//	}
//
//	private class NotificationTimerTask extends TimerTask {
//
//		public void run() {
//			NotificationAsyncTask task = new NotificationAsyncTask();
//			task.start();
//		}
//	}
	
	public IBinder onBind(Intent arg0) {
		return null;
	}
	

}
