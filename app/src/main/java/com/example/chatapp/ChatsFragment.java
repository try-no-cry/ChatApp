package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

RecyclerView  chats_list;
private FirebaseAuth firebaseAuth;
 private DatabaseReference chatsRefs,usersRef;
 String currentUserID;
    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_chats, container, false);
        chats_list=v.findViewById(R.id.chats_list);

        chats_list.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth=FirebaseAuth.getInstance();

        currentUserID=firebaseAuth.getCurrentUser().getUid();
        chatsRefs= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");



        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options=new FirebaseRecyclerOptions
                                            .Builder<Contact>()
                                            .setQuery(chatsRefs,Contact.class)
                                            .build();

        FirebaseRecyclerAdapter<Contact,ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<Contact, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contact model) {


                    final String usersID=getRef(position).getKey();

                    usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                String profilePicLink="";
                                if(dataSnapshot.hasChild("image"))
                                {
                                    //set the image of suserID
                                    profilePicLink=dataSnapshot.child("image").getValue().toString();
                                    Glide.with(getContext()).load(profilePicLink).into(holder.profilePic);

                                }

                                final String userName=dataSnapshot.child("name").getValue().toString();
                                String userStatus=dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(userName);
                                holder.userStatus.setText("Last Seen: ");
                                holder.timeUpdate.setText(" ");

                                final String finalProfilePicLink = profilePicLink;

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id",usersID);
                                        chatIntent.putExtra("visit_user_name",userName);
                                        chatIntent.putExtra("visit_image", finalProfilePicLink);
                                        startActivity(chatIntent);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v=LayoutInflater.from(getContext()).inflate(R.layout.single_chat,parent,false);

                return new ChatsViewHolder(v);
            }
        };


        adapter.startListening();
        chats_list.setAdapter(adapter);

    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircularImageView profilePic;
        TextView userName,userStatus,timeUpdate;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic=itemView.findViewById(R.id.group_dp_list);
            userName=itemView.findViewById(R.id.group_Name);
            userStatus=itemView.findViewById(R.id.group_Chat_hint);
            timeUpdate=itemView.findViewById(R.id.timeUpdate);
        }
    }
}



