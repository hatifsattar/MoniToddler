package com.example.hsattar.monitoddler;

import android.graphics.Color;
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

        patient_name = "patientX";

        Name = (TextView) findViewById(R.id.Name);
        Temp = (TextView) findViewById(R.id.temp);
        HR = (TextView) findViewById(R.id.heartrate);
        z_axis = (TextView) findViewById(R.id.z_axis);
        Critical = (TextView) findViewById(R.id.critical);

        Name.setText("Patient X");

        fire_base_setup();

    }

    private void fire_base_setup() {

        Firebase fb_hr = MainActivity.ref.child("MT").child(patient_name).child("HR");
        fb_hr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                HR.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        Firebase fb_temp = MainActivity.ref.child("MT").child(patient_name).child("TEMP");
        fb_temp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Temp.setText(value);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


//        Firebase fb_z_axis = MainActivity.ref.child("MT").child(patient_name).child("Z-AXIS");
//        fb_z_axis.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String value = dataSnapshot.getValue(String.class);
//                z_axis.setText(value);
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//            }
//        });

        Firebase fb_crit = MainActivity.ref.child("MT").child(patient_name).child("CRITICAL");
        fb_crit.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Critical.setText(value);
                if (value.equals("Yes")) {
                    Critical.setTextColor(Color.parseColor("#f2181b"));
                } else {
                    Critical.setTextColor(Color.parseColor("#04ea00"));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }
}
