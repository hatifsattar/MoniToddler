package com.example.hsattar.monitoddler;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UpdateParams extends AppCompatActivity {

    public static float LOWER_HR_PER_MIN_LIMIT = 60;
    public static float UPPER_HR_PER_MIN_LIMIT = 130;
    public static float LOWER_RR_PER_MIN_LIMIT = 4;
    public static float UPPER_RR_PER_MIN_LIMIT = 15;
    public static double DELTA_RR = 0.08;

    private EditText lowerHR;
    private EditText upperHR;
    private EditText lowerRR;
    private EditText upperRR;
    private EditText deltaRR;
    private Button saveParam;
    private Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_params);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lowerHR = (EditText) findViewById(R.id.lowerHR);
        upperHR = (EditText) findViewById(R.id.upperHR);
        lowerRR = (EditText) findViewById(R.id.lowerRR);
        upperRR = (EditText) findViewById(R.id.upperRR);
        deltaRR = (EditText) findViewById(R.id.deltaRR);
        saveParam = (Button) findViewById(R.id.buttonSave);
        saveParam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save_global_params();
            }
        });
        reset = (Button) findViewById(R.id.buttonReset);
        reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reset_params();
            }
        });


    }

    private void reset_params() {
        LOWER_HR_PER_MIN_LIMIT = 60;
        UPPER_HR_PER_MIN_LIMIT = 130;
        LOWER_RR_PER_MIN_LIMIT = 4;
        UPPER_RR_PER_MIN_LIMIT = 15;
        DELTA_RR = 0.08;

        lowerHR.getText().clear();
        upperHR.getText().clear();
        lowerRR.getText().clear();
        upperRR.getText().clear();
        deltaRR.getText().clear();

        print_message("Parameters reset to defaults!");
    }

    private void save_global_params() {

        int success = 0;
        String lower_hr = lowerHR.getText().toString();
        String upper_hr = upperHR.getText().toString();
        String lower_rr = lowerRR.getText().toString();
        String upper_rr = upperRR.getText().toString();
        String delta_rr = deltaRR.getText().toString();

//        float lower__hr;
//        float upper__hr;
//        float lower__rr;
//        float upper__rr;
//        double delta__rr;

        if ((lower_hr!=null) && (!lower_hr.matches(""))){
            LOWER_HR_PER_MIN_LIMIT = Float.parseFloat(lower_hr);
            success = 1;
        }
        if ((upper_hr!=null) && (!upper_hr.matches(""))){
            UPPER_HR_PER_MIN_LIMIT = Float.parseFloat(upper_hr);
            success = 1;
        }
        if ((lower_rr!=null) && (!lower_rr.matches(""))){
            LOWER_RR_PER_MIN_LIMIT = Float.parseFloat(lower_rr);
            success = 1;
        }
        if ((upper_rr!=null) && (!upper_rr.matches(""))){
            UPPER_RR_PER_MIN_LIMIT = Float.parseFloat(upper_rr);
            success = 1;
        }
        if ((delta_rr!=null) && (!delta_rr.matches(""))){
            DELTA_RR = Double.parseDouble(delta_rr);
            success = 1;
        }

        if (success == 0){
            print_message("Failed to update params!");
        }
        else if (success == 1){
            print_message("Updated params successfully!");
        }
    }

    private void print_message(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}
