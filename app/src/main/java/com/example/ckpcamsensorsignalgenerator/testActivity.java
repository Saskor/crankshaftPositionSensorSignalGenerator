package com.example.ckpcamsensorsignalgenerator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

public class testActivity extends AppCompatActivity {

    private static final int LAUNCH_TEST_RESULT = 103;
    //GUI Elems
    private TextView label;
    private EditText signalFrequencyEditText;
    private Button launchTestButton;
    private TextView bluetoothStatus;
    private Button connectButton;
    private ImageButton btOnButton;
    private ImageButton btOffButton;
    private TextView mReadBuffer;

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mShareEditor;

    private ArrayList<String[]> fronts;
    private String macAddress;
    private String adaptername;
    private BluetoothAdapter mBTAdapter;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private testActivity.ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private int allTeeth;
    private int teethMissing;
    private int ckpSignalIncrease;
    private int camSensorExist;
    private int sigOnFirstCKPTooth;
    private String jsonFronts;
    private int revolutionsPerMinut;
    String toBT ="";
    private String prescaler;
    private String remoteBTDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        //Define GUI Elems
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        connectButton = findViewById(R.id.connect);
        signalFrequencyEditText = findViewById(R.id.signalFrequencyEditText);
        launchTestButton = findViewById(R.id.launchTestButton);
        label = findViewById(R.id.signalFrequencyLabel);
        btOnButton = findViewById(R.id.btOn);
        btOffButton = findViewById(R.id.btOff);
        mReadBuffer = findViewById(R.id.mReadBuffer);

        mHandler = new Handler(){
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1) {
                        bluetoothStatus.setTextColor(Color.parseColor("#478e3a"));
                        bluetoothStatus.setText("Подсключено к: " + (String) (msg.obj));
                    } else {
                        bluetoothStatus.setTextColor(Color.RED);
                        bluetoothStatus.setText("Подключение не выполнено.");
                    }
                }
            }
        };

        //Get Shared Preferences
        mSharedPreferences = getSharedPreferences("ckpSignalGenerator.SHARED_PREF", Context.MODE_PRIVATE);
        mShareEditor = mSharedPreferences.edit();

        //Get Values From Shared Preferences
        macAddress = mSharedPreferences.getString("macAddress","0");
        adaptername = mSharedPreferences.getString("adaptername","0");
        allTeeth = getIntegerFromSharedPref("allTeeth");
        teethMissing = getIntegerFromSharedPref("teethMissing");
        ckpSignalIncrease = Integer.parseInt(mSharedPreferences.getString("signalIncrease", "0"));
        camSensorExist = Integer.parseInt(mSharedPreferences.getString("camSensorExist", "0"));
        sigOnFirstCKPTooth = Integer.parseInt(mSharedPreferences.getString("sigOnFirstCKPTooth", "0"));

        jsonFronts = mSharedPreferences.getString("jsonFronts","");
        ArrToIntent a = new Gson().fromJson(jsonFronts, ArrToIntent.class);
        if (a != null) {
            fronts = a.fronts;
        }

        if (mBTAdapter.isEnabled()){
            bluetoothStatus.setText("Bluetooth на Вашем устройстве включен." );
        } else {
            bluetoothStatus.setText("Bluetooth на Вашем устройстве выключен." );
        }
        //ConnectToBTDevice On Activity Created
        connectToBTDevice();

        btOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOn();
            }
        });

        btOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOff();
            }
        });
        //Manual ConnectToBTDevice
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToBTDevice();
            }
        });
        //Send string to BT Device (Arduino)
        launchTestButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 signalGenerateConditions();
             }
         });
    }

    @Override
    protected void onDestroy() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();

        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home buttonArrToIntent a = (ArrToIntent) data.getParcelableExtra("ArrToIntent");
            // fronts = a.fronts;
            case android.R.id.home:
                setActivityResult();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (mConnectedThread!=null) {
            mConnectedThread.cancel();
        }
        super.onPause();
    }

    private void setActivityResult() {
        mShareEditor.putString("bluetoothStatusText", bluetoothStatus.getText().toString());
        Intent resultIntent = new Intent(testActivity.this, ConfigActivity.class);
        startActivity(resultIntent);
        finish();
    }

    private int getIntegerFromSharedPref(String sharedPrefMemberName) {
        String getString = mSharedPreferences.getString(sharedPrefMemberName, "0");
        if (getString.matches("")) {
            return 0;
        } else {
            return Integer.parseInt(getString);
        }
    }

    private void signalGenerateConditions() {
        boolean validFlag = true;
        //ckpActivity Inputs Validation
        if (allTeeth == 0 || teethMissing == 0) {
            validFlag = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(testActivity.this);
            builder
                    .setTitle("Внимание, незаполненные текстовые поля сигнала ДПКВ.")
                    .setMessage("Желаете перейти на экран настроек сигнала ДПКВ?")
                    .setCancelable(true)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int flag) {
                            startActivity(new Intent(testActivity.this, setCKPSIgnalActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton("Нет",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        //camActivity Inputs Validation
        if(HelperMethods.getText(signalFrequencyEditText).matches("")) {
            validFlag = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(testActivity.this);
            builder
                    .setTitle("Внимание, незаполненные текстовые поля.")
                    .setMessage("Вы не указали частоту генерирования сигнала." +
                            "\n\nПожалуйста, укажите частоту генерирования сигнала.")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int flag) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        //Check is enabled BT on your Device
        if(!mBTAdapter.isEnabled()) {
            validFlag = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(testActivity.this);
            builder
                    .setTitle("Внимание, Bluetooth на Вашем устройтсве не включен.")
                    .setMessage("Включить Bluetooth?")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int flag) {
                            //Enable BT on your Device
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            bluetoothStatus.setText("Bluetooth на Вашем устройстве включен");
                            HelperMethods.showToast("Bluetooth включен", testActivity.this);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        if (validFlag) {
            writeStringToBT();
        }
    }

    private void writeStringToBT() {
        if(mConnectedThread != null) { //First check to make sure thread created
            String toBT = makeStringToWriteToBT();
            mConnectedThread.write(toBT);
            showToast(toBT,  testActivity.this);
        }
    }

    private String makeStringToWriteToBT() {
        String toBT;
        revolutionsPerMinut = Integer.parseInt(HelperMethods.getText(signalFrequencyEditText));
        prescaler = Integer.toString(48000000 / (revolutionsPerMinut) / 60 * allTeeth * 2);
        String counterResetValue = Integer.toString(allTeeth * 2);
        String first = Integer.toString((allTeeth * 2) - (teethMissing + 1) * 2);
        String second = Integer.toString((allTeeth * 2) - teethMissing);
        String third = Integer.toString(allTeeth * 2 - (teethMissing + 1));
        String fourth = Integer.toString(allTeeth * 2 + 1);
        toBT = "<" + prescaler + "," + counterResetValue + "," + first + "," + second + "," + third + "," + fourth + ">";
        return toBT;
    }

    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            bluetoothStatus.setText("Bluetooth включен");
            HelperMethods.showToast("Bluetooth включен", testActivity.this);

        }
        else{
            HelperMethods.showToast("Bluetooth уже включен", testActivity.this);
        }
    }

    private void bluetoothOff(){
        mBTAdapter.disable(); // turn off
        bluetoothStatus.setText("Bluetooth выключен");

        HelperMethods.showToast("Bluetooth выключен", testActivity.this);
    }

    public static void showToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0 ,0);
        toast.show();
    }
    //To connect to remote BT device (arduino in my case)
    private void connectToBTDevice() {
        new Thread()
        {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = mBTAdapter.getRemoteDevice(macAddress);

                try {
                    mBTSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                } catch (IOException e) {
                    fail = true;
                    showToast("Не получилось создать соединение", testActivity.this);
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        showToast("Не получилось создать соединение", testActivity.this);
                    }
                }
                if(!fail) {
                    mConnectedThread = new testActivity.ConnectedThread(mBTSocket);
                    mConnectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, adaptername)
                            .sendToTarget();
                }
            }
        }.start();
    }
    //Thread to send and read Data from BT Device (arduino in my case)
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[2048];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        //SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();// Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                if (mConnectedThread!=null){
                    mmSocket.close();
                    mmInStream.close();
                    mmOutStream.close();
                    mConnectedThread = null;
                }
            } catch (IOException e) { }
        }
    }
}
