package com.mimdal.blog.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mimdal.blog.Helpers.SingletonSharedPrefs;
import com.mimdal.blog.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterActivity extends BaseActivity {

    ImageView regUerPhoto;
    EditText registerPassword, registerConfirmPassword, registerName, registerEmail;
    Button regBtn;
    ProgressBar regProgressBar;

    Uri pickedImageUri;
    static final String PROFILE_IMAGE_PATH = "profile_image_path";
    private static final String TAG = "RegisterActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();





        regUerPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("LOG", "image clicked");
                checkAndRequestForPermission(RegisterActivity.this);

            }
        });


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regBtn.setVisibility(View.INVISIBLE);
                regProgressBar.setVisibility(View.VISIBLE);


                final String userName = registerName.getText().toString().trim();
                final String email = registerEmail.getText().toString().trim();
                final String password = registerPassword.getText().toString().trim();
                final String confirmPassword = registerConfirmPassword.getText().toString().trim();

                // all fields must be filled. so, an error message is show.

                if (userName.equals("") || email.equals("") || password.equals("") || confirmPassword.equals("")) {

                    regBtn.setVisibility(View.VISIBLE);
                    regProgressBar.setVisibility(View.INVISIBLE);

                    showMessage("please verify all fields");
                } else {

                    /*
                        store inputs in sharedPreferences for future purpose
                     */
                    SingletonSharedPrefs.getInstance().writeString("username",userName);
                    SingletonSharedPrefs.getInstance().writeString("userEmail",email);
                    SingletonSharedPrefs.getInstance().writeString("userPassword",password);
                    // every thing is ok and all fields are filled.
                    // now account is created
                    createUserAccount(email, userName, password);

                }
            }
        });

    }

    private void createUserAccount(String email, final String userName, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            showMessage("account created");
                            // userName and image uri are part of user information
                            // mAuth.getCurrentUser() is FirebaseUser
                            if (pickedImageUri == null) {
                                pickedImageUri = Uri.parse("android.resource://com.mimdal.blog/" + R.drawable.default_user);
                            }
                            updateUserInfo(userName, pickedImageUri, mAuth.getCurrentUser());

                        } else {

                            regBtn.setVisibility(View.VISIBLE);
                            regProgressBar.setVisibility(View.INVISIBLE);
                            showMessage("account creation failed\n"+task.getException().getMessage());
                            Log.d(TAG, "" + task.getException().getMessage());

                        }
                    }
                });
    }

    //update user photo and userName
    private void updateUserInfo(final String userName, final Uri pickedImageUri, final FirebaseUser currentUser) {
        //upload user photo in firebase storage and get url
        StorageReference storageRef = storage.getReference().child("users_photo");
        final StorageReference imageFilePath = storageRef.child(pickedImageUri.getLastPathSegment()+".jpg");

        imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // image is uploaded successfully
                // now we get image download url

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Log.d(TAG, "image download url: " + uri);

                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    regProgressBar.setVisibility(View.INVISIBLE);
                                    showMessage("register completed");

                                    updateUI();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                // updateProfile is failed.

                                regBtn.setVisibility(View.VISIBLE);
                                regProgressBar.setVisibility(View.INVISIBLE);
                                showMessage("account info couldn't verified. please try again.\n"+e.getMessage());
                                Log.d(TAG, "" + e.getMessage());

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        // image can not be uploaded. no download link to get

                        showMessage("account info couldn't verified. please try again. \n"+e.getMessage());

                        Log.d(TAG, "" + e.getMessage());

                    }
                });
            }
        });

    }

    private void updateUI() {

        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
        finish();

    }


    private void initViews() {

        regUerPhoto = findViewById(R.id.settingUserPhoto);
        registerPassword = findViewById(R.id.settingCurrentPassword);
        registerConfirmPassword = findViewById(R.id.settingNewPassword);
        registerName = findViewById(R.id.settingName);
        registerEmail = findViewById(R.id.settingEmail);
        regBtn = findViewById(R.id.saveBtn);
        regProgressBar = findViewById(R.id.login_progressBar);
        registerName.requestFocus();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                pickedImageUri = result.getUri();
                regUerPhoto.setImageURI(pickedImageUri);
                Log.d(TAG, "image uri: "+pickedImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG,error.getMessage());
            }
        }
    }
}