package com.example.hsattar.monitoddler;


import java.util.ArrayList;
import java.util.Collection;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wahoofitness.connector.HardwareConnectorEnums.SensorConnectionState;
import com.wahoofitness.connector.capabilities.Capability.CapabilityType;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
import com.example.hsattar.monitoddler.HardwareConnectorService;
import com.example.hsattar.monitoddler.HardwareConnectorServiceConnection;

public abstract class HardwareConnectorFragment extends Fragment implements
        HardwareConnectorService.Listener, HardwareConnectorServiceConnection.Listener {

    private HardwareConnectorServiceConnection mHardwareConnectorServiceConnection;

    private final HardwareConnectorServiceConnection.Listener mHardwareConnectorServiceConnectionListener = new HardwareConnectorServiceConnection.Listener() {

        @Override
        public void onHardwareConnectorServiceConnected(
                HardwareConnectorService hardwareConnectorService) {
            hardwareConnectorService.addListener(HardwareConnectorFragment.this);
            HardwareConnectorFragment.this
                    .onHardwareConnectorServiceConnected(hardwareConnectorService);

        }

        @Override
        public void onHardwareConnectorServiceDisconnected() {
            HardwareConnectorFragment.this.onHardwareConnectorServiceDisconnected();

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHardwareConnectorServiceConnection = new HardwareConnectorServiceConnection(getActivity(),
                mHardwareConnectorServiceConnectionListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Deregister the service listener
        HardwareConnectorService hardwareConnectorService = mHardwareConnectorServiceConnection
                .getHardwareConnectorService();
        if (hardwareConnectorService != null) {
            hardwareConnectorService.removeListener(this);
        }
        mHardwareConnectorServiceConnection.unbind();
    }

    /** Override this */
    public void onDeviceDiscovered(ConnectionParams params) {

    }

    /** Override this */
    public void onDiscoveredDeviceLost(ConnectionParams params) {

    }

    /** Override this */
    public void onDiscoveredDeviceRssiChanged(ConnectionParams params) {

    }

    @Override
    public void onHardwareConnectorServiceConnected(
            HardwareConnectorService hardwareConnectorService) {
        hardwareConnectorService.addListener(this);
    }

    /** Override this */
    public void onHardwareConnectorServiceDisconnected() {
    }

    public void onNewCapabilityDetected(SensorConnection sensorConnection,
                                        CapabilityType capabilityType) {

    }

    public void onSensorConnectionStateChanged(SensorConnection sensorConnection,
                                               SensorConnectionState state) {

    }

    protected SensorConnection connectSensor(ConnectionParams params) {
        if (mHardwareConnectorServiceConnection != null) {
            return mHardwareConnectorServiceConnection.connectSensor(params);
        } else {
            return null;
        }

    }

    protected void disconnectSensor(ConnectionParams params) {
        if (mHardwareConnectorServiceConnection != null) {
            mHardwareConnectorServiceConnection.disconnectSensor(params);
        }

    }

    protected boolean enableDiscovery(boolean enable) {
        if (mHardwareConnectorServiceConnection != null) {
            return mHardwareConnectorServiceConnection.enableDiscovery(enable);
        } else {
            return false;
        }

    }

    protected Collection<ConnectionParams> getDiscoveredConnectionParams() {
        if (mHardwareConnectorServiceConnection != null) {
            return mHardwareConnectorServiceConnection.getDiscoveredConnectionParams();
        } else {
            return new ArrayList<ConnectionParams>();
        }
    }

    protected SensorConnection getSensorConnection(ConnectionParams params) {
        if (mHardwareConnectorServiceConnection != null) {
            return mHardwareConnectorServiceConnection.getSensorConnection(params);
        } else {
            return null;
        }
    }

    protected Collection<SensorConnection> getSensorConnections() {
        if (mHardwareConnectorServiceConnection != null) {
            return mHardwareConnectorServiceConnection.getSensorConnections();
        } else {
            return new ArrayList<SensorConnection>();
        }
    }

    boolean isDiscovering() {
        if (mHardwareConnectorServiceConnection != null) {
            return mHardwareConnectorServiceConnection.isDiscovering();
        } else {
            return false;
        }
    }

    @Override
    public void onFirmwareUpdateRequired(SensorConnection sensorConnection,
                                         String currentVersionNumber, String recommendedVersion) {

    }

}

