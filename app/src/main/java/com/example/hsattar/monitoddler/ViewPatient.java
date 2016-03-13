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

    public String patient_id = "";
    public String patient_name = "";
    Firebase fb_ref;
    Firebase fb_hr;
    //Firebase fb_temp;
    Firebase fb_crit;
    Firebase fb_x_axis;
    Firebase fb_y_axis;
    Firebase fb_z_axis;
    Firebase fb_rr;
    ValueEventListener hr_listener;
    //ValueEventListener temp_listener;
    ValueEventListener crit_listener;
    ValueEventListener ref_listener;
    ValueEventListener x_axis_listener;
    ValueEventListener y_axis_listener;
    ValueEventListener z_axis_listener;
    ValueEventListener rr_listener;

    public TextView Name;
    //public TextView Temp;
    public TextView HR;
    public TextView RR;
    public TextView x_axis;
    public TextView y_axis;
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
        x_axis = (TextView) findViewById(R.id.x_axis);
        y_axis = (TextView) findViewById(R.id.temp);
        z_axis = (TextView) findViewById(R.id.z_axis);
        Critical = (TextView) findViewById(R.id.critical);

        firebase_setup();

    }

    private void firebase_setup() {

        Firebase head = fb_ref.child(patient_id);
        head.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("NAME").getValue(String.class);
                Name.setText(name);
                patient_name = name;
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

        //Heart Rate
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

        //Resp Rate
        fb_rr = fb_ref.child(patient_id).child("RR");
        rr_listener = fb_rr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                RR.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        fb_x_axis = fb_ref.child(patient_id).child("X-AXIS");
        x_axis_listener = fb_x_axis.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                x_axis.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) { }
        });

        fb_y_axis = fb_ref.child(patient_id).child("Y-AXIS");
        y_axis_listener = fb_y_axis.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                y_axis.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) { }
        });

        fb_z_axis = fb_ref.child(patient_id).child("Z-AXIS");
        z_axis_listener = fb_z_axis.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                z_axis.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) { }
        });

        fb_crit = fb_ref.child(patient_id).child("CRITICAL");
        crit_listener = fb_crit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Critical.setText(value);
                if (value!=null) {
                    if (value.equals("Yes")) { // null pointer here if value == null
                        Critical.setTextColor(Color.parseColor("#f2181b"));
                        if (MainActivity.EMERGENCY_NOTIFICATION_ENABLE == 1) {
                            SendNotification(patient_name);
                        }
                    } else {
                        Critical.setTextColor(Color.parseColor("#04ea00"));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {  }
        });

    }

    private void SendNotification(String patient) {
        String title = "Monitoddler - Emergency!";
        String msg = "Emergency! Patient " + patient + " is Critical";

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.mt_icon_round)
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
//        if ((MainActivity.EMERGENCY_NOTIFICATION_ENABLE == 0) &&
//                (crit_listener!=null)) {
//            fb_crit.removeEventListener(crit_listener); // Need to keep this listener ON when Notifications are enabled
//        }
        if (hr_listener!=null) fb_hr.removeEventListener(hr_listener);
        if (rr_listener!=null) fb_rr.removeEventListener(rr_listener);
        if (x_axis_listener!=null) fb_x_axis.removeEventListener(x_axis_listener);
        if (y_axis_listener!=null) fb_y_axis.removeEventListener(y_axis_listener);
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
