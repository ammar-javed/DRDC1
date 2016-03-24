package com.aps490.drdc.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
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
import android.widget.Toast;

import com.aps490.drdc.LeapMotion.LeapActionReceiver;
import com.aps490.drdc.LeapMotion.LeapTapEventReceiver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import java.io.IOException;
import java.util.UUID;

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

    //private final UUID MY_UUID = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb");
    private final UUID MY_UUID = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
    private final String NAME = "Band";
    private final String samsungMAC = "BC:72:B1:A8:A2:B2";
    private BluetoothDevice mmDevice;

    private android.os.Handler bandHandler;
    BluetoothAdapter mBluetoothAdapter;
    AcceptThread mAcceptThread;
    ConnectedThread mConnectedThread;

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

    BroadcastReceiver mBandReceiver;
    IntentFilter mBandFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        returnIntent = new Intent(this, listView.class);

        bandHandler = new android.os.Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.arg1) {
                    case Constants.MESSAGE_TOAST:
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.BLUETOOTH_CONNECTION_RESULT), Toast.LENGTH_LONG).show();
                }

            }
        };

        turnOnBluetooth();

        mmDevice = mBluetoothAdapter.getRemoteDevice(samsungMAC);
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();


        System.out.println("Creating instructions activity.");
        String assemblyName = intent.getStringExtra("name");
        task = intent.getIntExtra("task", 0);
        System.out.println("Acquired intent values.");

        setContentView(R.layout.activity_instructions);

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
        mLeapTapFilter = new IntentFilter(Constants.LEAP_INTERACT_RELEVANT_VIEW);
        mLeapTapFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mLeapTapReceiver = new InstructionsLeapTapEventReceiver(this);

        registerReceiver(mLeapTapReceiver, mLeapTapFilter);

        mBandFilter = new IntentFilter(Constants.MS_BAND_EVENT);
        mBandFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mBandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // MS_BAND_UPDATE_HEARTRATE
                String action = intent.getStringExtra("bandAction");

                switch (action) {
                    case Constants.MS_BAND_UPDATE_HEARTRATE:
                        String heartRate = intent.getStringExtra("hr");
                        updateHeartRate(heartRate);
                        break;
                    default:
                        break;
                }

            }
        };

        registerReceiver(mBandReceiver, mBandFilter);

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

        if(mAcceptThread != null)   {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }


        if(mConnectedThread != null)    {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

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

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mAcceptThread != null)   {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }


        if(mConnectedThread != null)    {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private void turnOnBluetooth() {
        // check if this device supports bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Your device does not support bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Your device supports bluetooth", Toast.LENGTH_SHORT).show();
            Log.i(Constants.TAG, "mBluetoothAdapter not null.");
        }

        // turn bluetooth on if off
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, RESULT_OK);
        }

    }



    public void getPreInstruction() {
        Instruction instruction = assembly.getPreviousInstr();
        if(instruction!=null)
            currentInstr = instruction;

        ((TextView) findViewById(R.id.textViewInst)).setText(currentInstr.getText());
        updateProgress();
    }

    public void getNextInstruction() {
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

    public void updateHeartRate( String heartRate ) {
        System.out.println("Updating heart rate to: " + heartRate);
        TextView hrText = (TextView) findViewById(R.id.textViewHeartRate);
        int hr = Integer.parseInt(heartRate);

        if (hr > 75) {
            hrText.setTextColor(getResources().getColor(R.color.rust));
        } else {
            hrText.setTextColor(getResources().getColor(R.color.darkblue));
        }


        hrText.setText(
                "Heart Rate: " + heartRate);
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
            builder.setMessage( R.string.finishPopUp)
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

    void goBack(){
        this.finish();
    }

    public class InstructionsLeapTapEventReceiver extends BroadcastReceiver {

        private Context mContext;

        private long lastSwipe = SystemClock.uptimeMillis();

        public InstructionsLeapTapEventReceiver(Context context) {
            super();
            mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getStringExtra("leapAction");
            Boolean closed;

            switch (action) {
                case Constants.LEAP_TAP_RELEVANT_VIEW:

                    closed = checkAndCloseDialogs();

                    if (!closed)
                        simulateTapOnView(context, intent);

                    break;
                case Constants.LEAP_SWIPE_RELEVANT_VIEW:
                    long timestamp = SystemClock.uptimeMillis();

                    if ((timestamp - lastSwipe) < 1000 ) {
                        break;
                    }
                    String direction = intent.getStringExtra("swipeDirection");

                    if (direction.equals(Constants.LEAP_SWIPE_RIGHT)) {

                        if (finishedDialog != null) {
                            finishedDialog.dismiss();
                        }


                        getPreInstruction();
                    } else if (direction.equals(Constants.LEAP_SWIPE_LEFT)){

                        if (finishedDialog != null) {
                            returnIntent = new Intent(mContext, listView.class);
                            startActivity(returnIntent);
                        }

                        getNextInstruction();
                    }

                    lastSwipe = timestamp;

                    break;
                case Constants.LEAP_CIRCLE_RELEVANT_VIEW:

                    closed = checkAndCloseDialogs();

                    if (!closed)
                        goBack();

                    break;
                default:
                    break;
            }
        }

        private Boolean checkAndCloseDialogs(){

            if (builder != null) {
                builder.dismiss();
                return true;
            } else if (finishedDialog != null) {
                finishedDialog.dismiss();
                return true;
            }
            return false;
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

    private class AcceptThread extends Thread {

        // socket listening to incoming connection
        private BluetoothServerSocket mmServerSocket = null;
        private InputStream mmInputStream;
        private boolean running = false;


        // constructor
        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            Log.i(Constants.TAG, "ACCEPTTHREAD");
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                //tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mmDevice.getName(), MY_UUID);
                Log.i(Constants.TAG, "listenUsingRfcommWithServiceRecord returned");
                running = true;
            } catch (IOException e) {
                Log.e(Constants.TAG, "mmServerSocket failed", e);
            }
            mmServerSocket = tmp;
            Log.i(Constants.TAG, "mmServerSocket is " + mmServerSocket);
        }

        // executed when the thread.start() is executed
        public void run() {

            BluetoothSocket mmSocket = null;
            Log.i(Constants.TAG, "AcceptThread run()");
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            // Accept an incoming socket connection
            // Keep listening until a socket is returned or exception occurs
            while (running) {
                try {
                    mmSocket = mmServerSocket.accept();
                    running = false;

                } catch (Exception e) {
                    Log.e(Constants.TAG, "IOException: accept() failed " + e);
                    break;
                }
                /**
                 if(mmSocket != null)   {
                 try {
                 mmServerSocket.close();
                 } catch (Exception e) {

                 }
                 break;
                 }
                 */
            }

            // notify user
            toastConnectionResult(mmServerSocket != null);

            if (mmSocket != null) {
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
            }

        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                if (mmServerSocket != null) {
                    mmServerSocket.close();
                }
            } catch (IOException e) {

            }
        }


    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(Constants.TAG, "create ConnectedThread: ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(Constants.TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            Log.i(Constants.TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            String heartRate= "";
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    /* TODO: Only send in buffer[0:bytes] to string, to avoid garbage from buffer.
                        Another thing we could try is clear the buffer array with '\0'
                     */
                    System.out.println("Attempting to load Heart Rate Buffer" );
                    heartRate = "";

                    for( int i =0; i<bytes; i++ )
                        heartRate = heartRate + (char) buffer[i];

                    Intent hrIntent = new Intent();
                    hrIntent.setAction(Constants.MS_BAND_EVENT);
                    hrIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    hrIntent.putExtra("bandAction", Constants.MS_BAND_UPDATE_HEARTRATE);
                    hrIntent.putExtra("hr", heartRate);
                    sendBroadcast(hrIntent);

                    // Send the obtained bytes to the UI Activity
                    Log.i(Constants.TAG, heartRate);

                } catch (IOException e) {
                    Log.e(Constants.TAG, "disconnected", e);
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * create toast message the result of bluetooth connection attemp
     *
     * @param result true: success, false: failure
     */
    private void toastConnectionResult(boolean result) {

//        Log.i(TAG, "toastConnectionResult");
        Message msg = Message.obtain();
        msg.arg1 = Constants.MESSAGE_TOAST;
        Bundle bundle = new Bundle();
        if (result) {
            bundle.putString(Constants.BLUETOOTH_CONNECTION_RESULT, "Bluetooth Connected");
        } else {
            bundle.putString(Constants.BLUETOOTH_CONNECTION_RESULT, "Bluetooth Connection Failed");
        }
        msg.setData(bundle);
        bandHandler.sendMessage(msg);

    }


}
