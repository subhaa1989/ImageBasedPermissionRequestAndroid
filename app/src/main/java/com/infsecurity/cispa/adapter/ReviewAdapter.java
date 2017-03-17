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
import com.infsecurity.cispa.utilities.FileCache;
import com.infsecurity.cispa.utilities.ImageLoader;


/**
 * Created by subha on 3/4/2016.
 */
public class ReviewAdapter extends ArrayAdapter{

    private final Context context;
    private final String[] name;
    private final String[] time;
    private final String[] text;
    private final String[] ratng;
    private final String[] img;

    private FileCache fileCache = null;
    private ImageLoader imageLoader = null;

    public ReviewAdapter(Context context, String[] name, String[] time,  String[] img, String[] rating ,String[] text) {
        super(context, R.layout.review_row, name);
        this.context = context;
        this.img = img;
        this.name = name;
        this.time = time;
        this.ratng = rating;
        this.text = text;
        fileCache = new FileCache(context);
        imageLoader= new ImageLoader(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.review_row, parent, false);

        //reviewer
       TextView nameText = (TextView) rowView.findViewById(R.id.author_name);
        nameText.setText(name[position]);
        //time
        TextView timeText = (TextView) rowView.findViewById(R.id.review_time);
        timeText.setText(time[position]);

        //rating txt
        TextView ratingText = (TextView) rowView.findViewById(R.id.rating_text);
        ratingText.setText(ratng[position]);

        RatingBar rating = (RatingBar) rowView.findViewById(R.id.rest_rating);
        rating.setRating(Float.parseFloat(ratng[position]));

        TextView reviewText = (TextView) rowView.findViewById(R.id.review_detail);
        reviewText.setText(text[position]);

        ImageView imgBtn = null;
        try {
             imgBtn = (ImageView) rowView.findViewById(R.id.authorProfileImg);
            imageLoader.DisplayImage(img[position], imgBtn,32,32);
        }
        catch (Exception e)
        {
            imgBtn.setImageResource(R.drawable.avatar);
        }

        return rowView;
    }

}
