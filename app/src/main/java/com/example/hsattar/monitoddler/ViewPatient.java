package com.example.hsattar.monitoddler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class ViewPatient extends AppCompatActivity {

    public final String TABLE_3 = "MT/patientX";
    public final String HEART_RATE_ENTRY = "HR";
    public final String CRITICAL_ENTRY = "CRITICAL";

    public String patient_name = "";
    Firebase fb_ref;
    Firebase fb_hr;
    Firebase fb_temp;
    Firebase fb_crit;
    Firebase fb_z_axis;
    ValueEventListener hr_listener;
    ValueEventListener temp_listener;
    ValueEventListener crit_listener;
    ValueEventListener ref_listener;
    ValueEventListener z_axis_listener;

    public TextView Name;
    public TextView Temp;
    public TextView HR;
    public TextView z_axis;
    public TextView Critical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);
        Firebase.setAndroidContext(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            patient_name = "patientX";
            fb_ref = MainActivity.ref.child("MT");
        }
        else{
            patient_name = bundle.getString("ID");
            fb_ref = MainActivity.ref.child("MT2");
        }

        Name = (TextView) findViewById(R.id.Name);
        Temp = (TextView) findViewById(R.id.temp);
        HR = (TextView) findViewById(R.id.heartrate);
        z_axis = (TextView) findViewById(R.id.z_axis);
        Critical = (TextView) findViewById(R.id.critical);

        firebase_setup();

    }

    private void firebase_setup() {

        Firebase head = fb_ref.child(patient_name);
        head.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("NAME").getValue(String.class);
                Name.setText(name);
//                for (DataSnapshot d : dataSnapshot.getChildren()) {
//                    if (d.getChildrenCount() < MainActivity.databse_fields_count) { return; }
//                    if ((d != null) && (d.getKey().matches("NAME"))) {
//                        String name = d.getValue(String.class);
//                        Name.setText(name);
//                    }
//                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        fb_hr = fb_ref.child(patient_name).child("HR");
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

        fb_temp = fb_ref.child(patient_name).child("TEMP");
        temp_listener = fb_temp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Temp.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        fb_z_axis = fb_ref.child(patient_name).child("Z-AXIS");
        z_axis_listener = fb_z_axis.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                z_axis.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        fb_crit = fb_ref.child(patient_name).child("CRITICAL");
        crit_listener = fb_crit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Critical.setText(value);
                if (value!=null) {
                    if (value.equals("Yes")) { //TODO null pointer here
                        Critical.setTextColor(Color.parseColor("#f2181b"));
                        //SendNotification(patient_name);
                    } else {
                        Critical.setTextColor(Color.parseColor("#04ea00"));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    private void SendNotification(String patient) {
        String title = "Monitoddler - Emergency!";
        String msg = "Emergency! Patient " + patient + " is Critical";

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.monitoddler_icon)
                        .setContentTitle(title)
                        .setContentText(msg);

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000});

        Intent resultIntent = new Intent(this,NotificationClass.class);
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
        if (crit_listener!=null) fb_crit.removeEventListener(crit_listener);
        if (hr_listener!=null) fb_hr.removeEventListener(hr_listener);
        if (temp_listener!=null) fb_temp.removeEventListener(temp_listener);
        if (z_axis_listener!=null) fb_z_axis.removeEventListener(z_axis_listener);
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
