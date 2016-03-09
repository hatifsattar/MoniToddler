package com.example.hsattar.monitoddler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class AddPatient extends AppCompatActivity {

    private static String PATIENT_NAME = "";
    private static String PATIENT_ID = "";

    private EditText name;
    private EditText age;
    private EditText case_number;
    private EditText doctor;
    private Button add;
    private Button transmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        PATIENT_NAME = "";
        PATIENT_ID = "";

        name = (EditText) findViewById(R.id.patientName);
        age = (EditText) findViewById(R.id.patientAge);
        case_number = (EditText) findViewById(R.id.patientHFN);
        doctor = (EditText) findViewById(R.id.doctorName);
        add = (Button) findViewById(R.id.buttonAddPatient);
        transmit = (Button) findViewById(R.id.buttonTransmit);

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                patient_check();
            }
        });

        transmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                remove_selected_patient();
            }
        });
    }

    private void remove_selected_patient() {
        if (PATIENT_NAME.equals("") || PATIENT_ID.equals("") ){
            print_message("Sorry, no patient selected!");
        }
        else {
            Firebase fb = MainActivity.ref.child("MT");
            fb.child(PATIENT_ID).removeValue();
            print_message("Patient " + PATIENT_NAME + " removed from database");
            PATIENT_ID = "";
            PATIENT_NAME = "";
        }
    }

    private void patient_check() {

        //Error checking
        if ( name.getText().toString().matches("") ||
             name.getText().toString().matches("[ ]*") ||
            !name.getText().toString().matches("[a-zA-Z ]*") ) {
            print_message("Please enter a valid Name");
        }
        else if (age.getText().toString().matches("") ||
                age.getText().toString().matches("0") ||
                age.getText().toString().contains("-")) {
            print_message("Please enter a valid Age");
        }
        else if (doctor.getText().toString().matches("") ||
                !doctor.getText().toString().matches("[a-zA-Z ]*") ) {
            print_message("Please enter a valid Name for Doctor");
        }
        else if (case_number.getText().toString().matches("") ||
                !case_number.getText().toString().matches("[0-9 ]*") ) {
            print_message("Please enter a valid Hospital File Number");
        }
        else {
            String n = name.getText().toString().trim(); // remove beginning and ending spaces
            final String NAME = n.substring(0,1).toUpperCase() + n.substring(1); //Capitalize first letter
            final String AGE = age.getText().toString();
            String d = doctor.getText().toString().trim();// remove beginning and ending spaces
            final String DOCTOR = d.substring(0,1).toUpperCase() + d.substring(1);//Capitalize first letter
            final String ID = case_number.getText().toString();

            Firebase fb = MainActivity.ref.child("MT");//.child(CASE);
            fb.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        //patient exists
                        print_message("Sorry, Another patient already exists for this File Number");
                    } else {
                        //patient does not exist
                        add_patient(NAME, AGE, DOCTOR, ID);
                        print_message("Added new patient, " + NAME);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });

        }
    }

    private void add_patient(String Name, String Age, String Doctor, String Id){
        Firebase fb = MainActivity.ref.child("MT").child(Id);
        fb.child("NAME").setValue(Name);
        fb.child("AGE").setValue(Age);
        fb.child("DOCTOR").setValue(Doctor);

        fb.child("HR").setValue("-");
        fb.child("TEMP").setValue("-");
        fb.child("CRITICAL").setValue("-");

        PATIENT_NAME = Name;
        PATIENT_ID = Id;
    }

    private void print_message(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("PATIENT_NAME", PATIENT_NAME);
        savedInstanceState.putString("PATIENT_ID", PATIENT_ID);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        PATIENT_NAME = savedInstanceState.getString("PATIENT_NAME");
        PATIENT_ID = savedInstanceState.getString("PATIENT_ID");
    }
}
