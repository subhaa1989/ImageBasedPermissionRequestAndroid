package com.infsecurity.cispa.permissionsdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.infsecurity.cispa.adapter.HotelListAdapter;
import com.infsecurity.cispa.utilities.FileCache;
import com.infsecurity.cispa.utilities.ImageLoader;
import com.infsecurity.cispa.utilities.PermissionUtils;
import com.infsecurity.cispa.utilities.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by subha on 1/11/2016.
 */

public class MainActivity extends AppCompatActivity  implements LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    String [] PermissionsLocation =
            {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            };

    int RequestLocationId = 0;
    Context context = null;
    ListView mListView = null;
    HotelListAdapter adapter = null;
    private Toolbar toolbar;
    EditText mRestSearch = null;
    Activity mainActivityInst = null;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    //for getting location details

    private LocationManager locationManager;
    private String provider;

    //latitude and longitude strings
    private double latitude = 0.0;
    private double longitude = 0.0;

    //places
    private static final String API_KEY = "AIzaSyBWUPdxlTq6k3qbVEIt_1EVVIe-JyomwMo";
    private static final String SERVER_KEY = "AIzaSyBJkLALGmySF1Zcg5YgbB7R1bVxQg5Q9GM";

    //holding list of places
    ArrayList<Places> placesList = new ArrayList<Places>();

    FileCache fileCache = null;
    ImageLoader imageLoader = null;

    private static boolean isLocationPermGranted = false;
    private static boolean isInternetPermissionGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        mainActivityInst = this;

        fileCache = new FileCache(context);
        imageLoader= new ImageLoader(context);


        mListView = (ListView)findViewById(R.id.list_view);
        mRestSearch = (EditText) findViewById(R.id.inputSearch);
        mRestSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                //
                if(adapter != null) {
                    String text = mRestSearch.getText().toString().toLowerCase(Locale.getDefault());
                    adapter.filter(text);
                }
            }
        });

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        //fetch from intnet
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isInternetPermissionGranted = extras.getBoolean("internetpermission");

        }

        if(isInternetPermissionGranted) {
            // Get the location manager
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            boolean enabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // check if enabled and if not send user to the GSP settings
            // Better solution would be to display a dialog and suggesting to
            // go to the settings
            if (!enabled) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

            fetchLocation();
            checkPermissions();

        }
        else {
            setEmptyList();

        }


    }


    private void setEmptyList()
    {
        //set empty text
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        if(!isInternetPermissionGranted)
        alertDialogBuilder.setMessage(R.string.no_display_data);
        else if(!isLocationPermGranted)
            alertDialogBuilder.setMessage(R.string.no_displaylocation_data);

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });



        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
       this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
       /* SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
*/

    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
       super.onResume();

       // isLocationPermGranted = preferences.getBoolean("locationPerm" , false);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = (location.getLatitude());
        longitude = (location.getLongitude());
        //current latitude and longitude
        /*Toast.makeText(this, "Location" +latitude+","+longitude,
                Toast.LENGTH_LONG).show();*/



    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }



    private void checkPermissions()
    {
        if ((int) Build.VERSION.SDK_INT < 23)
            return;
        else
        {
            GetLocationPermission();
        }
    }

    private void GetLocationPermission() {
        String permission = android.Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
        {
            //already permission granted
            //Toast.makeText(this, "Location access granted already.",  Toast.LENGTH_LONG).show();
            if(PermissionUtils.isNormalMode())
                doIfPermissionGranted();
            else {
                if (isLocationPermGranted)
                    doIfPermissionGranted();
                else
                    askLocationPermission();
            }
            return;
        }
        else
        {
            //ask permission
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                if(PermissionUtils.isNormalMode())
                     ActivityCompat.requestPermissions(this, PermissionsLocation  , MY_PERMISSIONS_REQUEST_READ_LOCATION);
                else {
                    if (isLocationPermGranted)
                        doIfPermissionGranted();
                    else
                        askLocationPermission();
                }
                return;
            }
        }

        //show explanation
       if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
        {
            //Explain to the user why we need to read the contacts
            //ask request again
            if(PermissionUtils.isNormalMode())
                ActivityCompat.requestPermissions(this, PermissionsLocation  , MY_PERMISSIONS_REQUEST_READ_LOCATION);
            else {
                if (isLocationPermGranted)
                    doIfPermissionGranted();
                else
                    askLocationPermission();
            }


        }
        else
        {
            //no expl needed
            Toast.makeText(this, R.string.access_denied_string,
                    Toast.LENGTH_LONG).show();

        }


    }

    private Dialog dialog = null;
    private void askLocationPermission()
    {
        //show dialog
        dialog = new Dialog(this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.permission_dialog);
        // Set dialog title
        dialog.setTitle(R.string.title_location_permission_dialog);

        // set values for custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.pText);
        text.setText(R.string.title_location_permission);
        ImageView image = (ImageView) dialog.findViewById(R.id.pimage);
        image.setImageResource(R.drawable.location_icon);
        //for rationale text
        TextView ratText = (TextView) dialog.findViewById(R.id.prationaletext);
        ratText.setText(R.string.detail_location_permission);

        ImageView perImg = (ImageView) dialog.findViewById(R.id.prationaleimage);
        //perImg.setImageResource(R.drawable.location_d2);

        //get static maps - hannover lat n long by default
        //getGoogleMapThumbnail(52.3667, 9.7167);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getGoogleMapThumbnail(latitude, longitude);
            }
        }, 1000);



        //handle for button
        Button denyBtn = (Button)dialog.findViewById(R.id.buttonDeny);
        denyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(context, R.string.access_denied_string,
                        Toast.LENGTH_LONG).show();
                dialog.dismiss();
                isLocationPermGranted = false;
                setEmptyList();
            }
        });

        Button allowBtn = (Button)dialog.findViewById(R.id.buttonAllow);
        allowBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //close dialog
                dialog.dismiss();
               // Toast.makeText(context, R.string.access_location_granted, Toast.LENGTH_LONG).show();
                isLocationPermGranted = true;
                doIfPermissionGranted();
                // Perform action on click
                //ask permission
               // ActivityCompat.requestPermissions(mainActivityInst, PermissionsLocation  , MY_PERMISSIONS_REQUEST_READ_LOCATION);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                  //  Toast.makeText(this, R.string.access_location_granted,
                         //   Toast.LENGTH_LONG).show();
                    doIfPermissionGranted();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //no expl needed
                    Toast.makeText(this, R.string.access_denied_string,
                            Toast.LENGTH_LONG).show();
                    setEmptyList();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void doIfPermissionGranted()
    {


        //get location using google play services
        fetchLocation();

        //call google places API
        requestPlaces();


    }


    private void populateList(ArrayList<Places> places)
    {

        // use  custom layout
        adapter = new HotelListAdapter(this,places);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(MainActivity.this, RestDetailActivity.class);
                //TODO : send list details
                Places selObject = placesList.get(position);
                intent.putExtra("Reference" ,selObject.getReference());
                startActivity(intent);
            }
        });
    }


    /**
     * google API for requesting places nearby
     */
    private void requestPlaces() {


        final String url = " https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius=2000&type=restaurant&key="+SERVER_KEY
                ;

        //  final String url = " https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&type=restaurant";
        //initiate request
        new PlacesRequestTask().execute(url);

    }


    /**
     * Method to fetch the location on UI
     * */
    public void fetchLocation()
    {
        //TODO : check for permision granted
        try {
// Define the criteria how to select the location provider -> use
            // default
            Criteria criteria = new Criteria();
          //  provider = locationManager.getBestProvider(criteria, false);

            //locationManager.requestLocationUpdates( provider, 0, 0, this);

            //Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //get best possible
            Location location = getLastKnownLocation();

           // Toast.makeText(this, "provider."+provider+"location recv now", Toast.LENGTH_LONG).show();



            // Initialize the location fields
            if (location != null) {
                System.out.println("Provider " + provider + " has been selected.");
                onLocationChanged(location);
            } else {
                latitude =0.0;
                longitude = 0.0;
            }

            //TODO : get restuarent nearby for this location
        }
        catch(SecurityException e)
        {
            Log.e(TAG, "Exception in fetchLocation()"+e.getMessage()+e);
        }
    }



    /**
     * get static images of map
     *
     * @param lati
     * @param longi
     * @return
     */
    public  void getGoogleMapThumbnail(double lati, double longi) {

        if(Double.compare(lati,0.0) ==0)

        {
            //set default lat hannover
            Log.d(TAG,"empty coor"+lati+longi);
            lati=52.3667;longi =9.7167;
        }

        /*Toast.makeText(context, "Response" + lati+longi,
                Toast.LENGTH_LONG).show();*/


        String mapurl = "http://maps.google.com/maps/api/staticmap?center=" + lati + "," + longi +"&markers=|color:blue|label:Marker|"+lati+"%2C"+longi +"&zoom=18&size=1600x400&sensor=false";

        new DownloadFilesTask().execute(mapurl);


    }

    private class DownloadFilesTask extends AsyncTask<String , Integer, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... urls)
        {
            Bitmap bitmap = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (IOException e) {
                // Log exception
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ImageView perImg = (ImageView) dialog.findViewById(R.id.prationaleimage);
            result = imageLoader.getResizedBitmap(result,500,1000);
            perImg.setImageBitmap(result);
            dialog.show();


        }
    }



    private class PlacesRequestTask extends AsyncTask<String , Integer, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            //String POST_PARAMS = "param1=" + params[0] + "&param2=" + params[1];
            String POST_PARAMS =" location=-33.8670522,151.1957362&radius=500&type=restaurant&key="+API_KEY;
            URL obj = null;
            HttpURLConnection con = null;
            String resp = null;
            try {
                obj = new URL(urls[0]);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.connect();
                /**
                 // For POST only - BEGIN
                 con.setDoOutput(true);
                 OutputStream os = con.getOutputStream();
                 os.write(POST_PARAMS.getBytes());
                 os.flush();
                 os.close();
                 // For POST only - END
                 **/

                final int responseCode = con.getResponseCode();
                Log.i(TAG, "POST Response Code :: " + responseCode);
               /* runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Response" + responseCode,
                                Toast.LENGTH_LONG).show();
                    }
                });*/

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
                parseDetails(result);
            }
            else
            {
                //parse reference and get address
                placesList =  parseSearchResults(result);
                populateList(placesList);
            }

        }
    }

    private void parseDetails(String result) {

    }

    private ArrayList<Places> parseSearchResults(String jsonRes) {

        float[] res = new float[4];
        try {
            JSONObject jsonobj = new JSONObject(jsonRes);
            JSONArray resarray = jsonobj.getJSONArray("results");

            if (resarray.length() == 0) {
                //No Results
            } else {
                int len = resarray.length();
               // Toast.makeText(this, "Array length"+len,  Toast.LENGTH_LONG).show();
                for (int j = 0; j < len; j++) {
                    Places place = new Places();

                    place.setName(resarray.getJSONObject(j).getString("name"));
                    place.setAddress(resarray.getJSONObject(j).getString("vicinity"));
                    place.setImgUrl(resarray.getJSONObject(j).getString("icon"));
                    place.setReference(resarray.getJSONObject(j).getString("reference"));
                    if(!(resarray.getJSONObject(j).isNull("rating")))
                    place.setRating(resarray.getJSONObject(j).getString("rating"));
                    else
                    place.setRating("0");
                   JSONObject geometry = resarray.getJSONObject(j).getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");

                    place.setLatitude(location.getDouble("lat"));
                    place.setLongitude(location.getDouble("lng"));
                    android.location.Location.distanceBetween(latitude, longitude, place.getLatitude(), place.getLongitude(), res);
                    place.setDistance(res[0]);
                    placesList.add(place);

                }
            }

           // Toast.makeText(this, "Length arraylist."+placesList.size(), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placesList;
    }


    private Location getLastKnownLocation() throws SecurityException{
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
