package com.example.hsattar.monitoddler;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    public static final String FIREBASE_URL = "https://crackling-torch-1983.firebaseio.com/";
    public static Firebase ref;

    public final String TABLE_1 = "listCheck";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
// Use Firebase to populate the list.
        Firebase.setAndroidContext(this);
        ref = new Firebase(MainActivity.FIREBASE_URL);

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);


        new Firebase(FIREBASE_URL + TABLE_1)
                .addChildEventListener(new ChildEventListener() {
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        adapter.add((String) dataSnapshot.child("text").getValue());
                    }

                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        adapter.remove((String) dataSnapshot.child("text").getValue());
                    }

                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        // Add items via the Button and EditText at the bottom of the window.
        final EditText text = (EditText) findViewById(R.id.someText);
        final Button button_add = (Button) findViewById(R.id.addButton);
        final Button btn_view = (Button) findViewById(R.id.viewPatient);
        final Button btn_update = (Button) findViewById(R.id.updateButton);
        button_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Firebase(FIREBASE_URL + TABLE_1)
                        .push()
                        .child("text")
                        .setValue(text.getText().toString());
                text.setText("");
            }
        });

        btn_view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view_activity();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                update_activity();
            }
        });

        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                new Firebase(FIREBASE_URL + TABLE_1)
                        .orderByChild("text")
                        .equalTo((String) listView.getItemAtPosition(arg2))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                    firstChild.getRef().removeValue();
                                }
                            }

                            public void onCancelled(FirebaseError firebaseError) {
                            }
                        });
                return true;
            }
        });
    }

    private void update_activity() {
        Intent myIntent = new Intent(this, UploadVitals.class);
        startActivity(myIntent);
    }

    private void view_activity() {
        Intent myIntent = new Intent(this, ViewPatient.class);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
