package com.infsecurity.cispa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.infsecurity.cispa.permissionsdialog.R;
import com.infsecurity.cispa.utilities.Places;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by subha on 3/9/2016.
 */
public class HotelListAdapter  extends BaseAdapter {
    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<Places> placesList = null;
    private ArrayList<Places> arraylist;

    public HotelListAdapter(Context context,
                           List<Places> worldpopulationlist) {
        mContext = context;
        this.placesList = worldpopulationlist;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<Places>();
        this.arraylist.addAll(worldpopulationlist);
    }

    public class ViewHolder {
        TextView name;
        TextView km;
        TextView details;
        RatingBar rating;
    }

    @Override
    public int getCount() {
        return placesList.size();
    }

    @Override
    public Places getItem(int position) {
        return placesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_item, null);

            holder.name = (TextView) view.findViewById(R.id.rest_name);
            holder.km = (TextView) view.findViewById(R.id.rest_km);
            holder.details = (TextView) view.findViewById(R.id.rest_detail);

            holder.rating = (RatingBar) view.findViewById(R.id.rest_rating);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(placesList.get(position).getName());

        double distance = (placesList.get(position).getDistance())/1000;
        //round off to decimal places
        float dist =(float) Math.round(distance*10)/10;
        holder.km.setText(String.valueOf(dist) + " km");

        holder.details.setText(placesList.get(position).getAddress());

        String ratng = placesList.get(position).getRating();
        holder.rating.setRating(Float.parseFloat(ratng));


        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        placesList.clear();
        if (charText.length() == 0) {
            placesList.addAll(arraylist);
        } else {
            for (Places wp : arraylist) {
                if (wp.getName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    placesList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }


}
