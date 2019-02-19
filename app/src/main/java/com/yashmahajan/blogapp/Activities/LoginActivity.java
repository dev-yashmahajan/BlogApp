package com.yashmahajan.blogapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yashmahajan.blogapp.R;

public class LoginActivity extends AppCompatActivity {

    private EditText userEmail, userPassword;
    private ProgressBar loginProgress;
    private Button loginBtn;
    ImageView loginPhoto;
    private TextView regText;


    private FirebaseAuth mAuth;

    private Intent HomeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //inu views
        userEmail = findViewById(R.id.loginEmail);
        userPassword = findViewById(R.id.loginPassword);
        loginProgress = findViewById(R.id.loginProgressBar);
        loginBtn = findViewById(R.id.loginBtn);
        loginProgress.setVisibility(View.INVISIBLE);

        HomeActivity = new Intent(this, com.yashmahajan.blogapp.Activities.HomeActivity.class);

        mAuth = FirebaseAuth.getInstance();

        loginPhoto = findViewById(R.id.userPhoto);

        regText = findViewById(R.id.regText);
        regText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerActivity = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerActivity);
                finish();
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginBtn.setVisibility(View.INVISIBLE);
                loginProgress.setVisibility(View.VISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()){

                    //all fields must be filled, we need to display an error
                    showMessage("Please verify all the fields are correctly entered and try again");
                    loginBtn.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }
                else {

                        signIn(email, password);
                }
            }
        });
    }


    //
    private void signIn(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    loginProgress.setVisibility(View.INVISIBLE);
                    loginBtn.setVisibility(View.VISIBLE);
                    updateUI();
                }
                else {

                    showMessage(task.getException().getMessage());
                    loginProgress.setVisibility(View.INVISIBLE);
                    loginBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    //update ui
    private void updateUI() {

        startActivity(HomeActivity);
        finish();
    }


    //method to show toast messages
    private void showMessage(String msg) {

        Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            updateUI();
        }
    }
}
