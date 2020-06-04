package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

public class find_friends extends AppCompatActivity {
private Toolbar find_friends_toolbar;
private RecyclerView find_friends_recycler;
private FirebaseDatabase firebaseDatabase;
private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        find_friends_toolbar=findViewById(R.id.find_friends_toolbar);
        find_friends_recycler=findViewById(R.id.find_friends_recycler);
        firebaseDatabase=FirebaseDatabase.getInstance();
        userRef=firebaseDatabase.getReference().child("Users");

        setSupportActionBar(find_friends_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(userRef, Contact.class)
                        .build();


        FirebaseRecyclerAdapter<Contact,findFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contact, findFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final findFriendViewHolder holder, final int position, @NonNull Contact model) {
                    holder.group_Name.setText(model.getName());
                    //image not added
                    holder.group_Chat_hint.setText(model.getStatus());
                    holder.timeUpdate.setText(" ");


                  userRef.child(model.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if(dataSnapshot.exists() && dataSnapshot.hasChild("image"))
                          {
                              Glide.with(find_friends.this).load(dataSnapshot.child("image").getValue().toString()).into(holder.profileImage);
                          }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  }) ;



                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String visit_user_id = getRef(position).getKey();
                            Intent profileIntent = new Intent(find_friends.this, ProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", visit_user_id);
                            startActivity(profileIntent);
                        }
                    });


            }

            @NonNull
            @Override
            public findFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View v=View.inflate(find_friends.this,R.layout.single_chat,parent);
               View v= LayoutInflater.from(find_friends.this).inflate(R.layout.single_chat,parent,false);
                findFriendViewHolder vh=new findFriendViewHolder(v);

                return vh;
            }
        };


        find_friends_recycler.setAdapter(adapter);
        find_friends_recycler.setLayoutManager(new LinearLayoutManager(find_friends.this));
        adapter.startListening();
    }

    public static class findFriendViewHolder extends RecyclerView.ViewHolder
    {

        CircularImageView profileImage;
        TextView group_Name,group_Chat_hint,timeUpdate;

        public findFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.group_dp_list);
            group_Name=itemView.findViewById(R.id.group_Name);
            group_Chat_hint=itemView.findViewById(R.id.group_Chat_hint);
            timeUpdate=itemView.findViewById(R.id.timeUpdate);
        }
    }
}
