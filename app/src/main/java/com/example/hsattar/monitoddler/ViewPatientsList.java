package com.example.hsattar.monitoddler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPatientsList extends AppCompatActivity {

    private ListView listView;
    public ArrayList<PatientInfo> infoArrayList;
    public static boolean ADD_LISTENER_ONRESUME = false;
    private CustomListAdapter arrayAdapter;
    Firebase fb_ref;
    ValueEventListener ref_listener;
    public int transmit;
    String TAG = "ViewPatList";

    private static boolean initial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patients_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            transmit = 0;
            toolbar.setSubtitle("Long press to remove a Patient");
            fb_ref = MainActivity.ref.child("MT");
        }
        else{
            transmit = bundle.getInt("TRANSMIT");
            if (transmit == 1){
                toolbar.setSubtitle("Click on a Patient to start Transmitting");
            } else {
                toolbar.setSubtitle("Long press to remove a Patient");
            }
            fb_ref = MainActivity.ref.child("MT2");
        }
        ADD_LISTENER_ONRESUME = false;

        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.patient_list);
        infoArrayList = new ArrayList<> ();
        initial = true;
        //idList = new ArrayList<>();

        fb_ref = MainActivity.ref.child("MT2");

        arrayAdapter = new CustomListAdapter(this, R.layout.patient_list_item, infoArrayList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                PatientInfo item = infoArrayList.get(position);
                if (transmit == 1) {
                    transmitPatientVitals(item.getName(), item.getCase());
                } else {
                    openPatientPage(item.getCase());
                }
                //print_message("Clicked position: " + position + ", Patient: " + item.getName());
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int arg2, long arg3) {
                if (transmit == 0) {
                    remove_patient_dialog(arg2);
                }
                return true;
            }
        });

    }

    private void transmitPatientVitals(String name, String case_num) {
        Intent myIntent = new Intent(this, SensorTagActivity.class);
        //Intent myIntent = new Intent(this, UploadVitals.class);
        myIntent.putExtra("ID", case_num);
        myIntent.putExtra("NAME", name);
        startActivity(myIntent);
    }

    private void openPatientPage(String case_num) {
        Intent myIntent = new Intent(this, ViewPatient.class);
        myIntent.putExtra("ID", case_num);
        startActivity(myIntent);
    }

    private void remove_patient_dialog(final int position){

        PatientInfo info = infoArrayList.get(position);
        final String id = info.getCase().toString();

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("Are you sure you want to remove " + info.getName() +
                " from the database?");

        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                remove_patient(id, position);
            }
        });
        alertDialog.show();

    }

    private void remove_patient(String id, int position) {
        fb_ref.child(id).removeValue();
    }

    private void getPatientsFromDatabase() {

   /*     fb_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();
                if (td.size() < MainActivity.databse_fields_count) { return; }
                if (td!=null){
                    String name = td.get("NAME").toString();
                    String id = dataSnapshot.getKey().toString();  //d.child("NAME").getKey().toString();
                    String doctor = td.get("DOCTOR").toString();
                    String critical = td.get("CRITICAL").toString();
                    String age = td.get("AGE").toString();

                    PatientInfo patientInfo = new PatientInfo(name, age, doctor, id, critical);
                    infoArrayList.add(patientInfo);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();
                if (td.size() < MainActivity.databse_fields_count) { return; }
                if (td!=null){
                    String name = td.get("NAME").toString();
                    String id = dataSnapshot.getKey().toString();  //d.child("NAME").getKey().toString();
                    String doctor = td.get("DOCTOR").toString();
                    String critical = td.get("CRITICAL").toString();
                    String age = td.get("AGE").toString();

                    PatientInfo patientInfo = new PatientInfo(name, age, doctor, id, critical);
                    int num = arrayAdapter.getIndex(id);
                    if ((infoArrayList.size() != 0) || num < infoArrayList.size()) {
                        infoArrayList.remove(num);
                        infoArrayList.add(patientInfo);
                    }
                }
                updateList();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();
                if (td.size() < MainActivity.databse_fields_count) { return; }
                if (td!=null){
                    String id = dataSnapshot.getKey().toString();  //d.child("NAME").getKey().toString();

                    int num = arrayAdapter.getIndex(id);
                    if ((infoArrayList.size() != 0) || num < infoArrayList.size()) {
                        infoArrayList.remove(num);
                        updateList();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


        fb_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                infoArrayList.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (d != null) {
                        String name = d.child("NAME").getValue().toString();
                        String id = d.getKey().toString();  //d.child("NAME").getKey().toString();
                        String doctor = d.child("DOCTOR").getValue().toString();
                        String critical = d.child("CRITICAL").getValue().toString();
                        String age = d.child("AGE").getValue().toString();

                        PatientInfo patientInfo = new PatientInfo(name, age, doctor, id, critical);
                        infoArrayList.add(patientInfo);
                        initial = false;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });*/


        ref_listener = fb_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                infoArrayList.clear();
                //updateList();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (d != null) {

                        if (d.getChildrenCount() < (long) MainActivity.databse_fields_count) {
                            return;
                        }
                        String name = d.child("NAME").getValue().toString();
                        String id = d.getKey().toString();
                        String doctor = d.child("DOCTOR").getValue().toString();
                        String critical = d.child("CRITICAL").getValue().toString();
                        String age = d.child("AGE").getValue().toString();
                        String hr = d.child("HR").getValue().toString();
                        String temp = d.child("TEMP").getValue().toString();
                        String rr = d.child("RR").getValue().toString();
                        String x_axis = d.child("X-AXIS").getValue().toString();
                        String y_axis = d.child("Y-AXIS").getValue().toString();
                        String z_axis = d.child("Z-AXIS").getValue().toString();
                        String bp = d.child("BP").getValue().toString();
                        String sat = d.child("SAT").getValue().toString();



                        PatientInfo patientInfo = new PatientInfo(name, age, doctor, id, critical,
                                hr, temp, rr, x_axis, y_axis, z_axis, bp, sat);
                        infoArrayList.add(patientInfo);
                    }
                }
                updateList();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void updateList() {
        arrayAdapter.notifyDataSetChanged();
    }

    private void print_message(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fb_ref.removeEventListener(ref_listener);
        ADD_LISTENER_ONRESUME = true;
        //fb_ref.goOffline();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ref_listener!=null) fb_ref.removeEventListener(ref_listener);
        //fb_ref.goOffline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ADD_LISTENER_ONRESUME) {
            if (ref_listener!=null) fb_ref.addValueEventListener(ref_listener);
            ADD_LISTENER_ONRESUME = false;
        }
        //fb_ref.goOnline();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPatientsFromDatabase();
        ADD_LISTENER_ONRESUME = false;
    }
}
