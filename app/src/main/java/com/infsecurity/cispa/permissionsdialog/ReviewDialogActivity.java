package com.infsecurity.cispa.permissionsdialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
public class ReviewDialogActivity extends Activity {

    private static final int SELECT_IMAGE = 300;
    Button addPhotosButton = null;
    Button cancelButton = null;
    Button publishButton = null;

    TextView addedPhotosView = null;
    private static final int MY_PERMISSIONS_READ_GALLERY = 12;
    private Context context;
    Activity thisActivityInst = null;
    private boolean isGalleryPermGranted = false;

    String [] permissionsRequired =
            {
                    Manifest.permission.READ_EXTERNAL_STORAGE,

            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_dialog);
        context = getApplicationContext();
        thisActivityInst = this;

        addedPhotosView = (TextView)findViewById(R.id.gridview);
        publishButton = (Button)findViewById(R.id.publish);
        addPhotosButton = (Button) findViewById(R.id.addPhotos);
        cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //close dialog

                finish();
            }
        });
        addPhotosButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //ask for permission to read from gallery
                askGalleryPermissionDialog();
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Toast.makeText(context, R.string.publish_success, Toast.LENGTH_LONG).show();
                finish();
            }
        });



    }

    /**
     * Decide whether to ask permission with the user
     */
    private void askGalleryPermissionDialog() {


            if ((int) Build.VERSION.SDK_INT < 23)
                return;
            else
            {
                String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                {
                    //already permission granted
                   // Toast.makeText(this, "User has granted permission to access gallery .",
                   //         Toast.LENGTH_LONG).show();
                    //open gallery when custom permission given
                    if(PermissionUtils.isNormalMode())
                        openGallery();
                    else {
                        if (isGalleryPermGranted)
                            openGallery();
                        else
                            showAccessGalleryPermissionDialog();
                    }
                    return;
                }
                else
                {
                    //ask permission
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                        if(PermissionUtils.isNormalMode())
                            ActivityCompat.requestPermissions(this, permissionsRequired
                                    , MY_PERMISSIONS_READ_GALLERY);
                        else
                        showAccessGalleryPermissionDialog();
                        return;
                    }


                    //show explanation
                    else
                    {
                        //Explain to the user why we need to read the contacts
                       /* Toast.makeText(this, "Location access is required to show coffee shops nearby.",
                                Toast.LENGTH_LONG).show();
*/
                        if(PermissionUtils.isNormalMode())
                            ActivityCompat.requestPermissions(this, permissionsRequired
                                    , MY_PERMISSIONS_READ_GALLERY);
                        else
                            showAccessGalleryPermissionDialog();

                    }

                }
            }
       }

    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    private void addToList(String filePath)
    {
            ImageView im = new ImageView(context);
            im.setImageBitmap(PermissionUtils.getBitmapFromPath(filePath));
            addedPhotosView.append(filePath+"\n");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        String filePath = null;
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    try {

                        // SDK < API11
                        if (Build.VERSION.SDK_INT < 11)
                            filePath = PermissionUtils.getRealPathFromURI_BelowAPI11(this, data.getData());

                            // SDK >= 11 && SDK < 19
                        else if (Build.VERSION.SDK_INT < 19)
                            filePath = PermissionUtils.getRealPathFromURI_API11to18(this, data.getData());

                            // SDK > 19 (Android 4.4)
                        else
                            filePath = PermissionUtils.getRealPathFromURI_API19(this, data.getData());

                        Toast.makeText(context, "Photo selected" +filePath, Toast.LENGTH_SHORT).show();
                        addToList(filePath);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(context, "Photo selection Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showAccessGalleryPermissionDialog() {

        //show dialog
        final Dialog dialog = new Dialog(this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.permission_dialog);
        // Set dialog title
        dialog.setTitle(R.string.title_gallery_permission_dialog);

        // set values for custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.pText);
        text.setText(R.string.title_gallery_permission);
        ImageView image = (ImageView) dialog.findViewById(R.id.pimage);
        image.setImageResource(R.drawable.gallery);
        //for rationale text
        TextView ratText = (TextView) dialog.findViewById(R.id.prationaletext);
        ratText.setText(R.string.detail_gallery_permission);

        ImageView perImg = (ImageView) dialog.findViewById(R.id.prationaleimage);
        perImg.setImageResource(R.drawable.gallery_d2);
        dialog.show();

        //handle for button
        Button denyBtn = (Button)dialog.findViewById(R.id.buttonDeny);
        denyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                dialog.dismiss();
                Toast.makeText(context, R.string.access_denied_string,
                        Toast.LENGTH_LONG).show();
            }
        });

        Button allowBtn = (Button)dialog.findViewById(R.id.buttonAllow);
        allowBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //close dialog
                dialog.dismiss();
                isGalleryPermGranted = true;
                // Perform action on click
                //ask permission
               // ActivityCompat.requestPermissions(thisActivityInst, permissionsRequired  , MY_PERMISSIONS_READ_GALLERY);
                openGallery();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_READ_GALLERY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this, "Permission to access contact list granted.",
                            Toast.LENGTH_LONG).show();
                    openGallery();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //no expl needed
                    Toast.makeText(this, "Permission Denied,Sorry, Gesto could not provide offer the service :(.",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}
