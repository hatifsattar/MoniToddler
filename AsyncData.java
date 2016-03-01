package com.example.hsattar.monitoddler;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Firebase;

// This AsyncTask is for data coming from sensortag
public class AsyncData extends AsyncTask<String, Void, String> {
    public Context context;
    private View rootView;
    private byte[] byteArray;
    private float[] accFloatArray;

    public AsyncData(Context context, View rootView, byte[] byteArray) {
        this.context = context;
        this.rootView = rootView;
        this.byteArray = byteArray;
    }

    @Override
    protected String doInBackground(String... params) {
        accFloatArray = convertAcc(byteArray);
        return "done";
    }

    @Override
    protected void onPostExecute(String currentText) {
        TextView accX = (TextView) rootView.findViewById(R.id.accX);
        TextView accY = (TextView) rootView.findViewById(R.id.accY);
        TextView accZ = (TextView) rootView.findViewById(R.id.accZ);
        accX.setText("X: " + accFloatArray[0]);
        accY.setText("Y: " + accFloatArray[1]);
        accZ.setText("Z: " + accFloatArray[2]);

        Firebase fb = MainActivity.ref.child("MT").child("patientX"");

        fb.child("HR").setValue(accFloatArray[0]);
        fb.child("TEMP").setValue(accFloatArray[1]);

    }

    public float[] convertAcc(final byte[] value) {
        /*
         * The accelerometer has the range [-2g, 2g] with unit (1/64)g.
         * To convert from unit (1/64)g to unit g we divide by 64.
         * (g = 9.81 m/s^2)
         * The z value is multiplied with -1 to coincide with how we have arbitrarily
         * defined the positive y direction. (illustrated by the apps accelerometer
         * image)
         */
        float[] accVals = new float[3];
        accVals[0] = convertTwoBytesToInt1 (value[6], value[7]) / (32768/4);
        accVals[1] = convertTwoBytesToInt1 (value[8], value[9]) / (32768/4);
        accVals[2] = convertTwoBytesToInt1 (value[10], value[11]) / (32768/4);

        return accVals;
    }

    public static float convertTwoBytesToInt1(byte b1, byte b2) {
        return (float) ((b2 << 8) | (b1 & 0xFF));
    }
}

