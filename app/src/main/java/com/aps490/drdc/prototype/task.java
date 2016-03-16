package com.aps490.drdc.prototype;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aps490.drdc.LeapMotion.LeapActionReceiver;
import com.aps490.drdc.LeapMotion.LeapTapEventReceiver;

import java.io.IOException;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;

public class task extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView l ;
    Intent intent;

    /**
     * Websocket connection
     */
    private WebSocketConnection mConnection;

    /**
     * Websocket connect handler, specified in onCreate
     */
    private WebSocketHandler mConnectionHandler;

    /**
     * Reference to URL websocket client will connect to,
     *
     */
    private String url;

    /**
     * Seperate thread handler to process leap motion frames
     */
    HandlerThread mHandlerThread;
    Looper mLooper;
    Handler mHandler;

    /**
     * Leap motion JSON frame receiver and processor
     */
    LeapActionReceiver mReceiver;
    IntentFilter mLeapProcessFilter;

    /**
     * Receives and performs leap tap events
     */
    LeapTapEventReceiver mLeapTapReceiver;
    IntentFilter mLeapTapFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        String[] values;
        intent = getIntent();
        String moduleName = intent.getStringExtra("name");
        System.out.println(moduleName);

        /**
         * Set up websocket to listen to leapmotion.
         */
        Resources res = getResources();
        url = String.format(res.getString(R.string.leap_endpoint));

        mConnection = new WebSocketConnection();

        mConnectionHandler = new WebSocketHandler() {

            @Override
            public void onOpen() {
                Log.d(Constants.TAG, "Status: Connected");
            }

            @Override
            public void onTextMessage(String payload) {
                sendLeapServicePayload(payload);
            }

            @Override
            public void onClose(int code, String reason) {
                Log.d(Constants.TAG, "Connection lost. Code:" + code);

                // Do not attempt reconnect if it was a manual disconnect from us.
                if (code != 3) {

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Log.i(Constants.TAG, "Websocket onClose sleep:", e);
                    }

                    reconnect();
                }
            }

            private void sendLeapServicePayload(String payload) {
                Intent payloadIntent = new Intent();
                payloadIntent.setAction(Constants.LEAP_PAYLOAD_TO_PROCESS);
                payloadIntent.addCategory(Intent.CATEGORY_DEFAULT);
                payloadIntent.putExtra("payload", payload);
                sendBroadcast(payloadIntent);
            }
        };

        try {
            mConnection.connect(url, mConnectionHandler);
        } catch (Exception e) {
            Log.e(Constants.TAG, e.toString());
        }

        try{
            Assembly assembly = new Assembly( getAssets().open(CourseModules.map.get(moduleName) ) );
            values = assembly.getModules().toArray(new String[0]);
            System.out.println(values);
//            getSupportActionBar().setHomeButtonEnabled(true);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            l = (ListView) findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_view_layout,R.id.list_content,values);
            l.setAdapter(adapter);
            l.setOnItemClickListener(this);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        /**
         * Set up leap motion receiveres
         */
        // Set up new thread and handler for broadcast receiver
        // to process LeapActionReceiver.
        mHandlerThread = new HandlerThread("LeapProcessingThread");
        mHandlerThread.start();
        mLooper = mHandlerThread.getLooper();
        mHandler = new Handler(mLooper);

        // Create new broadcast filter
        mLeapProcessFilter = new IntentFilter(Constants.LEAP_PAYLOAD_TO_PROCESS);
        mLeapProcessFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Pass in the root activity view as well as context.
        mReceiver = new LeapActionReceiver(this.getApplicationContext(), this.getWindow().getDecorView().getRootView());

        // Will not process on main thread.
        registerReceiver(mReceiver, mLeapProcessFilter, null, mHandler);

        // Register new receiver to process tap events on UI elements
        // will run on main thread, so allow access to UI.
        mLeapTapFilter = new IntentFilter(Constants.LEAP_TAP_RELEVANT_VIEW);
        mLeapTapFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mLeapTapReceiver = new LeapTapEventReceiver(this.getApplicationContext());

        registerReceiver(mLeapTapReceiver, mLeapTapFilter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent newIntent = new Intent(this, instructions.class);
        newIntent.putExtra("name",intent.getStringExtra("name"));
        newIntent.putExtra("task",position);
        startActivity(newIntent);

    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, listView.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    /**
     * If websocket connection goes down unintentionally,
     * attempt a reconnect.
     */
    void reconnect(){
        try {
            mConnection.connect(url, mConnectionHandler);
        } catch (Exception e) {
            Log.e(Constants.TAG, e.toString());
        }
    }

    /**
     * Android Activity Lifecycle event
     * Disconnect the websocket listener
     * Unregister the broadcast listener to avoid memory leaks outside of application
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mConnection != null) {
            mConnection.disconnect();
            unregisterReceiver(mReceiver);
            unregisterReceiver(mLeapTapReceiver);
            mConnection = null;
        }
    }

    /**
     * Android Activity Lifecycle event
     * Restart the websocket connection and handler
     * Register the broadcast listener for Leap action events.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mConnection == null) {
            mConnection = new WebSocketConnection();
            try {
                mConnection.connect(url, mConnectionHandler);
                registerReceiver(mReceiver, mLeapProcessFilter, null, mHandler);
                registerReceiver(mLeapTapReceiver, mLeapTapFilter);
            } catch (Exception e) {
                Log.e(Constants.TAG, e.toString());
            }
        }
    }
}
