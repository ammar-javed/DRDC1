package com.aps490.drdc.prototype;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;

public class LeapService extends IntentService {

    public static final String LEAP_THREAD_START = "leap.thread.start";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public LeapService() {
        super("LeapServiceDRDC");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        Boolean run = workIntent.getBooleanExtra(LEAP_THREAD_START, false);

        if (run) {
            Log.d("AMMAR", "Service: " + run);
            connectToServer();
        }

        broadcastGestureAction();
    }

    private void broadcastGestureAction() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(LeapReceiver.LEAP_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("tapx", 23);
        broadcastIntent.putExtra("tapy", 174);
        sendBroadcast(broadcastIntent);
    }

    private void connectToServer() {
        (new RetrieveLeapMotionDataTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class RetrieveLeapMotionDataTask extends
            AsyncTask<Void, String, Void> {

        private final WebSocketConnection mConnection = new WebSocketConnection();

        protected Void doInBackground(Void... args) {

            try {

                mConnection.connect("ws://192.168.2.24:8889", new WebSocketHandler() {

                    @Override
                    public void onOpen() {
                        Log.d("AMMAR", "Status: Connected to " + R.string.leap_endpoint);
                        mConnection.sendTextMessage("Hello, world!");
                    }

                    @Override
                    public void onTextMessage(String payload) {
                        Log.d("AMMAR", "Got echo: " + payload);
                    }

                    @Override
                    public void onClose(int code, String reason) {
                        Log.d("AMMAR", "Connection lost.");
                    }
                });


//                URL url = new URL(getString(R.string.leap_endpoint));
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//
//                Log.i("AMMAR", "connected");
//
//                JSONObject data = new JSONObject();
//                JSONObject response;
//
//                data.put("focused", "true");
//                data.put("background", "true");
//                data.put("enableGestures", "true");
//                Log.i("AMMAR", data.toString());
//
//                BufferedInputStream iStream = new BufferedInputStream(urlConnection.getInputStream());
//
//                String line = "";
//                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
//                StringBuilder sb = new StringBuilder();
//                while ((line = reader.readLine()) != null)
//                {
//                    sb.append(line + "\n");
//                }
//                // Response from server after login process will be stored in response variable.
//                response = new JSONObject(sb.toString());
//
//                Log.i("AMMAR", "Response: " + sb.toString());
//
//                Log.i("AMMAR","end of loop");

            } catch (Exception e) {
                Log.e("AMMAR", e.toString());
            }
            return null;
        }

    }
}
