package com.mimdal.blog.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mimdal.blog.Activities.HomeActivity;
import com.mimdal.blog.Fragments.Interface.OnHideProgressBar;
import com.mimdal.blog.R;





public class ChangeHeaderImageFragment extends DialogFragment implements OnHideProgressBar {

    @Override
    public void hideProgressBar() {
        getDialog().dismiss();
    }

    public interface OnReceivedHeaderLinkListener {

        void OnReceivedHeaderLink(String url);

    }


    FirebaseStorage storage;
    StorageReference storageReference;

    OnReceivedHeaderLinkListener onReceivedHeaderLinkListener;
    private static final String TAG = "ChangeHeaderImageFragme";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);

    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_change_header_image, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setCancelable(false);

        if (getDialog() != null) {

            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Log.d(TAG, "dialog frag is called");
        }
        storage = FirebaseStorage.getInstance();

        String headerImageUri = getArguments().getString("headerImageUri");
        String headerImageFilePath = getArguments().getString("headerImageFilePath");

        if (headerImageFilePath != null) {


            storageReference = storage.getReference("header_image").child(headerImageFilePath);

        }

        if (headerImageUri != null) {

            storageReference.putFile(Uri.parse(headerImageUri)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {


                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    if (onReceivedHeaderLinkListener != null) {

                                        Uri downloadLink = task.getResult();
                                        onReceivedHeaderLinkListener.OnReceivedHeaderLink(downloadLink.toString());
                                    }
                                } else {
                                    getDialog().dismiss();

                                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                    } else {

                        getDialog().dismiss();
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    }

                }
            });

        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


        if (context instanceof HomeActivity) {

            onReceivedHeaderLinkListener = (HomeActivity) getActivity();
        }
    }
}
