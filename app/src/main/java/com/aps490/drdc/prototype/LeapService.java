package com.aps490.drdc.prototype;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;

public class LeapService extends IntentService {

    /*
     * Intent identifier for client start/stop action.
     * boolean true to start client
     * boolean false to stop client
     */
    public static final String LEAP_THREAD_START = "leap.thread.start";

    /*
     * Asynchronous task which becomes the Websocket client.
     * Serves as a nice way to turn it off when needed.
     */
    private RetrieveLeapMotionDataTask leapListener;

    /*
     * Leap service's broadcast receiver
     */
    private LeapActionReceiver receiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public LeapService() {
        super("LeapServiceDRDC");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

//        if (receiver == null) {
//            IntentFilter filter = new IntentFilter(LeapActionReceiver.LEAP_PAYLOAD_TO_PROCESS);
//            filter.addCategory(Intent.CATEGORY_DEFAULT);
//            receiver = new LeapActionReceiver();
//            registerReceiver(receiver, filter);
//        }
        // Gets data from the incoming Intent
        Boolean run = workIntent.getBooleanExtra(LEAP_THREAD_START, false);

        if (run) {
            connectToServer();
            broadcastIntentResponseAction(Constants.CLIENT_STARTED);
        } else {
            disconnectFromServer();
            broadcastIntentResponseAction(Constants.CLIENT_STOPPED);
//            unregisterReceiver(receiver);
//            receiver = null;
        }

    }

    private void broadcastIntentResponseAction(String action_taken) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Constants.LEAP_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(Constants.ACTION_TAKEN, action_taken);
        sendBroadcast(broadcastIntent);
    }

    private void connectToServer() {

        if (leapListener == null) {
            leapListener = new RetrieveLeapMotionDataTask();
            leapListener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void disconnectFromServer() {
        if (leapListener != null) {
            Log.d(Constants.TAG, "Stopping");
            leapListener.cancel(true);
        } else {
            Log.d(Constants.TAG, "Null...");
        }
    }

    private class RetrieveLeapMotionDataTask extends
            AsyncTask<Void, String, Void> {

        private final WebSocketConnection mConnection = new WebSocketConnection();

        private Resources res = getResources();
        private final String url = String.format(res.getString(R.string.leap_endpoint));

//        public RetrieveLeapMotionDataTask(){
//            IntentFilter filter = new IntentFilter(Constants.LEAP_LISTENER_STOP);
//            filter.addCategory(Intent.CATEGORY_DEFAULT);
//            receiver = new LeapActionReceiver();
//            registerReceiver(receiver, filter);
//        }

        protected Void doInBackground(Void... args) {

            return null;
        }

    }
}
