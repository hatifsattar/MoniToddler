package com.example.hsattar.monitoddler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationClass extends AppCompatActivity {
    public TextView Msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_class);

        Msg = (TextView) findViewById(R.id.emergency);
        Msg.setText("MoniToddler has detected an Emergency. Please go to the app and check on patients!");

    }
}
