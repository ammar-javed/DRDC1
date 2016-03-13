package com.aps490.drdc.prototype;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * Created by JollyRancher on 16-03-11.
 */
public class LeapTapEventReceiver extends BroadcastReceiver {

    private Context mContext;

    public LeapTapEventReceiver(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        switch (action) {
            case Constants.LEAP_TAP_RELEVANT_VIEW:
                simulateTapOnView(context, intent);
                break;
            default:
                break;
        }
    }

    private void simulateTapOnView(Context context, Intent intent){

        try {
            int viewID = intent.getIntExtra("viewID", 0);
            View hitView = ((Activity) context).findViewById(viewID);


            if (hitView.getClass().equals("android.support.design.widget.TextInputLayout")) {
                ((EditText) hitView).requestFocus();
            } else {
                hitView.requestFocus();
            }

            hitView.performClick();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
