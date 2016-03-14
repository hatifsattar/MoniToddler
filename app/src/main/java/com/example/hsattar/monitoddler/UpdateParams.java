package com.example.hsattar.monitoddler;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UpdateParams extends AppCompatActivity {

    private EditText lowerHR;
    private EditText upperHR;
    private EditText lowerRR;
    private EditText upperRR;
    private EditText deltaRR;
    private Button saveParam;


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

    }

}
