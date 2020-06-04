package com.example.chatapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class FindGroupToJoinAdapter extends RecyclerView.Adapter<FindGroupToJoinAdapter.ViewHolder> {

    private ArrayList<Contact> groupsList;
    private Context context;
    private DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
    private DatabaseReference groupsInfoRef=FirebaseDatabase.getInstance().getReference().child("Groups Info");
    private String currentUserId= FirebaseAuth.getInstance().getUid();
    private DatabaseReference userInGroupsRef=FirebaseDatabase.getInstance().getReference().child("User In Groups");

    public FindGroupToJoinAdapter(ArrayList<Contact> groupsList, Context context) {
        this.groupsList = groupsList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircularImageView groupImage;
        TextView groupName,groupAbout,timeUpdate;
        Button sendJoinRequestBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage=itemView.findViewById(R.id.member_image_gm);
            groupName=itemView.findViewById(R.id.member_Name_gm);
            groupAbout=itemView.findViewById(R.id.member_status_gm);
            timeUpdate=itemView.findViewById(R.id.timeUpdate_gm);
            sendJoinRequestBtn=itemView.findViewById(R.id.remove_group_member);



        }
    }

    @NonNull
    @Override
    public FindGroupToJoinAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.group_member_row,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FindGroupToJoinAdapter.ViewHolder holder, final int position) {

        final String gID=groupsList.get(position).getUid();

        final String uniqueKey=currentUserId+"₹"+gID;  //cUID and rupee symbol and groupid to form a key  #IMP

        String image=groupsList.get(position).getProfileImage();

        if(!image.equals(""))
        {
            Glide.with(context).load(image).into(holder.groupImage);
        }

        holder.groupName.setText(groupsList.get(position).getName());
        holder.groupAbout.setText(groupsList.get(position).getStatus());

        holder.sendJoinRequestBtn.setVisibility(View.GONE);

        userInGroupsRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild(gID))
                {
                    if(holder.sendJoinRequestBtn.getVisibility()==View.GONE)
                            holder.sendJoinRequestBtn.setText("Already a member");
                }
                else
                {
                    notAMember(holder,position,gID,uniqueKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }




    @Override
    public int getItemCount() {
        return groupsList.size();
    }


    private void notAMember(final ViewHolder holder, final int position, final String gID, final String uniqueKey)
    {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Group Requests"))
                {
                    Toast.makeText(context,"Yes",Toast.LENGTH_SHORT).show();
                    databaseReference.child("Group Requests").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(uniqueKey))
                            {
                                Toast.makeText(context,"Request Already Sent",Toast.LENGTH_SHORT).show();
                                if(holder.sendJoinRequestBtn.getVisibility()==View.GONE)
                                    holder.sendJoinRequestBtn.setVisibility(View.VISIBLE);
                                holder.sendJoinRequestBtn.setBackgroundColor(Color.RED);
                                holder.sendJoinRequestBtn.setText("Cancel Join Request");
                            }
                            else
                            {
                                if(holder.sendJoinRequestBtn.getVisibility()==View.GONE)
                                    holder.sendJoinRequestBtn.setVisibility(View.VISIBLE);
                                holder.sendJoinRequestBtn.setBackgroundColor(Color.GREEN);
                                holder.sendJoinRequestBtn.setText("Send Join Request");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(context,"Some Error Occurred. Please reload the page.",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    if(holder.sendJoinRequestBtn.getVisibility()==View.GONE)
                        holder.sendJoinRequestBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(context,"NO",Toast.LENGTH_SHORT).show();
                    holder.sendJoinRequestBtn.setBackgroundColor(Color.GREEN);
                    holder.sendJoinRequestBtn.setText("Send Join Request");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,"Some Error Occurred. Please reload the page.",Toast.LENGTH_SHORT).show();
            }
        });




        holder.sendJoinRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(holder.sendJoinRequestBtn.getText().toString().equals("Send Join Request"))
                {
                    sendJoiningRequest(position,holder,gID,uniqueKey);
                }
                else
                {
                    cancelJoiningRequest(position,holder,uniqueKey);
                }

            }
        });

    }

    private void sendJoiningRequest(final int position, final ViewHolder holder, String gID, final String uniqueKey) {


        Toast.makeText(context,uniqueKey,Toast.LENGTH_LONG).show();
        groupsInfoRef.child(gID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChildren())
                {
                    for(DataSnapshot snap:dataSnapshot.getChildren())
                    {
                        String pos=snap.getValue().toString();

                        if(pos.equals("A"))
                        {
                           String toID=snap.getKey();

                           //now we have all info

                            databaseReference.child("Group Requests").child(uniqueKey).setValue(toID).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"Request Sent Successfully",Toast.LENGTH_SHORT).show();
                                    if(holder.sendJoinRequestBtn.getVisibility()==View.GONE)
                                        holder.sendJoinRequestBtn.setVisibility(View.VISIBLE);

                                    holder.sendJoinRequestBtn.setBackgroundColor(Color.RED);
                                    holder.sendJoinRequestBtn.setText("Cancel Join Request");

                               }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
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

    private void cancelJoiningRequest(int position, final ViewHolder holder, String uniqueKey)
    {
        databaseReference.child("Group Requests").child(uniqueKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Joining request cancelled.",Toast.LENGTH_SHORT).show();

                if(holder.sendJoinRequestBtn.getVisibility()==View.GONE)
                    holder.sendJoinRequestBtn.setVisibility(View.VISIBLE);
                holder.sendJoinRequestBtn.setBackgroundColor(Color.GREEN);
                holder.sendJoinRequestBtn.setText("Send Join Request");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Error. Please try again.",Toast.LENGTH_SHORT).show();
            }
        });
    }


}
