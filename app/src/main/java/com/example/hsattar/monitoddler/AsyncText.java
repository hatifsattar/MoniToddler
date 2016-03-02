package com.example.hsattar.monitoddler;

import android.content.Context;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

// This AsyncTask is for the scrolling connection messages
public class AsyncText extends AsyncTask<String, Void, String> {
    public Context context;
    private View rootView;

    public AsyncText(Context context, View rootView) {
        this.context = context;
        this.rootView = rootView;
    }

    @Override
    protected String doInBackground(String... params) {
        String newText = new String(params[0]);
        String currentText = new String(params[1]);
        currentText += "\n" + newText;
        int length = SensorTagActivityFragment.currentText.length();
        if (SensorTagActivityFragment.currentText.length() > 5000) {
            SensorTagActivityFragment.currentText = "";
        }
        else {
            SensorTagActivityFragment.currentText = currentText;
        }
        return currentText;
    }

    @Override
    protected void onPostExecute(String currentText) {
        TextView myTextView = (TextView) rootView.findViewById(R.id.textbox);
        final ScrollView myScrollView = (ScrollView) rootView.findViewById(R.id.scrollbox);
        myTextView.setText(currentText);
        myScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);
    }

}
