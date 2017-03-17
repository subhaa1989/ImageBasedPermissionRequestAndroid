package com.infsecurity.cispa.permissionsdialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.infsecurity.cispa.adapter.ReviewAdapter;
import com.infsecurity.cispa.utilities.FileCache;
import com.infsecurity.cispa.utilities.ImageLoader;
import com.infsecurity.cispa.utilities.PermissionUtils;
import com.infsecurity.cispa.utilities.Places;
import com.infsecurity.cispa.utilities.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by subha on 1/11/2016.
 */
public class RestDetailActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_SEND_SMS = 10;
    private static final int MY_PERMISSIONS_CALL_CONTACT = 11;

    private Toolbar toolbar;

    private boolean isSMSGranted = false;
    private boolean isCallGranted = false;

    ListView mListView = null;

    private Context context;
    Activity thisActivityInst = null;

    private static final String TAG = RestDetailActivity.class.getSimpleName();

    String [] permissionsRequired =
            {
                    Manifest.permission.SEND_SMS,

            };
    String [] permissionsContactsRequired = {   Manifest.permission.CALL_PHONE,  };

    //places
    private static final String API_KEY = "AIzaSyBWUPdxlTq6k3qbVEIt_1EVVIe-JyomwMo";
    private static final String SERVER_KEY = "AIzaSyBJkLALGmySF1Zcg5YgbB7R1bVxQg5Q9GM";

    FileCache fileCache = null;
    ImageLoader imageLoader = null;
    String mHotelPhoneNumber = "017634345213";

    Places selectedPlace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_detail);
        context = getApplicationContext();
        thisActivityInst = this;

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        fileCache = new FileCache(context);
        imageLoader= new ImageLoader(context);

        mListView = (ListView)findViewById(R.id.list_view);

        //fetch from intnet
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String reference = extras.getString("Reference");
            fetchDetails(reference);
        }


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
        else if(id == R.id.action_sms)
        {
            askSMSPermission();
        }
        else if(id == R.id.action_call)
        {
            askCallPermission();
        }
        else if(id == R.id.action_compose)
        {
            askReviewPermission();
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchDetails(String ref)
    {
        final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
        StringBuilder urlString = new StringBuilder(
                PLACES_DETAILS_URL);
        urlString.append("&reference=" + ref);
        urlString.append("&sensor=false&key=" + SERVER_KEY);

        new PlacesDetailRequestTask().execute(urlString.toString());
    }

    private class PlacesDetailRequestTask extends AsyncTask<String , Integer, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {

            URL obj = null;
            HttpURLConnection con = null;
            String resp = null;
            try {
                obj = new URL(urls[0]);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.connect();


                final int responseCode = con.getResponseCode();
                Log.i(TAG, "POST Response Code :: " + responseCode);


                if (responseCode == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    resp = response.toString();
                    final String  dummy = resp;
                    Log.i(TAG, resp);
                   /* runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Response obj " + dummy,
                                    Toast.LENGTH_LONG).show();
                        }
                    });*/

                } else {
                    Log.i(TAG, "POST request did not work.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resp;
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {

        }

        @Override
        protected void onPostExecute(String result) {

            if(result.contains("address"))
            {
                //parse address

                updateDetails(parseDetails(result));
            }


        }
    }

    private void updateDetails(Places details)
    {
        //current restuarent selected
        selectedPlace = details;
        //UPdate UI
        TextView restName =  (TextView)findViewById(R.id.textName);
        restName.setText(details.getName());
        TextView textRating =  (TextView)findViewById(R.id.textRating);
        textRating.setText(details.getRating());
        RatingBar ratingIcon = (RatingBar)findViewById(R.id.rest_rating);
        ratingIcon.setRating(Float.parseFloat(details.getRating()));

        //change rating star color
        Drawable progress = ratingIcon.getProgressDrawable();
        DrawableCompat.setTint(progress, Color.parseColor("#40BFFF"));

        TextView textAddress =  (TextView)findViewById(R.id.textAddress);
        textAddress.setText(details.getAddress());

        //phone number
        TextView textPhone =  (TextView)findViewById(R.id.textPhone);
        String phoneNo = details.getPhoneNumber()+" , ";
        if(details.getInternationalPhoneNumber()!= null )
          phoneNo+=details.getInternationalPhoneNumber();
        textPhone.setText(phoneNo);

        //fetch image
        ImageView restImg =  (ImageView)findViewById(R.id.rest_image);
        try {
            //set icon bitmap
            if (!details.getImgUrl().contains("place_api/icons")) {
                Log.d(TAG, "img subha" + details.getImgUrl());

                imageLoader.DisplayImage(details.getImgUrl(), restImg,0,0);
            } else if (details.getPhotoReference() != null) {

                String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photoreference="+details.getPhotoReference()+"&sensor=false&key="+SERVER_KEY;
                imageLoader.DisplayImage(url, restImg,0,0);
                //get photo
            } else
                restImg.setImageResource(R.drawable.restaurant_splash);
        }
        catch (Exception e)
        {
            Log.d(TAG,"Exception in downloading image"+e.getMessage()+e);
            restImg.setImageResource(R.drawable.restaurant_splash);
        }
        //iterate over review
        ArrayList<Review> reviewList = details.getReview();
        String[] name=  new String[reviewList.size()];;
        String[] time =  new String[reviewList.size()];;
        String[] text =  new String[reviewList.size()];;
        String[] ratng =  new String[reviewList.size()];;
        String[] img =  new String[reviewList.size()];;
        for (int i =0; i<reviewList.size();i++)
        {
            Review reviewDetails = reviewList.get(i);
            name[i] = reviewDetails.getAuthorName();
            time[i] = reviewDetails.getTimestamp();
            text[i] = reviewDetails.getReviewText();
            ratng[i] = reviewDetails.getRating();
            img[i] = reviewDetails.getProfileURL();

        }

        //review
        ReviewAdapter reviewAdapter = new ReviewAdapter(this,name,time,img,ratng,text);
        mListView.setAdapter(reviewAdapter);

    }



    private Places parseDetails(String jsonRes) {

        Places place = new Places();
        float[] res = new float[4];
        try {
            JSONObject jsonobj = new JSONObject(jsonRes);
            JSONObject resarray = jsonobj.getJSONObject("result");

            if (resarray != null) {
                //No Results

                place.setName(resarray.getString("name"));
                place.setAddress(resarray.getString("formatted_address"));
                place.setPhoneNumber(resarray.getString("formatted_phone_number"));
                place.setImgUrl(resarray.getString("icon"));

                if(!(resarray.isNull("rating")))
                    place.setRating(resarray.getString("rating"));
                else
                    place.setRating("0");

                if(!(resarray.isNull("international_phone_number")))
                    place.setInternationalPhoneNumber(resarray.getString("international_phone_number"));

                JSONObject geometry = resarray.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                place.setLatitude(location.getDouble("lat"));
                place.setLongitude(location.getDouble("lng"));

                if(!(resarray.isNull("photos"))) {
                    JSONObject photos = resarray.getJSONObject("photos");
                    if (photos != null) {
                        String photoRef = photos.getString("photo_reference");
                        place.setPhotoReference(photoRef);
                    }
                }

                //get reviews
                JSONArray reviewArray = resarray.getJSONArray("reviews");
                if (reviewArray.length() == 0) {
                    //No Results
                } else {
                    int len = reviewArray.length();
                   /* Toast.makeText(this, "Array length" + len,
                            Toast.LENGTH_LONG).show();*/
                    for (int j = 0; j < len; j++) {


                        Review review = new Review();
                        review.setAuthorName(reviewArray.getJSONObject(j).getString("author_name"));
                        if(!(reviewArray.getJSONObject(j).isNull("author_url")))
                            review.setAuthorURL(reviewArray.getJSONObject(j).getString("author_url"));

                        review.setRating(reviewArray.getJSONObject(j).getString("rating"));

                        if(!(reviewArray.getJSONObject(j).isNull("profile_photo_url")))
                        {
                            String profileURL = reviewArray.getJSONObject(j).getString("profile_photo_url");
                            if(profileURL != null && profileURL.startsWith("//"))
                                profileURL = profileURL.replaceFirst("//","http://");
                            Log.d(TAG, "Profile URL"+profileURL);
                            review.setProfileURL(profileURL);
                        }

                        review.setReviewText(reviewArray.getJSONObject(j).getString("text"));
                        String timeAgo = PermissionUtils.getDateCurrentTimeZone(reviewArray.getJSONObject(j).getLong("time"));
                        review.setTimestamp(timeAgo);
                        place.addReview(review);



                    }
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;


    }



    private void askReviewPermission() {
        //open review dialog
        Intent reviewIntent = new Intent(RestDetailActivity.this,ReviewDialogActivity.class);
        startActivity(reviewIntent);
    }

    private void askCallPermission() {
        if ((int) Build.VERSION.SDK_INT < 23)
            return;
        else
        {
            String permission = Manifest.permission.CALL_PHONE;

            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
            {
                //already permission granted
                // Toast.makeText(this, "User has granted permission to send SMS .", Toast.LENGTH_LONG).show();
                if(PermissionUtils.isNormalMode())
                   doIfContactPermissionGranted();
                else {
                    if(isCallGranted)
                    doIfContactPermissionGranted();
                    else
                        showContactPermissionDialog();
                }

            }
            else
            {
                //ask permission
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                    if(PermissionUtils.isNormalMode())
                        ActivityCompat.requestPermissions(this, permissionsContactsRequired
                                , MY_PERMISSIONS_CALL_CONTACT);
                    else
                        showContactPermissionDialog();

                }


                //show explanation
                else
                {
                    //Explain to the user why we need to read the contacts
                   /* Toast.makeText(this, "Location access is required to show coffee shops nearby.",
                            Toast.LENGTH_LONG).show();*/


                    if(PermissionUtils.isNormalMode())
                        ActivityCompat.requestPermissions(this, permissionsContactsRequired
                                , MY_PERMISSIONS_CALL_CONTACT);
                    else
                        showContactPermissionDialog();

                }

            }
        }
    }

    private void askSMSPermission() {

        if ((int) Build.VERSION.SDK_INT < 23)
            return;
        else
        {
            String permission = Manifest.permission.SEND_SMS;

            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
            {
                //already permission granted
               // Toast.makeText(this, "User has granted permission to send SMS .", Toast.LENGTH_LONG).show();
                if(PermissionUtils.isNormalMode())
               doIfSMSPermissionGranted();
                else {
                    if(!isSMSGranted)
                        showSMSPermissionDialog();
                    else
                    doIfSMSPermissionGranted();
                }
                return;
            }
            else
            {
                //ask permission
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                    if(PermissionUtils.isNormalMode())
                    ActivityCompat.requestPermissions(this, permissionsRequired
                            , MY_PERMISSIONS_SEND_SMS);
                    else
                    showSMSPermissionDialog();
                    return;
                }


                //show explanation
                else
                {
                    //Explain to the user why we need to read the contacts
                   /* Toast.makeText(this, "Location access is required to show coffee shops nearby.",
                            Toast.LENGTH_LONG).show();*/


                    if(PermissionUtils.isNormalMode())
                        ActivityCompat.requestPermissions(this, permissionsRequired
                                , MY_PERMISSIONS_SEND_SMS);
                    else
                        showSMSPermissionDialog();

                }

            }
        }
    }

    private void showSMSPermissionDialog() {

        //show dialog
        final Dialog dialog = new Dialog(this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.permission_dialog);
        // Set dialog title
        dialog.setTitle(R.string.title_sms_permission_dialog);

        // set values for custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.pText);
        text.setText(R.string.title_sms_permission);
        ImageView image = (ImageView) dialog.findViewById(R.id.pimage);
        image.setImageResource(R.drawable.icon_sms);
        //for rationale text
        TextView ratText = (TextView) dialog.findViewById(R.id.prationaletext);
        ratText.setText(R.string.detail_sms_permission);

        ImageView perImg = (ImageView) dialog.findViewById(R.id.prationaleimage);
        perImg.setImageResource(R.drawable.sms_d3);
        dialog.show();

        //handle for button
        Button denyBtn = (Button)dialog.findViewById(R.id.buttonDeny);
        denyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                dialog.dismiss();
                Toast.makeText(context, R.string.access_denied_string,
                        Toast.LENGTH_LONG).show();
                isSMSGranted = false;

            }
        });

        Button allowBtn = (Button)dialog.findViewById(R.id.buttonAllow);
        allowBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //close dialog
                dialog.dismiss();
                // Perform action on click
                //ask permission
               // ActivityCompat.requestPermissions(thisActivityInst, permissionsRequired  , MY_PERMISSIONS_SEND_SMS);
                Toast.makeText(context, "sms access granted.",Toast.LENGTH_LONG).show();
                isSMSGranted = true;
                doIfSMSPermissionGranted();
            }
        });
    }

    private void showContactPermissionDialog() {

        //show dialog
        final Dialog dialog = new Dialog(this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.permission_dialog);
        // Set dialog title
        dialog.setTitle(R.string.title_call_permission_dialog);

        // set values for custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.pText);
        text.setText(R.string.title_call_permission);
        ImageView image = (ImageView) dialog.findViewById(R.id.pimage);
        image.setImageResource(R.drawable.contact_icon);
        //for rationale text
        TextView ratText = (TextView) dialog.findViewById(R.id.prationaletext);
        ratText.setText(R.string.detail_call_permission);

        ImageView perImg = (ImageView) dialog.findViewById(R.id.prationaleimage);
        perImg.setImageResource(R.drawable.call_d2);
        dialog.show();

        //handle for button
        Button denyBtn = (Button)dialog.findViewById(R.id.buttonDeny);
        denyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                dialog.dismiss();
                Toast.makeText(context, R.string.access_denied_string,
                        Toast.LENGTH_LONG).show();
                isCallGranted = false;
            }
        });

        Button allowBtn = (Button)dialog.findViewById(R.id.buttonAllow);
        allowBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //close dialog
                dialog.dismiss();
                // Perform action on click
                //ask permission
               // ActivityCompat.requestPermissions(thisActivityInst, permissionsRequired , MY_PERMISSIONS_SEND_SMS);
                isCallGranted = true;
                doIfContactPermissionGranted();
            }
        });
    }

    private void doIfSMSPermissionGranted() {

        String text = "Hey, We must try this restuarent sometime"+"\n "+selectedPlace.getName()+","+selectedPlace.getAddress();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + mHotelPhoneNumber));
        intent.putExtra("sms_body", text);
        startActivity(intent);

        try {
            startActivity(intent);
          //  finish();
            Log.i("Finished sending SMS...", "");
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(RestDetailActivity.this,
                    "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void doIfContactPermissionGranted()
    {
        Toast.makeText(RestDetailActivity.this,
                "You can enjoy the calling now !!! .", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //Toast.makeText(this, "sms access granted.", Toast.LENGTH_LONG).show();
                    doIfSMSPermissionGranted();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //no expl needed
                    Toast.makeText(this, "Permission Denied,Sorry, Gesto could not provide offer the service :(.",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

            case MY_PERMISSIONS_CALL_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                   /* Toast.makeText(this, "Permission to access contact list granted.",
                            Toast.LENGTH_LONG).show();*/
                    doIfContactPermissionGranted();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //no expl needed
                    Toast.makeText(this, R.string.access_denied_string,
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


}
