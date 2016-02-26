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
            case Constants.LEAP_ACTION:
                asyncClientAction(intent);
                break;
            case Constants.LEAP_LISTENER_STOP:
                break;

            case Constants.LEAP_PAYLOAD_TO_PROCESS:
                processPayLoad(intent);
            default:
                break;
        }
    }

    private void asyncClientAction(Intent intent) {
        Log.d(Constants.TAG, "[" + this.getClass().getSimpleName() + "]" + "Leap Client: " + Constants.ACTION_TAKEN +
                " - " + intent.getStringExtra(Constants.ACTION_TAKEN));
    }

    private void processPayLoad(Intent intent) {
        String payload = intent.getStringExtra("payload");
        Log.d(Constants.TAG, "[" + this.getClass().getSimpleName() + "]" + payload);
    }
}
