package com.aps490.drdc.prototype;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;


public class ModuleSelection extends AppCompatActivity implements Communications {

    //private final UUID MY_UUID = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb");
    private final UUID MY_UUID = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
    private final String NAME = "Dicks";
    private final String samsungMAC = "BC:72:B1:A8:A2:B2";
    private BluetoothDevice mmDevice;

    private android.os.Handler mHandler;
    BluetoothAdapter mBluetoothAdapter;
    AcceptThread mAcceptThread;
    ConnectedThread mConnectedThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_selection);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String message_username = intent.getStringExtra(LoginActivity.USER_NAME);
        String message_password = intent.getStringExtra(LoginActivity.PASSWORD);

        mHandler = new android.os.Handler(Looper.getMainLooper()) {
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

    }

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

    @Override
    protected void onResume() {

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        super.onResume();
    }

    private void turnOnBluetooth() {
        // check if this device supports bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Your device does not support bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Your device supports bluetooth", Toast.LENGTH_SHORT).show();
        }

        // turn bluetooth on if off
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, RESULT_OK);
        }
    }


    public void openMainPage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, LoginActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }


    public void openSubAssemblyPage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, listView.class);
        startActivity(intent);
    }

    private class AcceptThread extends Thread {

        // socket listening to incoming connection
        private BluetoothServerSocket mmServerSocket = null;
        private InputStream mmInputStream;


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
            while (mmSocket == null) {
                try {
                    mmSocket = mmServerSocket.accept();

                } catch (IOException e) {
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
                mmServerSocket.close();
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
            String heartRate;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    /* TODO: Only send in buffer[0:bytes] to string, to avoid garbage from buffer.
                        Another thing we could try is clear the buffer array with '\0'
                     */
                    heartRate = new String(buffer);

                    //TODO: Send this heart rate to a TextView on main display activity!
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
        mHandler.sendMessage(msg);

    }


}
