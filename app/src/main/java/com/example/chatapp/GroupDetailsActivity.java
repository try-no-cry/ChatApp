package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class GroupDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircularImageView groupImage;
    private RecyclerView groupMembersList;
    private FloatingActionButton addNewMemberButton;
    private TextView groupNametv;
    private String groupName,groupID;
    private DatabaseReference groupIDInfoRef,usersRef;
    private ArrayList<Contact> groupMembersInfoList;
    private GroupDetailsRecylerAdapter adapter;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);



        intialize();
        groupName=getIntent().getExtras().getString("groupName");
        groupID=getIntent().getExtras().getString("groupID");
        groupNametv.setText(groupName);
        
        groupIDInfoRef= FirebaseDatabase.getInstance().getReference().child("Groups Info").child(groupID);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();


        groupIDInfoRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                                        
                    for(DataSnapshot ds:dataSnapshot.getChildren())
                    {
                        if(ds.getValue().toString().equals("M"))
                            addNewMemberButton.hide();
                        else addNewMemberButton.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        getMembersInfo();

        addNewMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                goToSelectFromUsers();
                
            }
        });


    }




    private void intialize() {
        toolbar=findViewById(R.id.group_details_bar);
        groupImage=findViewById(R.id.group_image);
        groupMembersList=findViewById(R.id.group_members_list);
        addNewMemberButton=findViewById(R.id.add_new_grp_member_btn);
        groupMembersInfoList=new ArrayList<>();
        adapter=new GroupDetailsRecylerAdapter(groupMembersInfoList,GroupDetailsActivity.this);
        groupMembersList.setLayoutManager(new LinearLayoutManager(GroupDetailsActivity.this));
        groupMembersList.setAdapter(adapter);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        groupNametv = findViewById(R.id.custom_user_name);
        //circular image view of toolbar is not defined here..mann kare to kar dena baad me
    }


    private void getMembersInfo() {

        groupIDInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChildren())
                {
                    for(DataSnapshot snap:dataSnapshot.getChildren())
                    {
                        String uid=snap.getKey().toString();
                        final String pos=snap.getValue().toString();

                        if(uid!="name")
                        {
                            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        String name=dataSnapshot.child("name").getValue().toString();
                                        String status=pos;  //putting status as admin or member
                                        String image=dataSnapshot.child("image").getValue().toString();
                                        String uid=dataSnapshot.child("uid").getValue().toString();

                                        Contact contact=new Contact(name,status,uid,image);
                                        groupMembersInfoList.add(contact);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
        
    }

    private void goToSelectFromUsers()
    {
        // startActivity(new Intent(GroupDetailsActivity.this,find_friends.class));
        Toast.makeText(GroupDetailsActivity.this,"Floating button clicked..",Toast.LENGTH_SHORT).show();
    }
}
