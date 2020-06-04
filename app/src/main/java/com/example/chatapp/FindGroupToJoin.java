package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindGroupToJoin extends AppCompatActivity {

    private RecyclerView GroupsRecyclerView;
    private ArrayList<Contact> groupsList;
    private FindGroupToJoinAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference groupsInfoRef;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group_to_join);


        groupsInfoRef= FirebaseDatabase.getInstance().getReference().child("Groups Info");

        toolbar=findViewById(R.id.find_groups_to_to_join_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Groups");

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        GroupsRecyclerView=findViewById(R.id.AllGroupsRecyclerView);
        groupsList=new ArrayList<>();
        adapter=new FindGroupToJoinAdapter(groupsList,FindGroupToJoin.this);
        linearLayoutManager=new LinearLayoutManager(FindGroupToJoin.this);
        GroupsRecyclerView.setAdapter(adapter);
        GroupsRecyclerView.setLayoutManager(linearLayoutManager);

        getAllGroups();

    }

    private void getAllGroups() {

        groupsInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChildren())
                {
                    for(DataSnapshot snap:dataSnapshot.getChildren())
                    {
                        String groupName=snap.child("name").getValue().toString();
                        String uid=snap.getKey();
                        String image="";   //till now no image faciltity
                        String status="";  //till now no status feature selected;

                        Contact contact=new Contact(groupName,status,uid,image);
                        groupsList.add(contact);
                        adapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindGroupToJoin.this,"Oops some error occurred!",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
