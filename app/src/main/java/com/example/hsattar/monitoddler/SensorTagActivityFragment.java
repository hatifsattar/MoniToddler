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

import com.example.hsattar.monitoddler.HardwareConnectorService;

import com.firebase.client.Firebase;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;

/**
 * A placeholder fragment containing a simple view.
 */
@TargetApi(18)
public class SensorTagActivityFragment extends HardwareConnectorFragment
        implements View.OnClickListener {

    public UUID UUID_IRT_SERV=UUID.fromString("f000aa00-0451-4000-b000-000000000000");
    public UUID UUID_IRT_DATA=UUID.fromString("f000aa01-0451-4000-b000-000000000000");
    public UUID UUID_IRT_CONF=UUID.fromString("f000aa02-0451-4000-b000-000000000000"); // 0: disable, 1: enable

    public UUID UUID_KEY_SERV=UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public UUID UUID_KEY_DATA=UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    //sensorTagGatt.java
    //new UUIDs for ACC https://e2e.ti.com/support/wireless_connectivity/bluetooth_low_energy/f/538/t/411571
    //more UUID stuff http://processors.wiki.ti.com/index.php/CC2650_SensorTag_User's_Guide#Data
    // wahoo android api http://api.wahoofitness.com/android/api/1.4.2.5/WahooAndroidAPIUsersGuide.pdf
    public UUID UUID_ACC_SERV = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
    public UUID UUID_ACC_DATA = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    public UUID UUID_ACC_CONF = UUID.fromString("f000aa82-0451-4000-b000-000000000000"); // 0: disable, 1: enable
    public UUID UUID_ACC_PERI = UUID.fromString("f000aa83-0451-4000-b000-000000000000"); // Period in tens of milliseconds

    public UUID CLIENT_CONFIG_DESCRIPTOR= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public String DeviceName="SensorTag";
    public String DeviceNameHR="TICKR";
    public static String currentText = "";
    public static String loggingText = "";
    public BluetoothAdapter BTAdapter;
    public BluetoothDevice BTDevice;
    public BluetoothDevice BTDeviceHR;
    public BluetoothGatt BTGatt;
    private boolean showedConnectedMsg[] = {false,false}; //sensortag,wahoo tickr
    List<BluetoothGattService> serviceList;
    List <BluetoothGattCharacteristic> charList = new ArrayList<BluetoothGattCharacteristic>();
    List <BluetoothGattDescriptor> descList = new ArrayList<BluetoothGattDescriptor>();

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
        output("Turn on Bluetooth Device(s) and press Scan");

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
        BTDeviceHR=null;
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
                    //wahoo tickr
                    enableDiscovery(false);
                    // end wahoo tickr
                    output("Stop scanning");
                }
            }, 5000);

            scanning=true;
            //enable one msg per scan if device connected already
            showedConnectedMsg[0] = false;
            showedConnectedMsg[1] = false;
            BTDevice=null;
            if (BTGatt != null)
            {
                BTGatt.disconnect();
                BTGatt.close();
            }
            BTGatt=null;

            boolean started = BTAdapter.startLeScan(DeviceLeScanCallback);
            //wahoo tickr
            enableDiscovery(true);
            // end wahoo tickr
            if (started) {
                output("Start scanning");
            } else {
                output("Enabling Bluetooth...");
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

            // sensortag
            if (device.getName().contains(DeviceName))
            {
                if (BTDevice == null) {
                    BTDevice=device;
                    BTGatt=BTDevice.connectGatt(getContext(),false,GattCallback);
                }
                else {
                    if (BTDevice.getAddress().equals(device.getAddress())) {
                        if (!showedConnectedMsg[0]) {
                            output("Sensortag already connected!");
                            showedConnectedMsg[0] = true;
                        }
                        return;
                    }
                }
                output(device.getName() + ":" + device.getAddress() + ", rssi:" + rssi);
            }
            // wahoo tickr
            else if (device.getName().contains(DeviceNameHR))
            {
                if (BTDeviceHR == null) {
                    BTDeviceHR=device;
                    BTGatt=BTDeviceHR.connectGatt(getContext(),false,GattCallback);
                }
                else {
                    if (BTDeviceHR.getAddress().equals(device.getAddress())) {
                        if (!showedConnectedMsg[1]) {
                            output("Wahoo Tickr already connected!");
                            showedConnectedMsg[1] = true;
                        }
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

            switch (ssstep) {
                case 0: //when serviceDiscovered
                    charList.clear();
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

                    //for sensortag
                    if (gatt.getDevice().getName().contains(DeviceName)) {
                        //characteristic write for motion sensors
                        // bit 0-2 for gyro, 3-5 for acc, 6 for magneto
                        // 7 for wake on motion, 8-9 acc range (0=2G, 1=4G, 2=8G, 3=16G)
                        characteristic = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_CONF);
                        //acc gyro mag is 1111111=0x7F
                        byte b[] = new byte[] {0x7F,0x00};
                        characteristic.setValue(b);
                        if (gatt.writeCharacteristic(characteristic) == false) {
                            output("Could not enable ACC");
                        }
                    }
                    else if (gatt.getDevice().getName().contains(DeviceNameHR)) {
                        //for wahoo tickr
                        for (int i = 0;i<charList.size();i++)
                        {
                            charList.get(i).setValue("56".getBytes());
                            if (gatt.writeCharacteristic(charList.get(i)) == false) {
                                output("Could not enable wahoo char "+i);
                            }
                        }
                    }
                    break;

                case 1: //descriptor write
                    //for sensortag
                    if (gatt.getDevice().getName().contains(DeviceName)) {
                        // Enable local notifications
                        characteristic = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
                        if (gatt.setCharacteristicNotification(characteristic, true) == false) {
                            output("Could not set ACC notification");
                        }
                        //Enable remote notifications
                        descriptor = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                    else if (gatt.getDevice().getName().contains(DeviceNameHR)) {
                        // wahoo tickr
                        // Enable local notifications
                        for (int i = 0;i<charList.size();i++)
                        {
                            if (gatt.setCharacteristicNotification(charList.get(i), true) == false) {
                                output("Could not set tickr notify "+i);
                            }

                            descList= charList.get(i).getDescriptors();

                            //Enable remote notifications
                            for (int ii=0;ii<descList.size();ii++)
                            {
                                descList.get(ii).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descList.get(ii));
                            }
                        }
                    }
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
        //disconnect
        BTDevice=null;
        BTDeviceHR=null;
        if (BTGatt != null)
        {
            BTGatt.disconnect();
            BTGatt.close();
        }
        BTGatt=null;
    }

    //wahoo tickr stuff

}
