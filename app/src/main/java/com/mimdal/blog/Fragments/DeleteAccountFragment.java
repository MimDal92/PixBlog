package com.mimdal.blog.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mimdal.blog.Activities.LoginActivity;
import com.mimdal.blog.Helpers.SingletonSharedPrefs;
import com.mimdal.blog.R;


public class DeleteAccountFragment extends DialogFragment {
    private static final String TAG = "DeleteAccountFragment";

    Button deleteFrag_yesBtn, deleteFrag_noBtn;
    ImageView deleteFrag_attentionImg;
    ProgressBar deleteFrag_progressBar;
    TextView deleteFrag_txt;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    Activity activity;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_delete_account, container, false);

        deleteFrag_yesBtn = view.findViewById(R.id.deleteFrag_yesBtn);
        deleteFrag_noBtn = view.findViewById(R.id.deleteFrag_noBtn);
        deleteFrag_attentionImg = view.findViewById(R.id.deleteFrag_attentionImg);
        deleteFrag_txt = view.findViewById(R.id.deleteFrag_txt);
        deleteFrag_progressBar = view.findViewById(R.id.deleteFrag_progressBar);

        deleteFrag_yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteFrag_yesBtn.setVisibility(View.INVISIBLE);
                deleteFrag_noBtn.setVisibility(View.INVISIBLE);
                deleteFrag_attentionImg.setVisibility(View.INVISIBLE);
                deleteFrag_txt.setVisibility(View.INVISIBLE);
                deleteFrag_progressBar.setVisibility(View.VISIBLE);

                reAuthenticate();

            }
        });

        deleteFrag_noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDialog().dismiss();
            }
        });

        return view;
    }

    private void reAuthenticate() {


        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),
                SingletonSharedPrefs.getInstance().readString("userPassword", ""));
        firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                if (task.isSuccessful()) {

                    firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                getDialog().dismiss();
                                activity.startActivity(new Intent(activity, LoginActivity.class));
                            } else {

                                getDialog().dismiss();
                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d(TAG, "delete failed: " + task.getException().getMessage());

                            }

                        }
                    });


                } else {

                    getDialog().dismiss();

                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();


                }


            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


        if (context instanceof Activity) {

            activity = (Activity) context;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        Window dialogWindow = getDialog().getWindow();

        Point size = new Point();
        if (dialogWindow != null) {

            dialogWindow.getWindowManager().getDefaultDisplay().getSize(size);

            dialogWindow.setLayout((int) (size.x * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
            dialogWindow.setGravity(Gravity.CENTER);
        }

    }
}