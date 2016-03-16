package com.aps490.drdc.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.app.DialogFragment;

import com.aps490.drdc.LeapMotion.LeapActionReceiver;
import com.aps490.drdc.LeapMotion.LeapTapEventReceiver;

import java.util.ArrayList;

import java.io.IOException;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;

public class instructions extends AppCompatActivity {
    Assembly assembly;
    Instruction currentInstr;int task;
    private ImageView mDialog;
    ArrayList<String> figures;
    Intent returnIntent;
    Intent intent;
    Dialog builder;
    FinishedDialog finishedDialog;

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
    InstructionsLeapTapEventReceiver mLeapTapReceiver;
    IntentFilter mLeapTapFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        returnIntent = new Intent(this, listView.class);

        System.out.println("Creating instructions activity.");
        String assemblyName = intent.getStringExtra("name");
        task = intent.getIntExtra("task", 0);
        System.out.println("Acquired intent values.");

        setContentView(R.layout.activity_instructions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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


        try {
            System.out.println("Attempting to load assembly " + assemblyName);
            assembly = new Assembly( getAssets().open(CourseModules.map.get(assemblyName) ) );
            System.out.println("Initialized assembly: " + assemblyName);
            assembly.selectModule(task);
            System.out.println("Module selected: " + assembly.getModules().get(task));

            Instruction instruction = assembly.getInstr();
            if(instruction==null)
                System.out.println("ERROR: First instruction was null!");

            currentInstr = instruction;

            ((TextView) findViewById(R.id.textViewInst)).setText(currentInstr.getText());

            updateProgress();
            figures = assembly.getFigures();

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
        mLeapTapReceiver = new InstructionsLeapTapEventReceiver(this.getApplicationContext());

        registerReceiver(mLeapTapReceiver, mLeapTapFilter);

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

    public void getPreInstruction(View view) {
        Instruction instruction = assembly.getPreviousInstr();
        if(instruction!=null)
            currentInstr = instruction;

        ((TextView) findViewById(R.id.textViewInst)).setText(currentInstr.getText());
        updateProgress();
    }

    public void getNextInstruction(View view) {
        Instruction instruction = assembly.getInstr();
        if(instruction!=null) {
            currentInstr = instruction;
            ((TextView) findViewById(R.id.textViewInst)).setText(currentInstr.getText());
            updateProgress();
        }
        else {
            finishPopUp();
        }

    }
    public void seeFigures(View view) {
        if( figures.isEmpty() ) {
            System.out.println("No figures for this module");
            //Add a popup saying no figure available for module
        }
        else {
            System.out.println("Figure name for this app is: " + assembly.getFigures().get(0));
            String figureName;
            if( currentInstr.hasFigure() )
              figureName = currentInstr.getFigure();
            else
              figureName = assembly.getFigures().get(0);

            showImage(figureName);
        }
    }

    public void showImage(String fileName ) {
        builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                builder = null;
            }
        });

        ImageView imageView = new ImageView(this);
        try {
            imageView.setImageBitmap(BitmapFactory.decodeStream(getAssets().open(fileName)));
        }catch(IOException e) {
            e.printStackTrace();
        }

        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }

    public void updateProgress() {
        ((TextView) findViewById(R.id.textViewInstrCount)).setText(
                "Instruction " + (assembly.currentInstrIndex() + 1) + "/" + assembly.instrCount());
    }

    public void finishPopUp() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        finishedDialog = new FinishedDialog();
        finishedDialog.show(ft, "dialog");
    }

    public class FinishedDialog extends DialogFragment implements DialogInterface.OnDismissListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.finishPopUp)
                    .setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Launch main menu activity
                            //TODO change to main menu
                            startActivity(returnIntent);
                        }
                    })
                    .setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing but exit pop up
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            finishedDialog = null;
        }

    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, task.class);
                homeIntent.putExtra("name", intent.getStringExtra("name"));
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));

    }

    public class InstructionsLeapTapEventReceiver extends BroadcastReceiver {

        private Context mContext;

        public InstructionsLeapTapEventReceiver(Context context) {
            super();
            mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            switch (action) {
                case Constants.LEAP_TAP_RELEVANT_VIEW:

                    if (builder != null) {
                        builder.dismiss();
                        return;
                    } else if (finishedDialog != null) {
                        finishedDialog.dismiss();
                        return;
                    }

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

                Log.i(Constants.TAG, hitView.getClass().toString());

                if (hitView instanceof ListView) {
                    int pos = intent.getIntExtra("listPos", -1);
                    Log.i(Constants.TAG, "List view position: " + pos);
                    ListView lView = (ListView) hitView;
                    lView.performItemClick(lView.getAdapter().getView(pos, null, null), pos,
                            lView.getAdapter().getItemId(pos));

                    return;
                }

                if (hitView.getClass().equals("android.support.design.widget.TextInputLayout")) {
                    ((EditText) hitView).requestFocus();
                } else if (hitView.getClass().equals("com.aps490.drdc.customlayouts.DrawingView")) {
                    return;
                } else {
                    hitView.requestFocus();
                }

                hitView.performClick();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
