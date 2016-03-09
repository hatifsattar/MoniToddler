package com.example.hsattar.monitoddler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.wahoofitness.common.log.Logger;
import com.wahoofitness.connector.HardwareConnector;
import com.wahoofitness.connector.HardwareConnectorEnums.HardwareConnectorState;
import com.wahoofitness.connector.HardwareConnectorEnums.SensorConnectionError;
import com.wahoofitness.connector.HardwareConnectorEnums.SensorConnectionState;
import com.wahoofitness.connector.HardwareConnectorTypes.NetworkType;
import com.wahoofitness.connector.HardwareConnectorTypes.SensorType;
import com.wahoofitness.connector.capabilities.Capability.CapabilityType;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
import com.wahoofitness.connector.listeners.discovery.DiscoveryListener;

/**
 * Implementation of a {@link Service} encapsulating and abstracting the Wahoo API's
 * {@link HardwareConnector}.
 */
public class HardwareConnectorService extends Service {

    public class HardwareConnectorServiceBinder extends Binder {

        public HardwareConnectorService getService() {
            return HardwareConnectorService.this;
        }
    }

    public interface Listener {

        void onDeviceDiscovered(ConnectionParams params);

        void onDiscoveredDeviceLost(ConnectionParams params);

        void onDiscoveredDeviceRssiChanged(ConnectionParams params);

        void onFirmwareUpdateRequired(SensorConnection sensorConnection,
                                      String currentVersionNumber, String recommendedVersion);

        void onNewCapabilityDetected(SensorConnection sensorConnection,
                                     CapabilityType capabilityType);

        void onSensorConnectionStateChanged(SensorConnection sensorConnection,
                                            SensorConnectionState state);

    }

    static {
        Logger.setLogLevel(Log.VERBOSE);
    }
    private final IBinder mBinder = new HardwareConnectorServiceBinder();

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {

        @Override
        public void onDeviceDiscovered(ConnectionParams params) {
            for (Listener listener : mListeners) {
                listener.onDeviceDiscovered(params);
            }
        }

        @Override
        public void onDiscoveredDeviceLost(ConnectionParams params) {
            for (Listener listener : mListeners) {
                listener.onDiscoveredDeviceLost(params);
            }
        }

        @Override
        public void onDiscoveredDeviceRssiChanged(ConnectionParams params, int rssi) {
            for (Listener listener : mListeners) {
                listener.onDiscoveredDeviceRssiChanged(params);
            }
        }
    };

    private HardwareConnector mHardwareConnector;

    private final HardwareConnector.Callback mHardwareConnectorCallback = new HardwareConnector.Callback() {

        @Override
        public void connectedSensor(SensorConnection arg0) {

        }

        @Override
        public void connectorStateChanged(NetworkType arg0, HardwareConnectorState arg1) {

        }

        @Override
        public void disconnectedSensor(SensorConnection arg0) {

        }

        @Override
        public void hasData() {

        }

        @Override
        public void onFirmwareUpdateRequired(SensorConnection sensorConnection,
                                             String currentVersionNumber, String recommendedVersion) {
            for (Listener listener : mListeners) {
                listener.onFirmwareUpdateRequired(sensorConnection, currentVersionNumber,
                        recommendedVersion);
            }
        }
    };

    private final Set<Listener> mListeners = new HashSet<Listener>();
    private final SensorConnection.Listener mSensorConnectionListener = new SensorConnection.Listener() {

        @Override
        public void onNewCapabilityDetected(SensorConnection sensorConnection,
                                            CapabilityType capabilityType) {
            for (Listener listener : mListeners) {
                listener.onNewCapabilityDetected(sensorConnection, capabilityType);
            }
        }

        @Override
        public void onSensorConnectionError(SensorConnection sensorConnection,
                                            SensorConnectionError error) {

        }

        @Override
        public void onSensorConnectionStateChanged(SensorConnection sensorConnection,
                                                   SensorConnectionState state) {
            for (Listener listener : mListeners) {
                listener.onSensorConnectionStateChanged(sensorConnection, state);
            }
        }
    };

    public HardwareConnectorService() {
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public SensorConnection connectSensor(ConnectionParams params) {
        if (mHardwareConnector != null) {
            return mHardwareConnector.requestSensorConnection(params,
                    this.mSensorConnectionListener);
        } else {
            return null;
        }
    }

    public void disconnectSensor(ConnectionParams params) {
        if (mHardwareConnector != null) {
            SensorConnection sensorConnection = mHardwareConnector.getSensorConnection(params);
            if (sensorConnection != null) {
                sensorConnection.disconnect();
            }
        }
    }

    public boolean enableDiscovery(boolean enable) {
        if (mHardwareConnector != null) {
            if (enable) {
                mHardwareConnector.startDiscovery(SensorType.NONE, NetworkType.UNSPECIFIED,
                        mDiscoveryListener);
            } else {
                mHardwareConnector.stopDiscovery(NetworkType.UNSPECIFIED);
            }
            return true;
        } else {
            return false;
        }
    }

    public Collection<ConnectionParams> getDiscoveredConnectionParams() {
        if (mHardwareConnector != null) {
            return mHardwareConnector.getDiscoveredConnectionParams(NetworkType.UNSPECIFIED,
                    SensorType.NONE);
        } else {
            return new ArrayList<ConnectionParams>();
        }
    }

    public HardwareConnector getHardwareConnector() {
        return mHardwareConnector;
    }

    public SensorConnection getSensorConnection(ConnectionParams params) {
        if (mHardwareConnector != null) {
            return mHardwareConnector.getSensorConnection(params);
        } else {
            return null;
        }
    }

    public SensorConnection.Listener getSensorConnectionListener() {
        return this.mSensorConnectionListener;
    }

    public Collection<SensorConnection> getSensorConnections() {
        if (mHardwareConnector != null) {
            return mHardwareConnector.getSensorConnections(SensorType.NONE);
        } else {
            return new ArrayList<SensorConnection>();
        }
    }

    public boolean isDiscovering() {
        if (mHardwareConnector != null) {
            return mHardwareConnector.isDiscovering(NetworkType.UNSPECIFIED);
        } else {
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHardwareConnector = new HardwareConnector(this, mHardwareConnectorCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHardwareConnector.stopDiscovery(NetworkType.UNSPECIFIED);
        mHardwareConnector.shutdown();
        mHardwareConnector = null;
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }
}
