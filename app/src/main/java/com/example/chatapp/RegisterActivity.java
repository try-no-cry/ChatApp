package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private ImageView register_img;
    private TextInputEditText register_email,register_pwd;
    private Button btn_Register;
    private TextView tvLogin_register;
    private AwesomeValidation awesomeValidation;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize();
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        tvLogin_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLogin();
            }
        });
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

    }

    private void registerUser() {
        final String email=register_email.getText().toString().trim();
        String pwd=register_pwd.getText().toString().trim();

         if(awesomeValidation.validate())
         {
             loadingBar.setTitle("Registering You..");
             loadingBar.setMessage("Breath in. We are creating your account.");
             loadingBar.setCanceledOnTouchOutside(true);
             loadingBar.show();
             //validation successful

                firebaseAuth.createUserWithEmailAndPassword(email,pwd)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                loadingBar.dismiss();
                                 if(task.isSuccessful())
                                {

                                    currentUser=firebaseAuth.getCurrentUser();
                                    String uid=currentUser.getUid();
                                    databaseReference.child("Users").child(uid).setValue("");

                                    Toast.makeText(RegisterActivity.this,"Successfully Registered",Toast.LENGTH_SHORT).show();
                                    goToSettings();
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(RegisterActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });

          }
    }

    private void goToSettings() {
        startActivity(new Intent(RegisterActivity.this,SettingsActivity.class));
    }

    private void sendUserToLogin() {
        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
    }

    private void initialize()
    {
        String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}";

        register_img=findViewById(R.id.register_img);
        register_email=findViewById(R.id.register_email);
        register_pwd=findViewById(R.id.register_pwd);
        btn_Register=findViewById(R.id.btn_Register);
        tvLogin_register=findViewById(R.id.tvLogin_register);
        awesomeValidation=new AwesomeValidation(ValidationStyle.COLORATION);
        awesomeValidation.addValidation(register_email, Patterns.EMAIL_ADDRESS,"Input valid Email-ID.");
        awesomeValidation.addValidation(register_pwd, new SimpleCustomValidation() {
            @Override
            public boolean compare(String s) {
                if(s.toString().trim().length()<6 )
                    return false;
                return true;
            }
        },"Input your Password of length more than 6 characters.");

        loadingBar=new ProgressDialog(RegisterActivity.this);
    }
}
