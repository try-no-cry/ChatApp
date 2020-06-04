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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {


    private View ContactsView;
    private RecyclerView contacts_list_recycler;

    private DatabaseReference ContacsRef, UsersRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView= inflater.inflate(R.layout.fragment_contacts, container, false);
        contacts_list_recycler=ContactsView.findViewById(R.id.contacts_list_recycler);
        contacts_list_recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth=FirebaseAuth.getInstance();
        currentUserID=firebaseAuth.getCurrentUser().getUid();

        ContacsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions
                                            .Builder<Contact>()
                                            .setQuery(ContacsRef,Contact.class)
                                            .build();

        final FirebaseRecyclerAdapter<Contact,ContactsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contact model) {
                                final String userIDs=getRef(position).getKey();

                                UsersRef.child(userIDs)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {
                                                    if(dataSnapshot.child("userState").hasChild("state"))
                                                    {
                                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                                        if(state.equals("online"))
                                                        {
                                                            //online hai to kya karenge
                                                        }
                                                        else if (state.equals("offline"))
                                                        {
                                                            //offline
                                                        }
                                                    }
                                                    else
                                                    {
                                                        //online icon ko invisible kar do
                                                    }

                                                    if(dataSnapshot.hasChild("image"))
                                                    {
                                                        String userImage = dataSnapshot.child("image").getValue().toString();
                                                        Glide.with(getContext()).load(userImage).into(holder.group_dp_list);
                                                    }

                                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                                        holder.group_Name.setText(profileName);
                                                        holder.group_Chat_hint.setText(profileStatus);




                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String visit_user_id = userIDs;
                                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id", visit_user_id);
                                        startActivity(profileIntent);
                                    }
                                });


                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View v=LayoutInflater.from(getContext()).inflate(R.layout.single_chat,parent,false);
                        return new ContactsViewHolder(v);
                    }
                };


        contacts_list_recycler.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        CircularImageView group_dp_list;
        TextView group_Name,group_Chat_hint,timeUpdate;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            group_dp_list=itemView.findViewById(R.id.group_dp_list);
            group_Name=itemView.findViewById(R.id.group_Name);
            group_Chat_hint=itemView.findViewById(R.id.group_Chat_hint);
            timeUpdate=itemView.findViewById(R.id.timeUpdate);


        }
    }
}
