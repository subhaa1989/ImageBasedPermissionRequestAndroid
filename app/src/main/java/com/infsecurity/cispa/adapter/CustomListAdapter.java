package com.infsecurity.cispa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.infsecurity.cispa.permissionsdialog.R;

import java.util.ArrayList;

/**
 * Created by subha on 1/20/2016.
 */
public class CustomListAdapter extends ArrayAdapter {

    private final Context context;
    private  String[] values;
    private final String[] address;
    private final double[] distance;
    private final String[] ratng;

    public CustomListAdapter(Context context, String[] values, String[] address,  double[] dist,String[] rating) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
        this.address = address;
        this.distance = dist;
        this.ratng = rating;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.rest_name);
        RatingBar rating = (RatingBar) rowView.findViewById(R.id.rest_rating);

        rating.setRating(Float.parseFloat(ratng[position]));

        TextView km = (TextView) rowView.findViewById(R.id.rest_km);
        TextView details = (TextView) rowView.findViewById(R.id.rest_detail);
        name.setText(values[position]);
        double distInkm = distance[position]/1000;
        //round off to decimal places
        float dist =(float) Math.round(distInkm*10)/10;
        km.setText(String.valueOf(dist)+" km");
        details.setText(address[position]);



        return rowView;
    }

    public void doFilter(String filterData)
    {
    //iterate through values
        ArrayList<String> tempVal = new ArrayList<>();
        for (int i = 0; i <values.length ; i++) {
            if(values[i].startsWith(filterData) || values[i].contains(filterData) || values[i].endsWith(filterData))
            {
                tempVal.add(values[i]);
            }

        }
        for (int i = 0; i <tempVal.size() ; i++) {
              values[i] = tempVal.get(i);
        }
        notifyDataSetChanged();
    }

}
