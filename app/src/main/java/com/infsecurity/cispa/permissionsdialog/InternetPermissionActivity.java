package com.infsecurity.cispa.permissionsdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.infsecurity.cispa.utilities.PermissionUtils;
/**
 * Created by subha on 1/11/2016.
 */
public class InternetPermissionActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    Context context = null;
    String [] PermissionsLocation =
            {
                    android.Manifest.permission.INTERNET,

            };
     Dialog dialog = null;
    private boolean isInternetGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.temp);
        context = getApplicationContext();
        PermissionUtils.setIsNormalMode(true);
        checkPermissions();
       // askInternetPermission();


    }

    private void checkPermissions()
    {
        if (Build.VERSION.SDK_INT < 23)
            return;
        else
        {
            GetInternetPermission();
        }
    }

    private void GetInternetPermission() {
        String permission = android.Manifest.permission.INTERNET;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
        {
            //already permission granted
            //Toast.makeText(this, "Internet access granted already.",
             //       Toast.LENGTH_LONG).show();
            isInternetGranted = true;
            //even though permission granted , just to give proper info we ask permission again
            if(!PermissionUtils.isNormalMode())
            askInternetPermission();
            else
                doIfInternetPermissionGranted();
            // Perform action on click
            /*Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);*/
        }

        //show explanation
       else  if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
        {
            //Explain to the user why we need to read the contacts
           // Toast.makeText(this, "Internet access is required to search restuarants nearby.",
            //        Toast.LENGTH_LONG).show();

            //ask request again
           // ActivityCompat.requestPermissions(this, PermissionsLocation, MY_PERMISSIONS_REQUEST_INTERNET);
            if(PermissionUtils.isNormalMode())
                doIfInternetPermissionGranted();
            else
                askInternetPermission();

        }
        else
        {
            Toast.makeText(this, R.string.user_not_given_permission,
                    Toast.LENGTH_LONG).show();
           // ActivityCompat.requestPermissions(this, PermissionsLocation , MY_PERMISSIONS_REQUEST_INTERNET);
            askInternetPermission();
            return;
        }

    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void doIfInternetPermissionGranted()
    {
        //close dialog
        if(dialog != null)
        dialog.dismiss();
        //chk internet connection
        if(isInternetGranted) {
            if (!isNetworkAvailable(context)) {
                Toast.makeText(context, R.string.check_internet_connection,
                        Toast.LENGTH_LONG).show();
            }
        }

            // Perform action on click
            finish();
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("internetpermission",isInternetGranted);
            startActivity(intent);

    }

    private void askInternetPermission()
    {
        //show dialog
        final Dialog dialog = new Dialog(this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.permission_dialog);
        // Set dialog title
        dialog.setTitle("Requesting Permission");

        // set values for custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.pText);
        text.setText(R.string.title_internet_access);
        ImageView image = (ImageView) dialog.findViewById(R.id.pimage);
        image.setImageResource(R.drawable.internet_icon1);
        //for rationale text
        TextView ratText = (TextView) dialog.findViewById(R.id.prationaletext);
        ratText.setText(R.string.detail_internet_access);

        ImageView perImg = (ImageView) dialog.findViewById(R.id.prationaleimage);
        perImg.setImageResource(R.drawable.internet_d3);
        dialog.show();

        //handle for button
        Button denyBtn = (Button)dialog.findViewById(R.id.buttonDeny);
        denyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(context, R.string.access_denied_string,
                        Toast.LENGTH_LONG).show();
                isInternetGranted = false;
                doIfInternetPermissionGranted();

            }
        });

        Button allowBtn = (Button)dialog.findViewById(R.id.buttonAllow);
        allowBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doIfInternetPermissionGranted();
            }
        });

    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
