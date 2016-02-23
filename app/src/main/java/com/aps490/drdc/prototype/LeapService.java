package com.aps490.drdc.prototype;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

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
}
