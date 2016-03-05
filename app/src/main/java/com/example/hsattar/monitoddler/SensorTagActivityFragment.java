package com.example.hsattar.monitoddler;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.firebase.client.Firebase;

/**
 * A placeholder fragment containing a simple view.
 */
@TargetApi(18)
public class SensorTagActivityFragment extends Fragment implements View.OnClickListener{

    public UUID UUID_IRT_SERV=UUID.fromString("f000aa00-0451-4000-b000-000000000000");
    public UUID UUID_IRT_DATA=UUID.fromString("f000aa01-0451-4000-b000-000000000000");
    public UUID UUID_IRT_CONF=UUID.fromString("f000aa02-0451-4000-b000-000000000000"); // 0: disable, 1: enable

    public UUID UUID_KEY_SERV=UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public UUID UUID_KEY_DATA=UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    //sensorTagGatt.java
    //new UUIDs for ACC https://e2e.ti.com/support/wireless_connectivity/bluetooth_low_energy/f/538/t/411571
    //more UUID stuff http://processors.wiki.ti.com/index.php/CC2650_SensorTag_User's_Guide#Data
    public UUID UUID_ACC_SERV = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
    public UUID UUID_ACC_DATA = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    public UUID UUID_ACC_CONF = UUID.fromString("f000aa82-0451-4000-b000-000000000000"); // 0: disable, 1: enable
    public UUID UUID_ACC_PERI = UUID.fromString("f000aa83-0451-4000-b000-000000000000"); // Period in tens of milliseconds

    public UUID CLIENT_CONFIG_DESCRIPTOR= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public String DeviceName="SensorTag";
    public static String currentText = "";
    public static String loggingText = "";
    public BluetoothAdapter BTAdapter;
    public BluetoothDevice BTDevice;
    public BluetoothGatt BTGatt;
    List<BluetoothGattService> serviceList;
    List <BluetoothGattCharacteristic> charList = new ArrayList<BluetoothGattCharacteristic>();

    private AsyncText task = null;
    private AsyncData taskData = null;
    public boolean scanning;
    public Handler handler;

    public View rootView;

    public SensorTagActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sensortag, container, false);

        ((Button)rootView.findViewById(R.id.scanbutton)).setOnClickListener(this);
        ((Button)rootView.findViewById(R.id.clearbutton)).setOnClickListener(this);
        clear();
        output("Turn on the Sensortag");

        BluetoothManager BTManager=(BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BTAdapter=BTManager.getAdapter();

        scanning=false;
        handler=new Handler();

        return rootView;
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.scanbutton:
                BTScan();
                break;
            case R.id.clearbutton:
                clear();
                break;
        }
    }

    @Override
    public void onPause() {
        if (SensorTagActivity.isLogging == true){
            SensorTagActivity.isLogging = false;
            try {
                SensorTagActivity.LogWriter.close();
                SensorTagActivity.fOut.close();
            }catch (Exception e) {
                message("Could not close file!");
            }
            message("logging ends!");
        }
        super.onPause();
        BTDevice=null;
        if (BTGatt != null)
        {
            BTGatt.disconnect();
            BTGatt.close();
        }
        BTGatt=null;
    }

    public void BTScan()
    {
        if(BTAdapter == null)
        {
            output("No Bluetooth Adapter");
            return;
        }

        if (!BTAdapter.isEnabled())
        {
            BTAdapter.enable();
        }

        if (scanning == false)
        {
            handler.postDelayed(new Runnable()
            {
                public void run()
                {
                    scanning=false;
                    BTAdapter.stopLeScan(DeviceLeScanCallback);
                    output("Stop scanning");
                }
            }, 5000);

            scanning=true;
            BTDevice=null;
            if (BTGatt != null)
            {
                BTGatt.disconnect();
                BTGatt.close();
            }
            BTGatt=null;

            boolean started = BTAdapter.startLeScan(DeviceLeScanCallback);
            if (started) {
                output("Start scanning");
            } else {
                output("There was a problem scanning...");
            }
        }
    }

    public BluetoothAdapter.LeScanCallback DeviceLeScanCallback=new BluetoothAdapter.LeScanCallback()
    {
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            if ((device == null) || (device.getName() == null)) {
                //output("No BT device found");
                return;
            }
            if (device.getName().contains(DeviceName))
            {
                if (BTDevice == null)
                {
                    BTDevice=device;
                    BTGatt=BTDevice.connectGatt(getContext(),false,GattCallback);
                }
                else
                {
                    if (BTDevice.getAddress().equals(device.getAddress()))
                    {
                        return;
                    }
                }
                output(device.getName() + ":" + device.getAddress() + ", rssi:" + rssi);
            }
        }
    };

    public BluetoothGattCallback GattCallback = new BluetoothGattCallback() {
        int ssstep = 0;

        public void SetupSensorStep(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            BluetoothGattDescriptor descriptor;

            charList.clear();
            switch (ssstep) {
                case 0: //when serviceDiscovered
                    serviceList = gatt.getServices();
                    if (serviceList.size() > 0) {
                        for (int ii = 0; ii < serviceList.size(); ii++) {
                            BluetoothGattService s = serviceList.get(ii);
                            List<BluetoothGattCharacteristic> c = s.getCharacteristics();
                            if (c.size() > 0) {
                                for (int jj = 0; jj < c.size(); jj++) {
                                    charList.add(c.get(jj));

                                }
                            }
                        }
                    }

                    output("Total characteristics " + charList.size());
                    //break;

                    //characteristic write for motion sensors
                    // bit 0-2 for gyro, 3-5 for acc, 6 for magneto
                    // 7 for wake on motion, 8-9 acc range (0=2G, 1=4G, 2=8G, 3=16G)
                    characteristic = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_CONF);
                    //acc only is 111000 bin = 38 hex = 56 dec
                    characteristic.setValue("56".getBytes());//new byte[] {1,1,1,1,1,1});
                    if (gatt.writeCharacteristic(characteristic) == false)
                    {
                        output("Could not enable ACC");
                    }
                    break;

                case 1: //descriptor write
                    // Enable local notifications
                    characteristic = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
                    if (gatt.setCharacteristicNotification(characteristic, true) == false)
                    {
                        output("Could not set ACC notification");
                    }
                    //Enable remote notifications
                    descriptor = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    break;
            }
            //increase the step
            ssstep++;
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                output("Connected to GATT Server");
                gatt.discoverServices();
            } else {
                output("Disconnected from GATT Server");
                gatt.close();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //gets called when discoverServices above
            output("Discover & Config GATT Services");
            ssstep = 0;
            SetupSensorStep(gatt);
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            SetupSensorStep(gatt);
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            SetupSensorStep(gatt);
        }

        //when sensor values gets updated, this gets hit
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (UUID_ACC_DATA.equals(characteristic.getUuid())) {
                outputData(characteristic.getValue());
            }
        }
    };

    public void message(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void output(String newText)
    {

        task=new AsyncText(rootView.getContext(),rootView.getRootView());
        task.execute(newText, currentText);
    }

    public void outputData(byte[] value)
    {

        taskData=new AsyncData(rootView.getContext(),rootView.getRootView(),value);
        taskData.execute();
    }

    public void clear()
    {
        currentText = "";
        TextView myTextView = (TextView) rootView.findViewById(R.id.textbox);
        myTextView.setText(currentText);
    }

}
