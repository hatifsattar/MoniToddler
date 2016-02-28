package com.example.hsattar.monitoddler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;

public class UploadVitals extends AppCompatActivity {

    public EditText enter_hr;
    public EditText enter_temp;
    public Button send;

    public String patient_name = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_vitals);
        patient_name = "patientX";

        enter_hr = (EditText) findViewById(R.id.set_hr);
        enter_temp = (EditText) findViewById(R.id.set_temp);
        send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String hrate_rate = enter_hr.getText().toString();
                int hr_num = Integer.parseInt(hrate_rate);

                String temp = enter_temp.getText().toString();
                int temp_num = Integer.parseInt(temp);
                set_vitals(hrate_rate, hr_num, temp, temp_num);
            }
        });

    }

    private void set_vitals(String hrate_rate, int hr_num, String temp, int temp_num) {

        Firebase fb = MainActivity.ref.child("MT").child(patient_name);

        fb.child("HR").setValue(hrate_rate);
        fb.child("TEMP").setValue(temp);

        String msg;
        if ((hr_num < 80)||(hr_num > 120) ||
                (temp_num < 32) || (temp_num > 42)){
            msg = "Yes";
        }
        else {
            msg = "No";
        }
        fb.child("CRITICAL").setValue(msg);

    }
}
