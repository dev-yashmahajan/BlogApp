package com.yashmahajan.blogapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yashmahajan.blogapp.R;

public class RegisterActivity extends AppCompatActivity {

    ImageView ImgUserPhoto;
    private final int PRegCode = 1;
    static int REQUESCODE =1;
    Uri pickedImageUri;

    private EditText userName, userEmail, userPassword, userPassword2 ;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //inu views
        userName = findViewById(R.id.regName);
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regPassword2);
        loadingProgress = findViewById(R.id.regProgressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String name = userName.getText().toString();
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPassword2.getText().toString();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || !password.equals(password2)){

                    //all fields must be filled, we need to display an error
                    showMessage("Please verify all the fields are correctly entered and try again");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
                else {

                    //everything is correct create new account
                    //CreateUserAccount creates account if email is valid
                    CreateUserAccount(name, email, password);
                }
            }
        });

        ImgUserPhoto = findViewById(R.id.regUserPhoto);
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 23){
                    checkAndRequestForPermission();
                }
                else {
                    openGallery();
                }
            }
        });
    }

    private void CreateUserAccount(final String name, String email, String password) {

        //this method creates user account

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //account created sucessfully
                            showMessage("Account created");
                            //update profile pic and name
                            updateUserInfo(name, pickedImageUri, mAuth.getCurrentUser());

                        }
                        else {
                            //account creation failed
                            showMessage("Account creation failed" +task.getException().getMessage());
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }


    //update user info
    private void updateUserInfo(final String name, Uri pickedImageUri, final FirebaseUser currentUser) {

        //upload user photo to firebase storage and get url
         StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("user_profile_photos");
         final StorageReference imageFilePath = mStorage.child(pickedImageUri.getLastPathSegment());
         imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                 //image uploaded sucessfully
                 //now we get image url

                 imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                     @Override
                     public void onSuccess(Uri uri) {
                        //uri contains user image url

                         UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                 .setDisplayName(name)
                                 .setPhotoUri(uri)
                                 .build();

                         currentUser.updateProfile(profileUpdate)
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            //user info updated sucessfully
                                            showMessage("Register Complete");
                                            updateUI();
                                        }
                                     }
                                 });
                     }
                 });
             }
         });

    }



    //update ui
    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(),HomeActivity.class);
        startActivity(homeActivity);
        finish();
    }


    //method to show toast messages
    private void showMessage(String msg) {

        Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_LONG).show();
    }



    private void openGallery() {
        //TODO: Open Gallery Intent and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);

    }



    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(RegisterActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
            }
            else {

                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PRegCode);
            }
        }
        else {
            openGallery();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null){

            //the user has successfully picked an image
            //we need to save its reference to Uri variable
            pickedImageUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImageUri);

        }
    }
}
