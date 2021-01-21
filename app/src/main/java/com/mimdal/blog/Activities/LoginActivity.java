package com.mimdal.blog.Activities;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.mimdal.blog.R;

public class LoginActivity extends BaseActivity {

    EditText login_mail, login_password;
    ImageView login_userPhoto;
    Button login_Btn;
    ProgressBar login_progressBar;
    TextView login_go_to_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();


        login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_Btn.setVisibility(View.INVISIBLE);
                login_progressBar.setVisibility(View.VISIBLE);

                final String mail = login_mail.getText().toString().trim();
                final String password = login_password.getText().toString().trim();


                if (mail.isEmpty() || password.isEmpty()) {

                    showMessage("please verify all fields.");
                    login_Btn.setVisibility(View.VISIBLE);
                    login_progressBar.setVisibility(View.INVISIBLE);

                } else {

                    signIn(mail, password);

                }


            }
        });

        login_go_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }

    private void signIn(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    login_progressBar.setVisibility(View.INVISIBLE);

                    updateUI();

                } else {

                    showMessage(""+task.getException().getMessage());
                    login_Btn.setVisibility(View.VISIBLE);
                    login_progressBar.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    private void updateUI() {

        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();

    }

    private void initViews() {

        login_mail = findViewById(R.id.login_mail);
        login_password = findViewById(R.id.settingCurrentPassword);
        login_userPhoto = findViewById(R.id.settingUserPhoto);
        login_Btn = findViewById(R.id.saveBtn);
        login_progressBar = findViewById(R.id.login_progressBar);
        login_go_to_register = findViewById(R.id.login_go_to_register);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // user is already connected. so redirect to home page
            updateUI();
        }

    }
}