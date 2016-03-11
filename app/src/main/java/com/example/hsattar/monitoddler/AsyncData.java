package com.example.hsattar.monitoddler;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.wahoofitness.connector.capabilities.Heartrate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

// This AsyncTask is for data coming from sensortag
public class AsyncData extends AsyncTask<String, Void, String> {
    public Context context;
    private View rootView;
    private byte[] byteArray;
    private Heartrate.Data hrData;
    //private static float[] accFloatArray = {0,0,0,0,0,0,0}; // Current Reading doesnt need to be saved
    private static float[] prevAccFloatArray = {0,0,0,0,0,0,0}; //Previous reading
    private float[] deltaPercent = {0,0,0,0,0,0,0};

    // Low Pass Filter
    private static double alpha = 0.5;



    public AsyncData(Context context, View rootView, byte[] byteArray, Heartrate.Data hrData) {
        this.context = context;
        this.rootView = rootView;
        this.byteArray = byteArray;
        this.hrData = hrData;
    }

    @Override
    protected String doInBackground(String... params) {
        if (byteArray != null) {
            prevAccFloatArray = convertAcc(byteArray);
        }
        //prevAccFloatArray = accFloatArray;
        return "done";
    }

    @Override
    protected void onPostExecute(String currentText) {
        TextView accX = (TextView) rootView.findViewById(R.id.accX);
        TextView accY = (TextView) rootView.findViewById(R.id.accY);
        TextView accZ = (TextView) rootView.findViewById(R.id.accZ);
        TextView HRText = (TextView) rootView.findViewById(R.id.heartrateText);
        accX.setText("X: " + prevAccFloatArray[3] + "\ngyroX: " + prevAccFloatArray[0]
                +"\nAccDeltaX: " + deltaPercent[3]);
        accY.setText("Y: " + prevAccFloatArray[4] + "\ngyroY: " + prevAccFloatArray[1]
                +"\nAccDeltaY: " + deltaPercent[4]);
        accZ.setText("Z: " + prevAccFloatArray[5] + "\ngyroZ: " + prevAccFloatArray[2]
                +"\nAccDeltaZ: " + deltaPercent[5]);
        if (hrData != null)
            HRText.setText("HR " + hrData.getHeartrate() + "\n AvgHR " + hrData.getAvgHeartrate());

        if (SensorTagActivity.isLogging){
            SensorTagActivityFragment.loggingText +=
                    "\n" + " X: " + prevAccFloatArray[3] +
                           " Y: " + prevAccFloatArray[4] +
                           " Z: " + prevAccFloatArray[5] +
                            " gyroX: " + prevAccFloatArray[0] +
                            " gyroY: " + prevAccFloatArray[1] +
                            " gyroZ: " + prevAccFloatArray[2] +
                            " AccDeltaX: " + deltaPercent[3] +
                            " AccDeltaY: " + deltaPercent[4] +
                            " AccDeltaZ: " + deltaPercent[5];


            if (SensorTagActivityFragment.loggingText.length() > 150){
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
        //Firebase fb = MainActivity.ref.child("MT").child("patientX");
        Firebase fb = SensorTagActivity.fb_ref.child(SensorTagActivity.patient_id);

        fb.child("HR").setValue(String.format("%.5f",prevAccFloatArray[0]));
        fb.child("TEMP").setValue(String.format("%.5f",prevAccFloatArray[1]));
        fb.child("Z-AXIS").setValue(String.format("%.5f",prevAccFloatArray[2]));


        MainActivity.sampling_counter++;

        if (MainActivity.sampling_counter == 50) {//20
            MainActivity.sampling_counter = 0;
            //xaxis acc only! TODO add all three
            float delta = deltaPercent[3]*20;//8
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
        float[] sensortagVals = new float[7];
        //gyro
        sensortagVals[0] = convertTwoBytesToInt1 (value[0], value[1]) / (65536 / 500);
        sensortagVals[1] = convertTwoBytesToInt1 (value[2], value[3]) / (65536 / 500);
        sensortagVals[2] = convertTwoBytesToInt1 (value[4], value[5]) / (65536 / 500);
        //acc
        sensortagVals[3] = convertTwoBytesToInt1 (value[6], value[7]) / (32768/4);
        sensortagVals[4] = convertTwoBytesToInt1 (value[8], value[9]) / (32768/4);
        sensortagVals[5] = convertTwoBytesToInt1 (value[10], value[11]) / (32768/4);


        float[] sensortagValsFiltered = new float[7];
        //gyro, acc, and deltas
        for (int i = 0; i<sensortagValsFiltered.length; i++) {
            sensortagValsFiltered[i] = lowPassFilter(sensortagVals[i], prevAccFloatArray[i]);
            // Using Filtered previous value
            deltaPercent[i] = (sensortagValsFiltered[i] - prevAccFloatArray[i])/prevAccFloatArray[i];
            //deltaPercent = (sensortagVals[3] - sensortagValsFiltered[3])/sensortagValsFiltered[3];
            //deltaPercent[i] += Math.abs(deltaPercent[i]);
        }

        return sensortagValsFiltered;
    }

    public static float convertTwoBytesToInt1(byte b1, byte b2) {
        return (float) ((b2 << 8) | (b1 & 0xFF));
    }

    public static float lowPassFilter (float raw, float prev){
        double filtered = alpha*raw + (1 - alpha)*prev;
        return (float)filtered;
    }
}

