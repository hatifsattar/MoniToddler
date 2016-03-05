package com.example.hsattar.monitoddler;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorTagActivity extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter = null;
    private Menu menu;

    //logging
    private File logFile;
    public static FileOutputStream fOut;
    private String fileDir = "/sdcard/Monitoddler";
    public static boolean isLogging = false;
    public static OutputStreamWriter LogWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensortag);

        // Check for Bluetooth support, if not exit application.
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            AlertDialog.Builder aB = new AlertDialog.Builder(this);
            aB.setTitle("Error !");
            aB.setMessage("This Android device does not have Bluetooth or there is an error in the " +
                    "bluetooth setup. Application cannot start, will exit.");
            aB.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            AlertDialog a = aB.create();
            a.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update, menu);
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
        if (id == R.id.action_update_settings) {
            MenuItem settingsTitle = menu.findItem(R.id.action_update_settings);
            if (!isLogging) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                    Date now = new Date();
                    File dir = new File(fileDir);
                    dir.mkdirs();
                    logFile = new File(dir, "Log_"+formatter.format(now)+".txt");
                    //logFile.createNewFile();
                    fOut = new FileOutputStream(logFile);
                    LogWriter = new OutputStreamWriter(fOut);
                    isLogging = true;
                    message("logging begins!");
                    settingsTitle.setTitle("Stop Logging");
                }catch (Exception e) {
                    message("Could not open file!");
                }

            }
            else
            {
                //gets closed onPause in fragment too
                try {
                    LogWriter.close();
                    fOut.close();
                    isLogging = false;
                    message("logging ends!");
                }catch (Exception e) {
                    message("Could not close file!");
                }

                settingsTitle.setTitle("Start Logging");
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void message(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
