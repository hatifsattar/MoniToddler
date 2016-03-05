package com.example.hsattar.monitoddler;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

// This AsyncTask is for data coming from sensortag
public class AsyncData extends AsyncTask<String, Void, String> {
    public Context context;
    private View rootView;
    private byte[] byteArray;
    private static float[] accFloatArray = {0,0,0}; // Current Reading
    private static float[] prevAccFloatArray = {0,0,0}; //Previous reading
    private float deltaPercent = 0;
    private float cumDelta = 0;

    // Low Pass Filter
    private static double alpha = 0.5;



    public AsyncData(Context context, View rootView, byte[] byteArray) {
        this.context = context;
        this.rootView = rootView;
        this.byteArray = byteArray;
    }

    @Override
    protected String doInBackground(String... params) {
        accFloatArray = convertAcc(byteArray);
        prevAccFloatArray = accFloatArray;
        return "done";
    }

    @Override
    protected void onPostExecute(String currentText) {
        TextView accX = (TextView) rootView.findViewById(R.id.accX);
        TextView accY = (TextView) rootView.findViewById(R.id.accY);
        TextView accZ = (TextView) rootView.findViewById(R.id.accZ);
        accX.setText("X: " + accFloatArray[0]);
        accY.setText("Y: " + accFloatArray[1]);
        accZ.setText("deltaX: " + deltaPercent);


        if (SensorTagActivity.isLogging){
            SensorTagActivityFragment.loggingText += "\n" + " X: " + accFloatArray[0] + " Y: "
                    + accFloatArray[1] + " deltaX: " + deltaPercent;


            if (SensorTagActivityFragment.loggingText.length() > 100){
                //add to file
                try {
                    SensorTagActivity.LogWriter.append(SensorTagActivityFragment.loggingText);
                }catch (Exception e) {
                    SensorTagActivityFragment.loggingText = "";
                }
                //clean up logging string
                SensorTagActivityFragment.loggingText = "";
            }
        }
        Firebase fb = MainActivity.ref.child("MT").child("patientX");

        fb.child("HR").setValue(String.format("%.5f",accFloatArray[0]));
        fb.child("TEMP").setValue(String.format("%.5f",accFloatArray[1]));
        fb.child("Z-AXIS").setValue(String.format("%.5f",accFloatArray[2]));


        MainActivity.counter++;

        if (MainActivity.counter == 50) {//20
            MainActivity.counter = 0;
            float delta = cumDelta*20;//8
            if (delta < 2) { // Crude detection
                fb.child("CRITICAL").setValue("Yes");
                //fb.child("DELTA").setValue(delta);
            } else {
                fb.child("CRITICAL").setValue("No");
            }
            fb.child("DELTA").setValue(delta);
        }

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


        float[] accValsFiltered = new float[3];
        accValsFiltered[0] = lowPassFilter(accVals[0], prevAccFloatArray[0]);
        accValsFiltered[1] = lowPassFilter(accVals[1], prevAccFloatArray[1]);
        accValsFiltered[2] = lowPassFilter(accVals[2], prevAccFloatArray[2]);

        //x axis only
        //deltaPercent = (accVals[0] - accFloatArray[0])/accFloatArray[0];
        deltaPercent = (accVals[0] - accValsFiltered[0])/accValsFiltered[0]; // Using Filtered
        cumDelta += Math.abs(deltaPercent);

        return accValsFiltered;//accVals;
    }

    public static float convertTwoBytesToInt1(byte b1, byte b2) {
        return (float) ((b2 << 8) | (b1 & 0xFF));
    }

    public static float lowPassFilter (float raw, float prev){
        double filtered = alpha*raw + (1 - alpha)*prev;
        return (float)filtered;
    }
}

