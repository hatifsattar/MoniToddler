package com.example.hsattar.monitoddler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.PriorityQueue;

public class AddExtraVitals extends AppCompatActivity {

    public String patient_name = "";
    public String patient_id = "";
    Firebase fb_ref;

    private TextView header;
    private EditText bp;
    private EditText sat;
    private EditText temp;
    private Button saveVitals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_extra_vitals);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            patient_name = "patientX";
            patient_id = "ERROR";
            fb_ref = MainActivity.ref.child("MT");
        }
        else{
            patient_name = bundle.getString("NAME");
            patient_id = bundle.getString("ID");
            fb_ref = MainActivity.ref.child("MT2");
        }

        header = (TextView) findViewById(R.id.addHead);
        header.setText("Add additional Vitals for Patient:\n" + patient_name);
        bp = (EditText) findViewById(R.id.addBP);
        sat = (EditText) findViewById(R.id.addSat);
        temp = (EditText) findViewById(R.id.addTemp);
        saveVitals = (Button) findViewById(R.id.buttonSaveVitals);
        saveVitals.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //add_vitals_activity();
            }
        });

    }
}
