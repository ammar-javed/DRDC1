package com.aps490.drdc.prototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by JollyRancher on 16-02-21.
 */
public class LeapReceiver extends BroadcastReceiver {

    public static final String LEAP_ACTION =
            "com.aps490.intent.action.MESSAGE_PROCESSED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AMMAR", "Leap action recieved in " + this.getClass().getSimpleName());
        Log.d("AMMAR", "Leap tap X: " + intent.getIntExtra("tapx", 0) + " Y: " + intent.getIntExtra("tapy", 0));
    }
}
