package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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


//   LAUNCHER ACTIVITY
public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private TextInputEditText login_email,login_pwd;
    private TextView pwd_forget,tvSignUp_login;
    private Button btn_login,btn_login_usingPhone;
    private ImageView login_img;
    private AwesomeValidation awesomeValidation;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();
        firebaseAuth=FirebaseAuth.getInstance();
        currentUser=firebaseAuth.getCurrentUser();
        tvSignUp_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegister();
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    private void loginUser()
    {
        String email=login_email.getText().toString().trim();
        String pwd=login_pwd.getText().toString().trim();

         if(awesomeValidation.validate())
        {

                progressDialog.setTitle("Checking Credentials..");
                progressDialog.show();

                    firebaseAuth.signInWithEmailAndPassword(email,pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();

                                      if(!task.isSuccessful())
                                    {
//
                                         Toast.makeText(LoginActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {

                                        Toast.makeText(LoginActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                        sendUserToMainActivity();
                                        finish();
                                    }
                                }
                            });



        }
    }

    private void sendUserToRegister()
    {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));

    }

    private void initialize()
    {
        String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}";

        login_email=findViewById(R.id.login_email);
        login_pwd=findViewById(R.id.login_pwd);
        pwd_forget=findViewById(R.id.pwd_forget);
        tvSignUp_login=findViewById(R.id.tvSignUp_login);
        btn_login=findViewById(R.id.btn_login);
        btn_login_usingPhone=findViewById(R.id.btn_login_usingPhone);
        login_img=findViewById(R.id.login_img);
        awesomeValidation=new AwesomeValidation(ValidationStyle.COLORATION);
        awesomeValidation.addValidation(login_email, Patterns.EMAIL_ADDRESS,"Input Valid EMail-ID.");
        awesomeValidation.addValidation(login_pwd, new SimpleCustomValidation() {
            @Override
            public boolean compare(String s) {
                if(s.trim().isEmpty())
                    return false;
                return true;
            }
        }, "Input Your Password .");
        progressDialog =new ProgressDialog(LoginActivity.this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser!=null)
        {
            sendUserToMainActivity();
            finish();
        }


    }

    private void sendUserToMainActivity() {
        Intent loginIntent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(loginIntent);
    }
}
