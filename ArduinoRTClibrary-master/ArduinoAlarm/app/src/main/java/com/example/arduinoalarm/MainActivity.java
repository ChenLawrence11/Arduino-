package com.example.arduinoalarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;
import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    public Button buttonConnect;
    public Toolbar toolbar;
    public ProgressBar progressBar;
    public Button morningTimeBtn;
    public Button afternoonTimeBtn;
    public Button eveningTimeBtn;

    public int hour,minute;

    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
        buttonConnect = findViewById(R.id.buttonConnect);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        morningTimeBtn = findViewById(R.id.morningTimeButton);
        afternoonTimeBtn = findViewById(R.id.afternoonTimeButton);
        eveningTimeBtn = findViewById(R.id.eveningTimeButton);
        morningTimeBtn.setEnabled(false);
        afternoonTimeBtn.setEnabled(false);
        eveningTimeBtn.setEnabled(false);
        progressBar.setVisibility(View.GONE);

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if(deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, getApplicationContext());
            createConnectThread.start();
        }

        // Second most important piece of Code. GUI Handler

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                morningTimeBtn.setEnabled(true);
                                afternoonTimeBtn.setEnabled(true);
                                eveningTimeBtn.setEnabled(true);
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                break;
                        }
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Move to adapter list
                Intent intent = new Intent(MainActivity.this,SelectDeviceActivity.class);
                startActivity(intent);
            }
        });




    }

    public void popMorningTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                String cmdText = null;
                int h = selectedHour;
                int m = selectedMinute;
                morningTimeBtn.setText(String.format(Locale.getDefault(),"%02d:%02d",h,m));
                cmdText = "M" + String.format(Locale.getDefault(),"%02d:%02d",h,m);
                connectedThread.write(cmdText);
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    public void popAfternoonTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute){
                String cmdText = null;
                int h = selectedHour;
                int m = selectedMinute;
                afternoonTimeBtn.setText(String.format(Locale.getDefault(),"%02d:%02d",h,m));
                cmdText = "A" + String.format(Locale.getDefault(),"%02d:%02d",h,m);
                connectedThread.write(cmdText);
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }


    public void popEveningTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute){
                String cmdText = null;
                int h = selectedHour;
                int m = selectedMinute;
                eveningTimeBtn.setText(String.format(Locale.getDefault(),"%02d:%02d",h,m));
                cmdText = "E" + String.format(Locale.getDefault(),"%02d:%02d",h,m);
                connectedThread.write(cmdText);
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }



    // Thread to Create Bluetooth Connection
    public static class CreateConnectThread extends Thread{
        Context tContext;

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, Context context){
            tContext = context;

            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            if(ActivityCompat.checkSelfPermission(tContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){

            }
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try{
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            }catch(IOException e){
                Log.d(TAG,"Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run(){
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(ActivityCompat.checkSelfPermission(tContext,Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){

            }
            bluetoothAdapter.cancelDiscovery();
            try{
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS,1,-1).sendToTarget();
            }catch(IOException connectException){
                // Unable to connect; close the socket and return.
                try{
                    mmSocket.close();
                    Log.e("Status","Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS,-1,-1).sendToTarget();
                }catch(IOException closeException){
                    Log.e(TAG,"Could not close the client socket", closeException);
                }
                return;

                //
            }

            connectedThread = new ConnectedThread(mmSocket);
        }

        // Closes the client socket and causes the thread to finish
        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e){
                Log.e(TAG,"Could not close the client socket", e);
            }
        }
    }

    // Thread for Data Transfer
    public static class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because member streams are final
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch(IOException e){}

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

//        public void run(){
//            byte[] buffer = new byte[1024];
//            int bytes = 0; // bytes returned from read()
//            // Keep listening to the InputStream until an excpetion occurs
//            while(true){
//                try{
//                    // Read from the InputStream from Arduino until termination character is reached. Then send the whole String message to GUI Handler.
//                    buffer[bytes] = (byte)mmInStream.read();
//                    String readMessage;
//                    if(buffer[bytes] == '\n'){
//                        readMessage = new String(buffer,0,bytes);
//                        Log.e("Arduino Message",readMessage);
//                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
//                        bytes = 0;
//                    }else{
//                        bytes++;
//                    }
//                }catch(IOException e){
//                    e.printStackTrace();
//                    break;
//                }
//            }
//        }

        // Call this from the main activity to send data to the remote device
        public void write(String input){
            byte[] bytes = input.getBytes();
            try{
                mmOutStream.write(bytes);
            }catch(IOException e){
                Log.e("Send Error","Unable to send message",e);
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e){}{}
        }
    }
}