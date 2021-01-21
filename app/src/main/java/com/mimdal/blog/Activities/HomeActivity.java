package com.mimdal.blog.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mimdal.blog.Fragments.ChangeHeaderImageFragment;
import com.mimdal.blog.Fragments.DeleteAccountFragment;
import com.mimdal.blog.Fragments.HomeFragment;
import com.mimdal.blog.Fragments.Interface.OnHideProgressBar;
import com.mimdal.blog.Fragments.ProfileFragment;
import com.mimdal.blog.Fragments.SettingsFragment;
import com.mimdal.blog.Models.Post;
import com.mimdal.blog.R;
import com.theartofdev.edmodo.cropper.CropImage;

public class HomeActivity extends BaseActivity implements ChangeHeaderImageFragment.OnReceivedHeaderLinkListener {

    private static final String TAG = "HomeActivity";


    DrawerLayout drawer_layout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView nav_view;

    //navigation drawer header
    ImageView header_bg;
    ImageView header_img;
    TextView header_name;
    TextView header_email;

    ////views of navigation drawer
    FloatingActionButton fab_add_post;
    CoordinatorLayout coordinatorLayout;

    // views of pop up window
    ImageView popup_userPhoto, popup_post_img, popup_add_btn;
    TextView popup_user_id;
    EditText popup_title, popup_description;
    ProgressBar popup_progress;
    ScrollView popup_window;
    private Uri pickedImageUri, headerImageUri;
    FirebaseAuth mAuthH;

    private final int headerImageCode = 300;

    //for visibility manage for loading nav header bg
    OnHideProgressBar onHideProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "HomeActivity: onCreate");
        mAuthH = FirebaseAuth.getInstance();

        initViews();


        toggle = new ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();


        getNavigationDrawerHeader();

        header_bgListener();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        showPopUpWindow();
        setUpPopUpWindow();
        changeFragment(new HomeFragment(), "Home", true);


        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.nav_home:

                        changeFragment(new HomeFragment(), "Home", true);
                        break;

                    case R.id.nav_profile:

                        changeFragment(new ProfileFragment(), "Profile", true);
                        break;

                    case R.id.nav_edit_profile:


                        changeFragment(new SettingsFragment(), "Edit profile", false);
                        break;


                    case R.id.nav_delete_account:

                        getSupportActionBar().setTitle("delete Account");
                        fab_add_post.setVisibility(View.INVISIBLE);
                        DeleteAccountFragment deleteDialog = new DeleteAccountFragment();
                        deleteDialog.show(getSupportFragmentManager(), "myDialog");
                        break;


                    case R.id.nav_signOut:

                        mAuthH.signOut();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        finish();
                        break;
                }

                drawer_layout.closeDrawer(GravityCompat.START);


                return true;
            }
        });

        nav_view.bringToFront();


    }

    public void changeFragment(Fragment fragment, String toolbarTitle, boolean fabVisibility) {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment != null) {

            if (fragment.getClass().equals(currentFragment.getClass())) {
                return;
            }
        }

        /*
            fragment one does not put in back stack
         */

        if (fragment instanceof HomeFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        getSupportActionBar().setTitle(toolbarTitle);

        fab_add_post.setVisibility((fabVisibility) ? View.VISIBLE : View.INVISIBLE);


    }

    private void header_bgListener() {
        header_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = CropImage.activity(headerImageUri)
                        .getIntent(HomeActivity.this);

                startActivityForResult(galleryIntent, headerImageCode);


            }
        });

    }


    private void setUpPopUpWindow() {

        popup_post_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check READ MEMORY permission and then open gallery

                checkAndRequestForPermission(HomeActivity.this);
            }
        });

        popup_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popup_window.getVisibility() == View.VISIBLE) {


                    if (!popup_title.getText().toString().isEmpty()
                            && !popup_description.getText().toString().isEmpty()
                            && pickedImageUri != null) {
                        popup_add_btn.setVisibility(View.INVISIBLE);
                        popup_progress.setVisibility(View.VISIBLE);
                        //todo disable user interaction during upload
                        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        enableViews(false);


                        //TODO create post object and add it to firebase dataBase

                        // 1. upload post image in firebase storage

                        StorageReference storageReference = storage.getReference().child("post_image");
                        final StorageReference imageFilePath = storageReference.child(pickedImageUri.getLastPathSegment());
                        imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Post post = new Post(popup_description.getText().toString(),
                                                uri.toString(),
                                                mAuthH.getUid(),
                                                mAuthH.getCurrentUser().getPhotoUrl().toString(),
                                                popup_title.getText().toString(),
                                                mAuthH.getCurrentUser().getDisplayName());

                                        // 2. add post to firebase database

                                        addPostInDatabase(post);
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                popup_progress.setVisibility(View.INVISIBLE);
                                                popup_add_btn.setVisibility(View.VISIBLE);
                                                // enable user interaction
                                                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                enableViews(true);


                                                showMessage("image can not upload. please try again.");
                                                Log.d(TAG, "" + e.getMessage());

                                            }
                                        });
                            }
                        });


                    } else {

                        popup_add_btn.setVisibility(View.VISIBLE);
                        popup_progress.setVisibility(View.INVISIBLE);
                        showMessage("please verify all input fields and pick an image. ");
                    }

                }
            }
        });
    }

    private void addPostInDatabase(Post post) {

        DatabaseReference databaseReference = database.getReference("posts").push();
        // get post unique id and add it to post fields
        String key = databaseReference.getKey();
        post.setPostKey(key);

        // add post to firebase database

        databaseReference.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                showMessage("post added successfully.");
                popup_progress.setVisibility(View.INVISIBLE);
                popup_add_btn.setVisibility(View.VISIBLE);
                popup_window.setVisibility(View.INVISIBLE);
                // enable user interaction
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                enableViews(true);


                // clear all views for next upload
                popup_post_img.setImageResource(0);
                pickedImageUri = null;
                popup_title.setText("");
                popup_description.setText("");


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                showMessage("Error with uploading post. please try again.");
                popup_progress.setVisibility(View.INVISIBLE);
                popup_add_btn.setVisibility(View.VISIBLE);
                // enable user interaction
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                popup_window.setEnabled(true);

                Log.d(TAG, "Error with uploading post. please try again.");
                Log.d(TAG, "" + e.getMessage());

            }
        });

    }

    private void showPopUpWindow() {

        fab_add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo set transparent layout visible
                popup_window.setVisibility(View.VISIBLE);

                changeStatusBarColor(Color.parseColor("#B3555555"));

                if (mAuthH.getCurrentUser().getPhotoUrl() != null) {

                    Glide.with(HomeActivity.this).load(mAuthH.getCurrentUser().getPhotoUrl()).into(popup_userPhoto);
                }

                //todo get name

                popup_user_id.setText(mAuthH.getCurrentUser().getDisplayName());

                //set up popup window


            }
        });

    }

    private void changeStatusBarColor(int color) {


        // status bar color changes just in api >=21
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }

    }

    private void initViews() {

        toolbar = findViewById(R.id.toolbar);
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        ////views of navigation drawer
        fab_add_post = findViewById(R.id.fab_add_post);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        //// views of pop up window
        popup_userPhoto = findViewById(R.id.popup_userPhoto);
        popup_post_img = findViewById(R.id.popup_post_img);
        popup_add_btn = findViewById(R.id.popup_add_btn);
        popup_user_id = findViewById(R.id.popup_user_id);
        popup_title = findViewById(R.id.popup_title);
        popup_description = findViewById(R.id.popup_description);
        popup_progress = findViewById(R.id.popup_progress);
        popup_window = findViewById(R.id.popup_window);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {


        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);

        } else if (popup_window.getVisibility() == View.VISIBLE) {
            popup_window.setVisibility(View.INVISIBLE);
            changeStatusBarColor(Color.parseColor("#e91e63"));

        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {

            /*
                popBackStack() does not have correct answer
             */
            getSupportFragmentManager().popBackStackImmediate();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            getSupportActionBar().setTitle(currentFragment.toString());
            if (!(currentFragment instanceof SettingsFragment)) {
                fab_add_post.setVisibility(View.VISIBLE);
            } else {
                fab_add_post.setVisibility(View.INVISIBLE);
            }

        } else {

            super.onBackPressed();
        }
    }

    private void enableViews(boolean isEnable) {
        popup_title.setEnabled(isEnable);
        popup_description.setEnabled(isEnable);
        popup_post_img.setEnabled(isEnable);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                pickedImageUri = result.getUri();
                popup_post_img.setImageURI(pickedImageUri);
                Log.d(TAG, "image uri: " + pickedImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, error.getMessage());
            }
        } else if (requestCode == headerImageCode) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                headerImageUri = result.getUri();
//                header_bg.setImageURI(headerImageUri);
                Log.d(TAG, "image uri: " + headerImageUri);

            }


            String getUid = null;
            String stringOfHeaderImageUri = null;
            if (headerImageUri != null) {

                stringOfHeaderImageUri = headerImageUri.toString();
                getUid = mAuth.getUid();

                ChangeHeaderImageFragment changeHeaderImageFragment = new ChangeHeaderImageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("headerImageUri", stringOfHeaderImageUri);
                bundle.putString("headerImageFilePath", getUid);
                changeHeaderImageFragment.setArguments(bundle);
                changeHeaderImageFragment.show(getSupportFragmentManager(), "change header");

                /*
                    the following line is very important. if "headerImageUri" not set to null,
                    new window for pick image not open.
                 */
                headerImageUri = null;
            }

            Log.d(TAG, "image uri: " + stringOfHeaderImageUri);
            Log.d(TAG, "headerImageFilePath: " + getUid);

        }


    }

    public void getNavigationDrawerHeader() {

        View headerView = nav_view.getHeaderView(0);
        header_bg = headerView.findViewById(R.id.header_bg);
        header_img = headerView.findViewById(R.id.header_img);
        header_name = headerView.findViewById(R.id.header_name);
        header_email = headerView.findViewById(R.id.header_email);

        if (mAuth != null) {

            updateNavigationDrawerViews();

            updateNavigationDrawerHeader();
        }


    }

    public void updateNavigationDrawerHeader() {
        storage.getReference("header_image").child(mAuth.getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(HomeActivity.this).load(uri).into(header_bg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                header_bg.setImageResource(R.color.colorPrimary);

            }
        });
    }

    public void updateNavigationDrawerViews() {
        Glide.with(HomeActivity.this).load(mAuth.getCurrentUser().getPhotoUrl()).into(header_img);
        header_name.setText(mAuth.getCurrentUser().getDisplayName());
        header_email.setText(mAuth.getCurrentUser().getEmail());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "HomeActivity: onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "HomeActivity: onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "HomeActivity: onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "HomeActivity: onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "HomeActivity: onDestroy");

    }


    @Override
    public void OnReceivedHeaderLink(String url) {

        Glide.with(HomeActivity.this).load(url).into(header_bg);

        onHideProgressBar = (ChangeHeaderImageFragment) getSupportFragmentManager().findFragmentByTag("change header");
        if (onHideProgressBar !=null) {
            onHideProgressBar.hideProgressBar();
        }


    }


}