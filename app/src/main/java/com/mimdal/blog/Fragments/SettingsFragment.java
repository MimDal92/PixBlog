package com.mimdal.blog.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mimdal.blog.Activities.HomeActivity;
import com.mimdal.blog.Helpers.PermissionUtils;
import com.mimdal.blog.Helpers.SingletonSharedPrefs;
import com.mimdal.blog.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {

    ImageView settingUserPhoto;
    EditText settingName;
    EditText settingEmail;
    EditText settingNewPassword;
    Button saveBtn;
    Button cancelBtn;
    ProgressBar settingProgressBar;

    FirebaseUser firebaseUser;
    FirebaseUser firebaseUser2;
    FirebaseStorage storage;
    FirebaseAuth auth;

    Uri settingPickedImageUri = null;

    UserProfileChangeRequest profileUpdate;

    protected final int PreqCode = 300;
    protected final int IReqCode = 400;
    protected String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private Uri finalUri;
    private String downloadLink = "";
    StorageReference imageFilePath;
    private Uri previousSettingPickedImageUri;
    private static final String TAG = "SettingsFragment";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        settingUserPhoto = view.findViewById(R.id.settingUserPhoto);
        settingName = view.findViewById(R.id.settingName);
        settingEmail = view.findViewById(R.id.settingEmail);
        settingNewPassword = view.findViewById(R.id.settingNewPassword);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        settingProgressBar = view.findViewById(R.id.settingProgressBar);

        Glide.with(getActivity()).load(firebaseUser.getPhotoUrl()).into(settingUserPhoto);
        Log.d(TAG, "photoURL: " + firebaseUser.getPhotoUrl());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (firebaseUser != null) {
            settingName.setText(firebaseUser.getDisplayName());
            settingEmail.setText(firebaseUser.getEmail());
            settingNewPassword.setText(SingletonSharedPrefs.getInstance().readString("userPassword", ""));
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (firebaseUser != null) {

                    settingProgressBar.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.INVISIBLE);
                    cancelBtn.setVisibility(View.INVISIBLE);

                    //re-authenticate
                    AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),
                            SingletonSharedPrefs.getInstance().readString("userPassword", ""));
                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                updatePassword();

                            } else {
                                Toast.makeText(getContext(), "ReAuthentication failed. please login again. \n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d(TAG, "ReAuthentication failed. please login again. ");
                                settingProgressBar.setVisibility(View.INVISIBLE);
                                saveBtn.setVisibility(View.VISIBLE);
                                cancelBtn.setVisibility(View.VISIBLE);

                            }

                        }
                    });

                }

            }
        });

        settingUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check for permission and then open gallery

                checkAndRequestForPermission(getActivity());


            }
        });

    }


    private void updatePassword() {

        //update password

        firebaseUser.updatePassword(settingNewPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                SingletonSharedPrefs.getInstance().writeString("userPassword", settingNewPassword.getText().toString());
                Log.d(TAG, "password updates successfully");
                Log.d(TAG, "email: " + firebaseUser.getEmail());
                Log.d(TAG, "password: " + SingletonSharedPrefs.getInstance().readString("userPassword", ""));
                //((HomeActivity) getActivity()).updateNavigationDrawerViews(); // Todo: ??
                updateEmail();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "updateProfile failed. \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "password updates failed");
                Log.d(TAG, e.getMessage());
                settingProgressBar.setVisibility(View.INVISIBLE);
                saveBtn.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.VISIBLE);


            }
        });

    }

    private void updateEmail() {

        Log.d(TAG, "updateEmail method");

        //update email
        firebaseUser.updateEmail(settingEmail.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Log.d(TAG, "email updates successfully");
                ((HomeActivity) getActivity()).updateNavigationDrawerViews();

                updateProfile();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "updateProfile failed. \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "email updates failed");
                Log.d(TAG, e.getMessage());
                settingProgressBar.setVisibility(View.INVISIBLE);
                saveBtn.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.VISIBLE);


            }
        });
    }

    private void updateProfile() {


        if (settingPickedImageUri != null) {


            Log.d(TAG, "uri: " + settingPickedImageUri);
            imageFilePath = storage.getReference().child("users_photo").child(settingPickedImageUri.getLastPathSegment());

            imageFilePath.putFile(settingPickedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                downloadLink = uri.toString();

                                Log.d(TAG, "downloadLink: " + downloadLink);

                                profileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(settingName.getText().toString())
                                        .setPhotoUri(Uri.parse(downloadLink))
                                        .build();

                                firebaseUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            /*
                                             *** FINALLY update profile is finished! ***
                                             */
                                            Log.d(TAG, "updateProfile successfully");
                                            Log.d(TAG, "profile link: " + firebaseUser.getPhotoUrl());
                                            ((HomeActivity) getActivity()).updateNavigationDrawerViews();
                                            Toast.makeText(getContext(), "profile updated successfully", Toast.LENGTH_SHORT).show();
                                            settingProgressBar.setVisibility(View.INVISIBLE);
                                            saveBtn.setVisibility(View.INVISIBLE);
                                            cancelBtn.setVisibility(View.INVISIBLE);

                                            /*
                                                close current fragment and return to hosted activity
                                             */

                                            ((HomeActivity) getActivity()).changeFragment(new HomeFragment(), "Home", true);


                                        } else {

                                            settingProgressBar.setVisibility(View.INVISIBLE);
                                            saveBtn.setVisibility(View.VISIBLE);
                                            cancelBtn.setVisibility(View.VISIBLE);
                                            Toast.makeText(getContext(), "updateProfile failed. \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                            Log.d(TAG, "updateProfile failed");
                                            Log.d(TAG, "profile link: " + firebaseUser.getPhotoUrl());


                                        }

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                settingProgressBar.setVisibility(View.INVISIBLE);
                                saveBtn.setVisibility(View.VISIBLE);
                                cancelBtn.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(), "updateProfile failed. \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "getDownloadUrl failed ");
                                Log.d(TAG, "" + e.getMessage());

                            }
                        });

                    } else {

                        Log.d(TAG, "putFile failed ");

                        Toast.makeText(getContext(), "updateProfile failed. \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        settingProgressBar.setVisibility(View.INVISIBLE);
                        saveBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.VISIBLE);

                    }

                }
            });

        } else {


            profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(settingName.getText().toString())
                    .build();


            firebaseUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        Log.d(TAG, "updateProfile successfully");
                        Log.d(TAG, "profile link: " + firebaseUser.getPhotoUrl());
                        Log.d(TAG, "profile name: " + firebaseUser.getDisplayName());
                        ((HomeActivity) getActivity()).updateNavigationDrawerViews();

                        Toast.makeText(getContext(), "profile updated successfully", Toast.LENGTH_SHORT).show();

                        settingProgressBar.setVisibility(View.INVISIBLE);
                        saveBtn.setVisibility(View.INVISIBLE);
                        cancelBtn.setVisibility(View.INVISIBLE);

                        /*
                         close current fragment and return to hosted activity
                         */

                        ((HomeActivity) getActivity()).changeFragment(new HomeFragment(), "Home", true);


                    } else {

                        Log.d(TAG, "updateProfile failed");
                        Log.d(TAG, "profile link: " + firebaseUser.getPhotoUrl());

                        Toast.makeText(getContext(), "updateProfile failed. \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        settingProgressBar.setVisibility(View.INVISIBLE);
                        saveBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.VISIBLE);


                    }

                }

            });


        }


    }


    protected void openGallery() {


        CropImage.activity()
                .setMinCropResultSize(500, 500)
                .start(getContext(), this);


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


            openGallery();

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                settingPickedImageUri = result.getUri();
                settingUserPhoto.setImageURI(settingPickedImageUri);
                Log.d(TAG, "image uri: " + settingPickedImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, error.getMessage());
            }
        }

    }

    @Override
    public String toString() {
        return "Edit Profile";
    }


}
