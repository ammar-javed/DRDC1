package com.aps490.drdc.prototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

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
        //Log.d(Constants.TAG, "[" + this.getClass().getSimpleName() + "]" + payload);

        try {
            JSONObject frame = new JSONObject(payload);


            // Gestures
            if (frame.getJSONArray("gestures").length() != 0) {
                JSONObject gesture = frame.getJSONArray("gestures").getJSONObject(0);

                String gestureType = gesture.getString("type");

                switch (gestureType){
                    case "swipe":
                        Double swipeDirectionX = gesture.getJSONArray("direction").getDouble(0);
                        if (swipeDirectionX > 0) {
                            Log.i(Constants.TAG, "You swiped to the right! Open menu");
                        } else if (swipeDirectionX <= 0) {
                            Log.i(Constants.TAG, "You swiped to the left! Close menu");
                        }
                        break;
                    default:
                        Log.i(Constants.TAG, "Gesture new recognized yet!");
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, e.toString());
        }
    }
}
