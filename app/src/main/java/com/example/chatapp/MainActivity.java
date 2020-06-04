package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout main_tabs;
    private ViewPager main_tabs_pager;
    private TabsAccessAdapter tabsAccessAdapter;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference,groupsInfoRef,userInGroups;
    private String currentUserId;
    private ProgressDialog loadingBar;
    private AlertDialog.Builder builder;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatApp");

        loadingBar=new ProgressDialog(MainActivity.this);
        firebaseAuth=FirebaseAuth.getInstance();
        currentUser=firebaseAuth.getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        groupsInfoRef=databaseReference.child("Groups Info");
        userInGroups=databaseReference.child("User In Groups");
        currentUserId=currentUser.getUid();

        main_tabs_pager=findViewById(R.id.main_tabs_pager);
        tabsAccessAdapter=new TabsAccessAdapter(getSupportFragmentManager());
        main_tabs_pager.setAdapter(tabsAccessAdapter);
        main_tabs=findViewById(R.id.main_tabs);
        main_tabs.setupWithViewPager(main_tabs_pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top,menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.new_group:
                make_newGroup();
                break;

            case R.id.find_friends_menu_btn:
                goToFindFriendsActivity();
                break;
            case R.id.menu_settings:
                goToSettings();
               break;

            case R.id.join_group_menu_btn:
                showAllGroups();
                break;

            case R.id.menu_logout:
                logOutUser();
                break;

            case R.id.new_grp_requests_menu:
                startActivity(new Intent(MainActivity.this,GroupRequestsActivity.class));
                break;

            default: return false;
        }

        return true;
    }

    private void showAllGroups() {

        startActivity(new Intent(MainActivity.this,FindGroupToJoin.class));

    }



    private void goToFindFriendsActivity() {
        startActivity(new Intent(MainActivity.this,find_friends.class));
        //not finishing for the back button to work
    }

    private void make_newGroup() {


       builder =new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);

        View v= LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_create_new_grp_layout,null,false);
            final EditText et=v.findViewById(R.id.set_new_group_name);
            Button createGroupBtn=v.findViewById(R.id.create_new_grp_btn);

        builder.setView(v);

        builder.show();
        builder.create();

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName=et.getText().toString().trim();

                if(groupName.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"Please write group name",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("New group");
                    loadingBar.setMessage("Creating the group "+groupName);
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    createTheGroup(groupName);

                }
            }
        });



    }

    private void createTheGroup(final String groupName) {

       final String groupId= groupsInfoRef.push().getKey();
       Toast.makeText(this,groupId,Toast.LENGTH_SHORT).show();

        HashMap<String,String> mp=new HashMap<>();
        mp.put("name",groupName);
        mp.put(currentUserId,"A");

       groupsInfoRef.child(groupId).setValue(mp)
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {

                   }
               });


        databaseReference.child("Groups").child(groupId).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                userInGroups.child(currentUserId).child(groupId).setValue(groupName).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingBar.dismiss();

                                        Toast.makeText(MainActivity.this,"Group Created Successfully",Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingBar.dismiss();
                                        Toast.makeText(MainActivity.this,"Some Error Occurred..Please try again!",Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                            else
                            {
                                loadingBar.dismiss();
                                Toast.makeText(MainActivity.this,task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                    }
                });
    }

    private void goToSettings() {
        startActivity(new Intent(MainActivity.this,SettingsActivity.class));
    }

    private void logOutUser() {
        firebaseAuth.signOut();
        currentUser=null;
        sendUserToLogin();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser==null)
        {
            sendUserToLogin();
            finish();
        }
    }

    private void sendUserToLogin() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(currentUser!=null)
        {
            finish();
        }
    }
}
