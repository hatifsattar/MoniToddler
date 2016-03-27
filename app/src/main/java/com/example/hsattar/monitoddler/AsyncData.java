package com.example.hsattar.monitoddler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.collection.LLRBNode;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.wahoofitness.connector.capabilities.Heartrate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

// This AsyncTask is for data coming from sensortag
public class AsyncData extends AsyncTask<String, Void, String> {
    public Context context;
    private View rootView;
    private byte[] byteArray;
    private Heartrate.Data hrData;
    //private static float[] accFloatArray = {0,0,0,0,0,0,0}; // Current Reading doesnt need to be saved
    private static float[] prevAccFloatArray = {0,0,0,0,0,0,0}; //Previous reading
    private float[] deltaPercent = {0,0,0,0,0,0,0};
    //private float[] delta = {0,0,0,0,0,0,0};
    //private float[] deltaCounter = {0,0,0,0,0,0,0};
    private static float delta = 0;
    private static int peakCounter = 0;
    private static int peakCounterMinute = 0;
    public static long sampling_counter = 0;
    public static long currentTime = 0;

    private static int hr_critical_count = 0;
    private static boolean patient_critical = false;

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
        currentTime = System.currentTimeMillis();
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
                + "\nAccDeltaX: " + deltaPercent[3]);

        accY.setText("Y: " + prevAccFloatArray[4] + "\ngyroY: " + prevAccFloatArray[1]
                + "\nAccDeltaY: " + deltaPercent[4]);

        accZ.setText("Z: " + prevAccFloatArray[5] + "\ngyroZ: " + prevAccFloatArray[2]
                + "\nAccDeltaZ: " + deltaPercent[5]);
        if (hrData != null) {
            HRText.setText("HR " + hrData.getHeartrate() + "\n AvgHR " + hrData.getAvgHeartrate()
                    + "Breaths/min " + peakCounterMinute + " Breaths " + peakCounter);
        }
        else {
            HRText.setText("Breaths/min " + peakCounterMinute + " Breaths " + peakCounter);
        }

        //graphing
        //make sure there are only a certain number of entries in the arraylist
        int chartEntries = 51;

        if (SensorTagActivityFragment.AccXEntries.size()>chartEntries)
            SensorTagActivityFragment.AccXEntries.remove(0);
        if (SensorTagActivityFragment.AccYEntries.size()>chartEntries)
            SensorTagActivityFragment.AccYEntries.remove(0);
        if (SensorTagActivityFragment.AccZEntries.size()>chartEntries)
            SensorTagActivityFragment.AccZEntries.remove(0);

        //add new entry to Yaxis
        SensorTagActivityFragment.AccXEntries.add(new Entry(prevAccFloatArray[3],SensorTagActivityFragment.TimeAxis));
        SensorTagActivityFragment.AccYEntries.add(new Entry(prevAccFloatArray[4],SensorTagActivityFragment.TimeAxis));
        SensorTagActivityFragment.AccZEntries.add(new Entry(prevAccFloatArray[5],SensorTagActivityFragment.TimeAxis));
        //fill the new dataset with all data
        Color mColour = new Color();
        LineDataSet datasetX = new LineDataSet(SensorTagActivityFragment.AccXEntries, "X");
        datasetX.setDrawCircles(false);
        datasetX.setDrawValues(false);
        datasetX.setLineWidth(4);
        datasetX.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        LineDataSet datasetY = new LineDataSet(SensorTagActivityFragment.AccYEntries, "Y");
        datasetY.setDrawCircles(false);
        datasetY.setDrawValues(false);
        datasetY.setLineWidth(4);
        datasetY.setColor(ColorTemplate.VORDIPLOM_COLORS[2]);
        LineDataSet datasetZ = new LineDataSet(SensorTagActivityFragment.AccZEntries, "Z");
        datasetZ.setDrawCircles(false);
        datasetZ.setDrawValues(false);
        datasetZ.setLineWidth(4);
        datasetZ.setColor(ColorTemplate.VORDIPLOM_COLORS[3]);
        //fill in the Xaxis labels
        SensorTagActivityFragment.labels.add(Integer.toString(SensorTagActivityFragment.TimeAxis));
        //find Line chart view
        LineChart Linechart1 = (LineChart) rootView.findViewById(R.id.Linechart1);
        //put together the xaxis and yaxis
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(datasetX);
        dataSets.add(datasetY);
        dataSets.add(datasetZ);
        LineData datas = new LineData(SensorTagActivityFragment.labels,dataSets);
        //zoom the Yaxis based on visible data (needs to happen before setting data)
        YAxis leftAxis = Linechart1.getAxisLeft();
        YAxis rightAxis = Linechart1.getAxisRight();
        leftAxis.resetAxisMaxValue();
        leftAxis.resetAxisMinValue();
        rightAxis.resetAxisMaxValue();
        rightAxis.resetAxisMinValue();
        //dont show Xaxis numbers
        XAxis xaxis = Linechart1.getXAxis();
        xaxis.setDrawLabels(false);
        //set up legend with white text
        Legend legend = Linechart1.getLegend();
        legend.setTextColor(Color.WHITE);
        //set data
        Linechart1.setDescription("");
        //Linechart1.setBackgroundColor(Color.WHITE); //defaults to transparent
        Linechart1.setDrawGridBackground(false);
        Linechart1.setData(datas);
        Linechart1.notifyDataSetChanged();
        Linechart1.invalidate();
        //force the graph to show the most recent values
        Linechart1.setVisibleXRangeMaximum(50);
        Linechart1.moveViewToX(SensorTagActivityFragment.TimeAxis);
        //update time
        SensorTagActivityFragment.TimeAxis++;

        if (SensorTagActivity.isLogging) {
            SensorTagActivityFragment.loggingText +=
                    "\n" + " X," + prevAccFloatArray[3] +
                            ",Y," + prevAccFloatArray[4] +
                            ",Z," + prevAccFloatArray[5] +
                            ",gyroX," + prevAccFloatArray[0] +
                            ",gyroY," + prevAccFloatArray[1] +
                            ",gyroZ," + prevAccFloatArray[2] +
                            ",AccDeltaX," + deltaPercent[3] +
                            ",AccDeltaY," + deltaPercent[4] +
                            ",AccDeltaZ," + deltaPercent[5];


            if (SensorTagActivityFragment.loggingText.length() > 150) {
                //add to file
                try {
                    SensorTagActivity.LogWriter.append(SensorTagActivityFragment.loggingText);
                } catch (Exception e) {
                    SensorTagActivityFragment.loggingText = "";
                }
                //clean up logging string
                SensorTagActivityFragment.loggingText = "";
            }
        }
        //Firebase fb = MainActivity.ref.child("MT").child("patientX");
        Firebase fb = SensorTagActivity.fb_ref.child(SensorTagActivity.patient_id);

        patient_critical = false;

        if (hrData != null) {
            //Write heart rate after cutting it to 5 significant numbers
            String hr_string = hrData.getHeartrate().toString();
            String hr_cut = hr_string.substring(10, hr_string.length());
            fb.child("HR").setValue(hr_cut);

            //Determining Critical
            //Obtain the number only
            String[] split_str = hr_cut.split("/");
            float hr_num = Float.parseFloat(split_str[0]);
            if ((hr_num > UpdateParams.UPPER_HR_PER_MIN_LIMIT) ||
                (hr_num < UpdateParams.LOWER_HR_PER_MIN_LIMIT)){
                //Patient is Critical
                hr_critical_count = hr_critical_count + 1;
            }
        }

        //Upload raw accelerometer stats to server
        fb.child("X-AXIS").setValue(String.format("%.5f",prevAccFloatArray[3]));
        fb.child("Y-AXIS").setValue(String.format("%.5f",prevAccFloatArray[4]));
        fb.child("Z-AXIS").setValue(String.format("%.5f",prevAccFloatArray[5]));

        //Calculate number of breaths (peaks) detected
        if (Math.abs(delta) >= UpdateParams.DELTA_RR) {//0.08
            peakCounter++;
            //reset the delta so the patient has to breathe again
            delta = 0;
        }

        // Sensortag takes ~6 readings/sec so this (36) is about 6 secs of time

        /*if (this.sampling_counter < currentTime) {
            this.sampling_counter = currentTime;
            peakCounter = 0;
        }*/

        if (currentTime >= this.sampling_counter + 10000 ) { //10 secs
            if (peakCounter > 0){
                fb.child("CRITICAL").setValue("No");
            } else {
                fb.child("CRITICAL").setValue("Yes");
            }

            //TODO - Use this
//            if ((peakCounter > 0) &&
//                (hr_critical_count == 0)) {
//                fb.child("CRITICAL").setValue("No");
//            } else {
//                fb.child("CRITICAL").setValue("Yes");
//            }

            this.sampling_counter = currentTime;
            peakCounterMinute = peakCounter*6;//display #br/min
            peakCounter = 0;
            hr_critical_count = 0;
        }

        //for displaying, per minute breaths

//            if (rr_critical || hr_critical){
//                fb.child("CRITICAL").setValue("Yes");
//            } else {
//                fb.child("CRITICAL").setValue("No");
//            }

        //send Resp Rate to firebase server
        //int resp_rate = peakCounter * 6;
        fb.child("RR").setValue(peakCounter);

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
            if (i == 5) { //z if standing/sitting, x if lying down
                if (sensortagValsFiltered[i] >= prevAccFloatArray[i]) {
                    delta = delta + (sensortagValsFiltered[i] - prevAccFloatArray[i]);
                } else {
                    delta = 0;
                }
            }
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

