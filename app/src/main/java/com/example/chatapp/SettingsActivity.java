package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private CircularImageView settg_profile_pic;
    private TextInputEditText edit_profile;
    private TextInputEditText edit_status;
    private Button btn_updateStatus;
    ;
    private AwesomeValidation awesomeValidation;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference userProfileImagesRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initialize();

        if(currentUser!=null)
        {

                retrieveUserData();
                settg_profile_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent galleryIntent=new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent,GALLERY_PICK);
                    }
                });
         }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null)
        {
            Uri resultUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                {
                    loadingBar.setTitle("Profile Pic");
                    loadingBar.setMessage("Updating Your Profile Picture!!");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    Uri resultUri = result.getUri();
                    final StorageReference filePath= userProfileImagesRef.child(currentUser.getUid() +".jpg");
                    filePath.putFile(resultUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadURL=uri.toString();

                                            databaseReference.child("Users").child(currentUser.getUid()).child("image").setValue(downloadURL)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(SettingsActivity.this,"Profile Image Updated Successfully",Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    });

                                        }
                                    });
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SettingsActivity.this,"Please try again!",Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                        }
                    });
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void retrieveUserData() {
        databaseReference.child("Users").child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")))
                        {

                                String name=dataSnapshot.child("name").getValue().toString();
                                String status=dataSnapshot.child("status").getValue().toString();
                                String img=dataSnapshot.child("image").getValue().toString();
                            Glide.with(SettingsActivity.this).load(img).into(settg_profile_pic);
                                edit_profile.setText(name);
                                edit_status.setText(status);



                        }
                        else if(dataSnapshot.exists() && (dataSnapshot.hasChild("name")))
                        {
                            String name=dataSnapshot.child("name").getValue().toString();
                            String status=dataSnapshot.child("status").getValue().toString();
                            edit_profile.setText(name);
                            edit_status.setText(status);

                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this,"Set your profile here",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initialize()
    {

        firebaseAuth=FirebaseAuth.getInstance();
        currentUser=firebaseAuth.getCurrentUser();
        databaseReference=FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef=FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar=new ProgressDialog(this);

        settg_profile_pic=findViewById(R.id.settg_profile_pic);
        edit_profile=findViewById(R.id.edit_profile);
        edit_status=findViewById(R.id.edit_status);
        btn_updateStatus=findViewById(R.id.btn_updateStatus);

        Log.d("abc",edit_profile.toString());
        awesomeValidation=new AwesomeValidation(ValidationStyle.COLORATION);

        awesomeValidation.addValidation(edit_profile, new SimpleCustomValidation() {
            @Override
            public boolean compare(String s) {
                if(s.length()==0)
                    return false;
                return  true;
            }
        }, "Please Input Your Name");

        awesomeValidation.addValidation(edit_status, new SimpleCustomValidation() {
            @Override
            public boolean compare(String s) {
                if(s.length()==0)
                return false;
                return  true;
            }
        }, "Put Your Status!");

        btn_updateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus();
            }
        });

    }

    private void updateStatus() {
        String uName=edit_profile.getText().toString().trim();
        String status=edit_status.getText().toString().trim();

        if(awesomeValidation.validate())
        {
//            HashMap<String,String > profileMap=new HashMap<>();
//                profileMap.put("name",uName);
//                profileMap.put("status",status);
//                profileMap.put("uid",currentUser.getUid());


                DatabaseReference ref=databaseReference.child("Users").child(currentUser.getUid());

            ref.child("name").setValue(uName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this,"Name Updated Successfully...",Toast.LENGTH_SHORT).show();
                            recreate();
//                            sendUserToMainActivity();

                        }
                        else Toast.makeText(SettingsActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                });

            ref.child("status").setValue(status)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SettingsActivity.this,"Status Updated Successfully...",Toast.LENGTH_SHORT).show();
                                recreate();
//                            sendUserToMainActivity();

                            }
                            else Toast.makeText(SettingsActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                        }
                    });

            ref.child("uid").setValue(currentUser.getUid())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SettingsActivity.this,"Status Updated Successfully...",Toast.LENGTH_SHORT).show();
                                recreate();
//                            sendUserToMainActivity();

                            }
                            else Toast.makeText(SettingsActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                        }
                    });


        }

    }

    private void sendUserToMainActivity() {
        Intent loginIntent=new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(loginIntent);
    }


}
