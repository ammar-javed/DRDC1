package com.aps490.drdc.prototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by JollyRancher on 16-02-21.
 */
public class LeapActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        switch (action) {
            case Constants.LEAP_PAYLOAD_TO_PROCESS:
                processPayLoad(intent);
            default:
                break;
        }
    }

    private void processPayLoad(Intent intent) {
        String payload = intent.getStringExtra("payload");
        Log.d(Constants.TAG, "[" + this.getClass().getSimpleName() + "]" + payload);
    }
}
