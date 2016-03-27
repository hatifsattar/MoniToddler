package com.example.hsattar.monitoddler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ViewPatient extends AppCompatActivity {

    public String patient_id = "";
    public String patient_name = "";
    Firebase fb_ref;
    Firebase fb_note;
    Firebase fb_main;
    Firebase fb_crit;

    ValueEventListener note_listener;
    ValueEventListener crit_listener;
    ValueEventListener ref_listener;

    Firebase fb_x_axis;
    Firebase fb_y_axis;
    Firebase fb_z_axis;
    Firebase fb_rr;
    //ValueEventListener temp_listener;
    ValueEventListener x_axis_listener;
    ValueEventListener y_axis_listener;
    ValueEventListener z_axis_listener;
    ValueEventListener rr_listener;

    public TextView Name;
    public TextView NOTE;
    public TextView HR;
    public TextView RR;
    public TextView x_axis;
    public TextView y_axis;
    public TextView z_axis;
    public TextView Critical;
    public TextView BP;
    public TextView SAT;
    public TextView TEMP;
    public TextView LastUpdate;

    public Button add_vitals;

    //graphing
    public static ArrayList<Entry> AccXEntries = new ArrayList<>();
    public static ArrayList<Entry> AccYEntries = new ArrayList<>();
    public static ArrayList<Entry> AccZEntries = new ArrayList<>();
    public static int TimeAxis = 0;
    public static ArrayList<String> labels = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);
        Firebase.setAndroidContext(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            patient_id = "patientX";
            fb_ref = MainActivity.ref.child("MT");
        }
        else{
            patient_id = bundle.getString("ID");
            fb_ref = MainActivity.ref.child("MT2");
        }

        Name = (TextView) findViewById(R.id.Name);
        HR = (TextView) findViewById(R.id.heartrate);
        RR = (TextView) findViewById(R.id.resprate);
        BP = (TextView) findViewById(R.id.bloodpressure);
        TEMP = (TextView) findViewById(R.id.temperature);
        SAT = (TextView) findViewById(R.id.saturation);
        NOTE = (TextView) findViewById(R.id.note);
        x_axis = (TextView) findViewById(R.id.x_axis);
        y_axis = (TextView) findViewById(R.id.temp);
        z_axis = (TextView) findViewById(R.id.z_axis);
        Critical = (TextView) findViewById(R.id.critical);
        LastUpdate = (TextView) findViewById(R.id.lastUpdateTime);
        add_vitals = (Button) findViewById(R.id.buttonAddVitals);
        add_vitals.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                add_vitals_activity();
            }
        });

        firebase_setup();

        //clear all ArrayLists to be safe
        AccXEntries.clear();
        AccYEntries.clear();
        AccZEntries.clear();
        labels.clear();
        TimeAxis = 0;

    }

    private void add_vitals_activity() {
        Intent myIntent = new Intent(this, AddExtraVitals.class);
        myIntent.putExtra("NAME", patient_name);
        myIntent.putExtra("ID", patient_id);
        startActivity(myIntent);
    }

    private void firebase_setup() {

        Firebase head = fb_ref.child(patient_id);
        head.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("NAME").getValue(String.class);
                Name.setText(name);
                patient_name = name;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        fb_main = fb_ref.child(patient_id);
        ref_listener = fb_main.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() < (long) MainActivity.databse_fields_count) {
                    return;
                }

                if (dataSnapshot != null) {

                    //String name = d.child("NAME").getValue().toString();
                    //String id = d.getKey().toString();
                    //String doctor = d.child("DOCTOR").getValue().toString();
                    //String critical = d.child("CRITICAL").getValue().toString();
                    //String age = d.child("AGE").getValue().toString();
                    String hr = dataSnapshot.child("HR").getValue(String.class);
                    String temp = dataSnapshot.child("TEMP").getValue(String.class);
                    String rr = dataSnapshot.child("RR").getValue(String.class);
                    String sat = dataSnapshot.child("SAT").getValue(String.class);
                    String bp = dataSnapshot.child("BP").getValue(String.class);
                    String x_a = dataSnapshot.child("X-AXIS").getValue(String.class);
                    String y_a = dataSnapshot.child("Y-AXIS").getValue(String.class);
                    String z_a = dataSnapshot.child("Z-AXIS").getValue(String.class);
                    String note = dataSnapshot.child("NOTE").getValue(String.class);
                    String update_time = dataSnapshot.child("UPDATE_TIME").getValue(String.class);

                    HR.setText(hr);
                    RR.setText(rr);
                    BP.setText(bp);
                    SAT.setText(sat);
                    TEMP.setText(temp);
                    x_axis.setText(x_a);
                    y_axis.setText(y_a);
                    z_axis.setText(z_a);
                    NOTE.setText("Note:\n" + note);
                    LastUpdate.setText("Last update: " + update_time);

                    //graphing
                    //make sure there are only a certain number of entries in the arraylist
                    int chartEntries = 75;

                    if (AccXEntries.size()>chartEntries)
                        AccXEntries.remove(0);
                    if (AccYEntries.size()>chartEntries)
                        AccYEntries.remove(0);
                    if (AccZEntries.size()>chartEntries)
                        AccZEntries.remove(0);

                    //add new entry to Yaxis
                    AccXEntries.add(new Entry(Float.parseFloat(x_a),TimeAxis));
                    AccYEntries.add(new Entry(Float.parseFloat(y_a),TimeAxis));
                    AccZEntries.add(new Entry(Float.parseFloat(z_a),TimeAxis));
                    //fill the new dataset with all data
                    Color mColour = new Color();
                    LineDataSet datasetX = new LineDataSet(AccXEntries, "X");
                    datasetX.setDrawCircles(false);
                    datasetX.setDrawValues(false);
                    datasetX.setLineWidth(4);
                    datasetX.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
                    LineDataSet datasetY = new LineDataSet(AccYEntries, "Y");
                    datasetY.setDrawCircles(false);
                    datasetY.setDrawValues(false);
                    datasetY.setLineWidth(4);
                    datasetY.setColor(ColorTemplate.VORDIPLOM_COLORS[2]);
                    LineDataSet datasetZ = new LineDataSet(AccZEntries, "Z");
                    datasetZ.setDrawCircles(false);
                    datasetZ.setDrawValues(false);
                    datasetZ.setLineWidth(4);
                    datasetZ.setColor(ColorTemplate.VORDIPLOM_COLORS[3]);
                    //fill in the Xaxis labels
                    labels.add(Integer.toString(TimeAxis));
                    //find Line chart view
                    LineChart Linechart1 = (LineChart) findViewById(R.id.LinechartView);
                    //put together the xaxis and yaxis
                    ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                    dataSets.add(datasetX);
                    dataSets.add(datasetY);
                    dataSets.add(datasetZ);
                    LineData datas = new LineData(labels,dataSets);
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
                    Linechart1.moveViewToX(TimeAxis);
                    //update time
                    TimeAxis++;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

 /*       //Heart Rate
        fb_hr = fb_ref.child(patient_id).child("HR");
        hr_listener = fb_hr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                HR.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
*/
        fb_crit = fb_ref.child(patient_id).child("CRITICAL");
        crit_listener = fb_crit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                //Critical.setText(value);
                if (value!=null) {
                    if (value.equals("Yes")) { // null pointer here if value == null
                        Critical.setText("Patient Critical");
                        Critical.setTextColor(Color.parseColor("#f2181b"));
                        if (MainActivity.EMERGENCY_NOTIFICATION_ENABLE == 1) {
                            SendNotification(patient_name, true);
                        }
                    } else {
                        Critical.setText("Patient Healthy");
                        Critical.setTextColor(Color.parseColor("#04ea00"));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {  }
        });

        fb_note = fb_ref.child(patient_id).child("NOTE");
        note_listener = fb_note.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                NOTE.setText(value);
                if (value!=null) {
                    if (MainActivity.EMERGENCY_NOTIFICATION_ENABLE == 1) {
                        SendNotification(patient_name, false);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {  }
        });
    }

    private void SendNotification(String patient, boolean critical) {
        String title = "Monitoddler - EMERGENCY!";
        String msg = "Patient " + patient.toUpperCase() + " is Critical";
        String msg_2 = "MoniToddler has detected an Emergency. \n" +
                "Please go to the main app and check on patient " + patient;

        if (critical == false){
            title = "Monitoddler - UPDATE!";
            msg = "New Note for Patient " + patient.toUpperCase();
            msg_2 = "A new Note has been posted. \n" +
                    "Please go to the main app and check on patient " + patient;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.mt_icon_round)
                        .setContentTitle(title)
                        .setContentText(msg);

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000});

        Intent resultIntent = new Intent(this,NotificationClass.class);
        resultIntent.putExtra("MSG", msg_2);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    @Override
    protected void onPause() {
        super.onPause();//TODO
        //fb_ref.goOffline();
        if (ref_listener!=null) fb_ref.removeEventListener(ref_listener);
//        if ((MainActivity.EMERGENCY_NOTIFICATION_ENABLE == 0) &&
//                (crit_listener!=null)) {
//            fb_crit.removeEventListener(crit_listener); // Need to keep this listener ON when Notifications are enabled
//        }
//        if (hr_listener!=null) fb_hr.removeEventListener(hr_listener);
//        if (rr_listener!=null) fb_rr.removeEventListener(rr_listener);
//        if (x_axis_listener!=null) fb_x_axis.removeEventListener(x_axis_listener);
//        if (y_axis_listener!=null) fb_y_axis.removeEventListener(y_axis_listener);
//        if (z_axis_listener!=null) fb_z_axis.removeEventListener(z_axis_listener);

        //clear all ArrayLists
        AccXEntries.clear();
        AccYEntries.clear();
        AccZEntries.clear();
        labels.clear();
        TimeAxis = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
//        fb_ref.removeEventListener(ref_listener);
//        fb_crit.removeEventListener(crit_listener);
//        fb_hr.removeEventListener(hr_listener);
//        fb_temp.removeEventListener(temp_listener);
//        fb_z_axis.removeEventListener(z_axis_listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //fb_ref.goOnline();

//        fb_ref.addValueEventListener(ref_listener);
//        fb_crit.addValueEventListener(crit_listener);
//        fb_hr.addValueEventListener(hr_listener);
//        fb_temp.addValueEventListener(temp_listener);
//        fb_z_axis.addValueEventListener(z_axis_listener);
    }
}
