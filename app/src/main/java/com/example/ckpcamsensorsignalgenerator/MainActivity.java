package com.example.ckpcamsensorsignalgenerator;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private String macAddress;
    private String adaptername;
    private String btStatusFromShPref;
    // GUI Components
    private TextView mBluetoothStatus;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;

    private Button mConfigureButton;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mShareEditor;


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int ACTION_REQUEST_MULTIPLE_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get Bluetooth Permissions From User
        int pCheck = this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (pCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACTION_REQUEST_MULTIPLE_PERMISSION);        }
        //GUI Elems
        mBluetoothStatus = findViewById(R.id.bluetoothStatus);
        mScanBtn = findViewById(R.id.scan);
        mOffBtn = findViewById(R.id.off);
        mDiscoverBtn = findViewById(R.id.discover);
        mListPairedDevicesBtn = findViewById(R.id.PairedBtn);
        mConfigureButton = findViewById(R.id.configure);
        //List Adapter For Represent List Of Bluetooth Devices
        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        //Get Shared Pref
        mSharedPreferences = getSharedPreferences("ckpSignalGenerator.SHARED_PREF", Context.MODE_PRIVATE);
        mShareEditor = mSharedPreferences.edit();


        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth не найден");
            HelperMethods.showToast("Bluetooth не найден", MainActivity.this);
        }
        else {

            if (mBTAdapter.isEnabled()){
                mBluetoothStatus.setText("Bluetooth на Вашем устройстве включен." );
            } else {
                mBluetoothStatus.setText("Bluetooth на Вашем устройстве выключен." );
            }
            //Buttons ClickListeners 
            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });
            //BT On Button Action
            mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    bluetoothOff(v);
                }
            });
            //Paired Button Action
            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });
            //Discover Button Action
            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });

            mConfigureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveActivityState();
                    Intent configIntent = new Intent(MainActivity.this, ConfigActivity.class);
                    startActivity(configIntent);
                }
            });
        }
        registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth включен");
            HelperMethods.showToast("Bluetooth включен", MainActivity.this);

        }
        else{
            HelperMethods.showToast("Bluetooth уже включен", MainActivity.this);
        }
    }

    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth выключен");

        HelperMethods.showToast("Bluetooth выключен", MainActivity.this);
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            HelperMethods.showToast("Процесс обнаружения Bluetooth устройств, остановлен", MainActivity.this);
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                HelperMethods.showToast("Запущен процесс обнаружения Bluetooth устройств", MainActivity.this);
            }
            else{
                HelperMethods.showToast("Bluetooth не включен", MainActivity.this);
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };



    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            mBTArrayAdapter.clear();
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            HelperMethods.showToast("Отображение Bluetooth устройств, спаренных с вашим устройством", MainActivity.this);
        }
        else
        HelperMethods.showToast("Bluetooth не включен", MainActivity.this);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                HelperMethods.showToast("Bluetooth не включен", MainActivity.this);
                return;
            }

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);
            macAddress = address;
            adaptername = name;

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setMessage("Вы выбрали устроойство с названием: " + adaptername + " и MAC адресом: " + macAddress)
                    .setCancelable(true)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int flag) {
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
            // Spawn a new thread to avoid blocking the GUI one

        }
    };

    private  void saveActivityState() {
        if (macAddress != null) {
            mShareEditor.putString("macAddress", macAddress);
        }

        if (adaptername != null) {
            mShareEditor.putString("adaptername", adaptername);
        }
        mShareEditor.apply();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(blReceiver);
        super.onDestroy();
    }
}
