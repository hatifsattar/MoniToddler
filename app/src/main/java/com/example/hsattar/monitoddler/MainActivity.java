package com.example.hsattar.monitoddler;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final int MT_PERMISSION_ACCESS_STORAGE = 12;
    private static final int MT_PERMISSION_ACCESS_GPS = 13;
    private static final int MT_PERMISSION_ACCESS_BLUETOOTH = 14;
    private static final int MT_PERMISSION_ACCESS_STATE = 15;

    public static int EMERGENCY_NOTIFICATION_ENABLE;

    public static int databse_fields_count = 11; //IMPORTANT - Update this when increasing number of upload fields

    public static final String FIREBASE_URL = "https://crackling-torch-1983.firebaseio.com/";

    public static Firebase ref;
    private Menu menu;
    private Context mContext;

    //public final String TABLE_1 = "listCheck";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        checkPermissions();

        // Using Firebase to populate the list.
        Firebase.setAndroidContext(this);
        ref = new Firebase(MainActivity.FIREBASE_URL);
        mContext = this;

        EMERGENCY_NOTIFICATION_ENABLE = 0;

        // Add items via the Button and EditText at the bottom of the window.
        final Button btn_view = (Button) findViewById(R.id.viewPatient);
        final Button btn_update = (Button) findViewById(R.id.updateButton);
        final Button btn_add = (Button) findViewById(R.id.addButton);

        btn_view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //view_activity();
                view_all_patients_activity();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                update_activity();
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                add_patient();
            }
        });
    }

    private void update_activity() {
        //Intent myIntent = new Intent(this, SensorTagActivity.class);
        Intent myIntent = new Intent(this, ViewPatientsList.class);
        myIntent.putExtra("TRANSMIT", 1);
        startActivity(myIntent);
    }

    private void view_activity() {
        Intent myIntent = new Intent(this, ViewPatient.class);
        startActivity(myIntent);
    }

    private void view_all_patients_activity() {
        Intent myIntent = new Intent(this, ViewPatientsList.class);
        myIntent.putExtra("TRANSMIT", 0);
        startActivity(myIntent);
    }

    private void add_patient(){
        Intent myIntent = new Intent(this, AddPatient.class);
        startActivity(myIntent);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
        {
            //we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MT_PERMISSION_ACCESS_BLUETOOTH);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            //we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MT_PERMISSION_ACCESS_STORAGE);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            //we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, MT_PERMISSION_ACCESS_STATE);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MT_PERMISSION_ACCESS_GPS);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        checkPermissions(); //Call checkPermissions again until all permissions have been granted
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MenuItem settingsTitle = menu.findItem(R.id.action_settings);
            if (EMERGENCY_NOTIFICATION_ENABLE == 1){
                EMERGENCY_NOTIFICATION_ENABLE = 0;
                settingsTitle.setTitle("Enable Notification");
                Toast.makeText(this, "Disabling Notifications", Toast.LENGTH_SHORT).show();
            } else {
                EMERGENCY_NOTIFICATION_ENABLE = 1;
                settingsTitle.setTitle("Disable Notification");
                Toast.makeText(this, "Enabling Notifications", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "Exiting MoniToddler - Disabling Notifications", Toast.LENGTH_SHORT).show();
        EMERGENCY_NOTIFICATION_ENABLE = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
}
