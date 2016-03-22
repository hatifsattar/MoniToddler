package com.example.hsattar.monitoddler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationClass extends AppCompatActivity {
    public TextView Msg;
    public String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_class);


        Msg = (TextView) findViewById(R.id.emergency);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Msg.setText("MoniToddler has received a notification. \n" +
                    "Please go to the app and check on patients!");
        }
        else{
            message = bundle.getString("MSG");
            if (message!=null){
                Msg.setText(message);
            }
        }

//        Msg.setText("MoniToddler has detected an Emergency. Please go to the app and check on patients!");

    }
}
