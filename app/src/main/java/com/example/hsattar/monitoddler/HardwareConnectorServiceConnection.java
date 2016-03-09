package com.example.hsattar.monitoddler;

        import java.util.ArrayList;
        import java.util.Collection;

        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ServiceConnection;
        import android.os.IBinder;

        import com.wahoofitness.connector.HardwareConnector;
        import com.wahoofitness.connector.conn.connections.SensorConnection;
        import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
        import com.example.hsattar.monitoddler.HardwareConnectorService.HardwareConnectorServiceBinder;

public class HardwareConnectorServiceConnection {

    public interface Listener {

        void onHardwareConnectorServiceConnected(HardwareConnectorService hardwareConnectorService);

        void onHardwareConnectorServiceDisconnected();

    }

    private final Context mContext;
    private HardwareConnectorService mHardwareConnectorService = null;
    private final Listener mListener;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            HardwareConnectorServiceBinder hardwareConnectorServiceBinder = (HardwareConnectorServiceBinder) binder;
            HardwareConnectorService hardwareConnectorService = hardwareConnectorServiceBinder
                    .getService();
            mHardwareConnectorService = hardwareConnectorService;
            mListener.onHardwareConnectorServiceConnected(mHardwareConnectorService);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mHardwareConnectorService = null;
            mListener.onHardwareConnectorServiceDisconnected();
        }
    };

    public HardwareConnectorServiceConnection(Context context, Listener listener) {
        mContext = context;
        mListener = listener;

        // mContext.startService(new Intent(mContext, HardwareConnectorService.class));

        Intent intent = new Intent(mContext, HardwareConnectorService.class);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public SensorConnection connectSensor(ConnectionParams params) {
        if (mHardwareConnectorService != null) {
            return mHardwareConnectorService.connectSensor(params);
        } else {
            return null;
        }
    }

    public void disconnectSensor(ConnectionParams params) {
        if (mHardwareConnectorService != null) {
            mHardwareConnectorService.disconnectSensor(params);
        }

    }

    public boolean enableDiscovery(boolean enable) {
        if (mHardwareConnectorService != null) {
            return mHardwareConnectorService.enableDiscovery(enable);

        } else {
            return false;
        }
    }

    public Collection<ConnectionParams> getDiscoveredConnectionParams() {
        if (mHardwareConnectorService != null) {
            return mHardwareConnectorService.getDiscoveredConnectionParams();
        } else {
            return new ArrayList<ConnectionParams>();
        }
    }

    public HardwareConnector getHardwareConnector() {
        if (mHardwareConnectorService != null) {
            return mHardwareConnectorService.getHardwareConnector();

        } else {
            return null;
        }
    }

    public HardwareConnectorService getHardwareConnectorService() {
        return mHardwareConnectorService;
    }

    public SensorConnection getSensorConnection(ConnectionParams params) {
        if (mHardwareConnectorService != null) {
            return mHardwareConnectorService.getSensorConnection(params);
        } else {
            return null;
        }
    }

    public Collection<SensorConnection> getSensorConnections() {
        if (mHardwareConnectorService != null) {
            return mHardwareConnectorService.getSensorConnections();
        } else {
            return new ArrayList<SensorConnection>();
        }
    }

    public boolean isBound() {
        return (mHardwareConnectorService != null)
                && (mHardwareConnectorService.getHardwareConnector() != null);
    }

    public boolean isDiscovering() {
        if (mHardwareConnectorService != null) {
            return mHardwareConnectorService.isDiscovering();

        } else {
            return false;
        }
    }

    public void unbind() {
        mContext.unbindService(mServiceConnection);

    }
}
