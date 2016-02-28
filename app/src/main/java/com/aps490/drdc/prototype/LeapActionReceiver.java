package com.aps490.drdc.prototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by JollyRancher on 16-02-21.
 */
public class LeapActionReceiver extends BroadcastReceiver {

    private int screenWidth;

    private int screenHeight;

    private final int leapXNormalizer = 350;

    private final int leapBoundXMin = 100;

    private final int leapBoundXMax = 650;

    private final int leapBoundYMin = 100;

    private final int leapBoundYMax = 450;

    public LeapActionReceiver(Context context) {
        super();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        Log.d(Constants.TAG, "Android Screen: " + screenWidth + " x " + screenHeight);
    }

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

            // Pointables
            JSONArray pointables = frame.getJSONArray("pointables");

            if (pointables.length() != 0) {
                Double x = pointables.getJSONObject(0).getJSONArray("tipPosition").getDouble(0);
                Double y = pointables.getJSONObject(0).getJSONArray("tipPosition").getDouble(1);

                Point norm_point = normalizePointToScreenDevice(x, y);

                if (norm_point != null) {
                    Log.d(Constants.TAG, "Pointer at (" + norm_point.x +", " +
                            norm_point.y + ")");
                }
            }


            // Gestures
            if (frame.getJSONArray("gestures").length() != 0) {
                JSONObject gesture = frame.getJSONArray("gestures").getJSONObject(0);

                String gestureType = gesture.getString("type");

                switch (gestureType){
                    case "swipe":
                        String state = gesture.getString("state");
                        if (state.equals("stop")){
                            Double swipeDirectionX = gesture.getJSONArray("direction").getDouble(0);
                            if (swipeDirectionX > 0) {
                                Log.i(Constants.TAG, "You swiped to the right! Open menu");
                            } else if (swipeDirectionX <= 0) {
                                Log.i(Constants.TAG, "You swiped to the left! Close menu");
                            }
                        }
                        break;
                    case "screenTap":
                        Log.i(Constants.TAG, "Screen Tap at " + gesture);
                        break;
                    default:
                        Log.i(Constants.TAG, "Gesture not recognized yet!");
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, e.toString());
        }
    }

    private Point normalizePointToScreenDevice(Double xRaw, Double yRaw) {
        
        // Leap coordinates start from ~(-350, 0, X)
        // so no need to normalize Y coordinate
        Double x = xRaw + leapXNormalizer;
        Double y = yRaw;

        if ( !(leapBoundXMin <= x) || !(x <= leapBoundXMax) ) {
            return null;
        }

        if ( !(leapBoundYMin <= y) || !(y <= leapBoundYMax) ) {
            return null;
        }

        // The pointer is within our bounding box, continue processing
        Point new_point = new Point();
        Double x_norm = x - leapBoundXMin;
        Double y_norm = y - leapBoundYMin;

        Double x_norm_factor = x_norm / (leapBoundXMax - leapBoundXMin);
        Double y_norm_factor = y_norm / (leapBoundYMax - leapBoundYMin);

        new_point.x = (int) (screenWidth * x_norm_factor);
        new_point.y = (int) (screenHeight * y_norm_factor);

        return new_point;

    }
}
