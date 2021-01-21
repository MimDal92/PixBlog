package com.mimdal.blog.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.mimdal.blog.Helpers.PermissionUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    protected FirebaseStorage storage;
    protected FirebaseDatabase database;
    protected final int PreqCode = 100;
    protected final int IReqCode = 200;
    protected String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    protected void showMessage(String message) {

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IReqCode);


    }

    protected void checkAndRequestForPermission(final Activity activity) {

        if (Build.VERSION.SDK_INT >= 23) {

            new PermissionUtils(activity, new PermissionUtils.PermissionAskListener() {
                @Override
                public void onPermissionGranted() {

                    openGallery();
                }

                @Override
                public void onPermissionRequest() {


                    ActivityCompat.requestPermissions(activity,
                            permissions, PreqCode);
                }

                @Override
                public void onPermissionPreviouslyDenied() {

                    new AlertDialog.Builder(activity)
                            .setTitle("permission required")
                            .setMessage("permission(s) needed for app to work well.")
                            .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    onPermissionRequest();
//                                ActivityCompat.requestPermissions(RegisterActivity.this,
//                                        permissions, PreqCode);
                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }

                @Override
                public void onPermissionDisabled() {

                    new AlertDialog.Builder(activity)
                            .setTitle("permission disabled")
                            .setMessage("enable permission in following path. setting>user>permission")
                            .setPositiveButton("go to setting", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            })
                            .create()
                            .show();

                }
            }, permissions).checkPermissions();

        } else {


            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropResultSize(500, 500)
                    .start(activity);


            //openGallery();

        }






    }


}