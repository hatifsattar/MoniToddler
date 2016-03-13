package com.example.hsattar.monitoddler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hsattar on 3/8/2016.
 */
public class CustomListAdapter  extends ArrayAdapter<PatientInfo> {

    private Context context;
    private int resource;
    private PatientInfo[] list;
    private ArrayList<PatientInfo> arrayList;

    public CustomListAdapter(Context context, int resource, PatientInfo[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.list = objects;
    }
    public CustomListAdapter(Context context, int resource, ArrayList<PatientInfo> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.arrayList = objects;
    }

    public int getIndex (String id) {
        int index = 0;

        for (int i = 0; i < arrayList.size(); i++){
            PatientInfo patientInfo = arrayList.get(i);
            if (patientInfo.getCase().matches(id)){
                index = i;
            }
        }
        return index;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater layoutInflater = ((Activity)this.context).getLayoutInflater();
            convertView = layoutInflater.inflate(this.resource, parent, false);
        }
        PatientInfo contactInfo = arrayList.get(position);
        TextView name = (TextView) convertView.findViewById(R.id.patient_name);
        TextView ward = (TextView) convertView.findViewById(R.id.ward_id);
        TextView doctor = (TextView) convertView.findViewById(R.id.doctor_name);
        TextView case_name = (TextView) convertView.findViewById(R.id.case_name);
        TextView hr = (TextView) convertView.findViewById(R.id.x_axis);
        TextView bp = (TextView) convertView.findViewById(R.id.bloodpressure);
        TextView sat = (TextView) convertView.findViewById(R.id.saturation);
        TextView temp = (TextView) convertView.findViewById(R.id.temperature);
        TextView last_update = (TextView) convertView.findViewById(R.id.last_updated);
        if (contactInfo != null) {
            name.setText(contactInfo.getName());
            ward.setText("Age: " + contactInfo.getAge());
            doctor.setText("Dr. " + contactInfo.getDoctor());
            case_name.setText("HFN# " + contactInfo.getCase());
            hr.setText("Heart Rate: " + contactInfo.getHeartRate());
//            bp.setText("Blood Pressure: " + contactInfo.getBloodPressure());
            bp.setVisibility(View.GONE);
            sat.setText("Resp. Rate: " + contactInfo.getRespRate());
            temp.setText("-");//temp.setText("Temp.: " + contactInfo.getTemperature());
            String value = contactInfo.getCritical();
            last_update.setText("Critical: " + value);
            if (value!=null) {
                if (value.equals("Yes")) {
                    last_update.setTextColor(Color.parseColor("#f2181b"));
                } else {
                    last_update.setTextColor(Color.parseColor("#04ea00"));
                }
            }
        }
        return convertView;
    }
}


