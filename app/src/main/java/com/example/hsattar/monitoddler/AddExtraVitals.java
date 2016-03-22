package com.example.hsattar.monitoddler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class AddExtraVitals extends AppCompatActivity {

    public String patient_name = "";
    public String patient_id = "";
    Firebase fb_ref;

    private TextView header;
    private EditText bp;
    private EditText sat;
    private EditText temp;
    private EditText note;
    private EditText noteAuthor;
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
        note = (EditText) findViewById(R.id.addNote);
        noteAuthor = (EditText) findViewById(R.id.noteAuthor);
        saveVitals = (Button) findViewById(R.id.buttonSaveVitals);
        saveVitals.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                upload_extra_vitals();
            }
        });

    }

    private void upload_extra_vitals() {

        int success = 0;

        String bloodpres = bp.getText().toString();
        String saturation = sat.getText().toString();
        String temperature = temp.getText().toString();
        String note_ = note.getText().toString();
        String noteAuth_ = noteAuthor.getText().toString();

        if (bloodpres!=null){
            if (!bloodpres.matches("") && /* No null accepted*/
                !bloodpres.matches("[ ]*") && /* No only spaces accepted */
                 bloodpres.matches("[a-zA-Z0-9,/: ]*")  /*Only Valid formats are alphanumeric, "," , ":" , "/" and spaces */ )
            {
                fb_ref.child(patient_id).child("BP").setValue(bloodpres);
                success = 1;
            }
        }
        if (saturation!=null){
            if (!saturation.matches("")&&
                !saturation.matches("[ ]*") &&
                 saturation.matches("[a-zA-Z0-9,/: ]*") )
            {
                fb_ref.child(patient_id).child("SAT").setValue(saturation);
                success = 1;
            }
        }
        if (temperature!=null){
            if (!temperature.matches("")&&
                !temperature.matches("[ ]*") &&
                 temperature.matches("[a-zA-Z0-9,/: ]*") )
            {
                fb_ref.child(patient_id).child("TEMP").setValue(temperature);
                success = 1;
            }
        }
        if ((note_!=null) && (noteAuth_!=null)){
            if (!note_.matches("") &&
                !note_.matches("[ ]*") &&
                !note_.matches("[\n]*")  )
            {
                if (note_.length() > 100) {
                    print_message("Please restrict your note to 100 characters!");
                    success = 2;
                }
                else if (!note_.matches("[a-zA-Z0-9,/:. ]*")) {
                    print_message("Please avoid any special characters in your note!");
                    success = 2;
                }
                else if ( noteAuth_.matches("") ||
                          noteAuth_.matches("[ ]*") ||
                          noteAuth_.matches("[\n]*") ||
                         !noteAuth_.matches("[a-zA-Z0-9,. ]*"))
                {
                    print_message("Please enter a valid name for the Note Author!");
                    success = 2;
                }
                else {
                    String name_trim = noteAuth_.trim(); // remove beginning and ending spaces
                    String note_trim = note_.trim(); // remove beginning and ending spaces
                    final String NAME = name_trim.substring(0,1).toUpperCase() + name_trim.substring(1); //Capitalize first letter
                    fb_ref.child(patient_id).child("NOTE").setValue(NAME + " - " + note_trim);
                    success = 1;
                }
            }
        }

        if (success == 0){
            print_message("Failed to upload any Vitals!");
        }
        else if (success == 1){
            print_message("Uploaded Changes!");
        }

    }

    private void print_message(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}
