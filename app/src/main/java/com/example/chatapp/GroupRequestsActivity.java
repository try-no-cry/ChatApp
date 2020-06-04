package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupRequestsActivity extends AppCompatActivity {

    private RecyclerView RequestsRecylerView;
    private GroupRequestAdapter adapter;
    private ArrayList<Contact> requestsList;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference groupRequestsRef,usersRef;
    private String currentUserID;
    private Toolbar toolbar;
    //access remaining

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_requests);

        groupRequestsRef= FirebaseDatabase.getInstance().getReference().child("Group Requests");
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        toolbar=findViewById(R.id.group_requests_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Group Requests");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        RequestsRecylerView=findViewById(R.id.new_group_requests_recycler);
        requestsList=new ArrayList<>();
        linearLayoutManager=new LinearLayoutManager(GroupRequestsActivity.this);
        adapter=new GroupRequestAdapter(requestsList,GroupRequestsActivity.this);
        RequestsRecylerView.setLayoutManager(linearLayoutManager);
        RequestsRecylerView.setAdapter(adapter);

        getGroupRequests();
    }

    private void getGroupRequests() {

        if(groupRequestsRef!=null)
        {
            Query query=groupRequestsRef.orderByValue().equalTo(currentUserID);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        for(DataSnapshot snap:dataSnapshot.getChildren())
                        {
                            String key=snap.getKey();



                            int index=key.lastIndexOf("₹");
                            final String senderUid=key.substring(0,index);
                            final String groupIDToJoin=key.substring(index+1);

                            usersRef.child(senderUid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        String senderName=dataSnapshot.child("name").getValue().toString();
                                        String image=dataSnapshot.child("image").getValue().toString();

                                        Contact contact=new Contact(senderName,groupIDToJoin,senderUid,image);
                                        requestsList.add(contact);
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

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
}
