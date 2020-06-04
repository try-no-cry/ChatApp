package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class GroupRequestAdapter extends RecyclerView.Adapter<GroupRequestAdapter.ViewHolder> {

    private ArrayList<Contact> requestsList;
    private Context context;
    private DatabaseReference groupsInfoRef=FirebaseDatabase.getInstance().getReference().child("Groups Info");
    private DatabaseReference groupRequestsRef=FirebaseDatabase.getInstance().getReference().child("Group Requests");
    private DatabaseReference userInGroupsRef=FirebaseDatabase.getInstance().getReference().child("User In Groups");
    private ProgressDialog loadingBar;


    public GroupRequestAdapter(ArrayList<Contact> requestsList, Context context) {
        this.requestsList = requestsList;
        this.context = context;
        this.loadingBar=new ProgressDialog(context);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        CircularImageView userImage;
        TextView userName,groupToJoin,timeUpdate;
        Button AcceptButton, CancelButton;


        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);


            userImage=itemView.findViewById(R.id.group_dp_list);
            userName=itemView.findViewById(R.id.group_Name);
            groupToJoin=itemView.findViewById(R.id.group_Chat_hint);
            timeUpdate=itemView.findViewById(R.id.timeUpdate);

            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);
        }
    }

    @NonNull
    @Override
    public GroupRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.users_display_layout,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupRequestAdapter.ViewHolder holder, final int position) {

        holder.userName.setText(requestsList.get(position).getName()); //name of sender

        String image=requestsList.get(position).getProfileImage(); //senders image

        if(!image.equals(""))
        {
            Glide.with(context).load(image).into(holder.userImage);
        }

        final String[] groupName = {""};
        final String groupIdRequestedtoJoin=requestsList.get(position).getStatus();  //groupID to join

        groupsInfoRef.child(groupIdRequestedtoJoin).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    groupName[0] =dataSnapshot.getValue().toString();
                    holder.groupToJoin.setText("Requested to join "+ groupName[0] + " Group");  //#IMP  remember to send group id in status field
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                notifyDataSetChanged();
                holder.groupToJoin.setText("Unable to load group name..Please refresh this page.");  //#IMP  remember to send group id in status field
            }
        });


        holder.timeUpdate.setText("");

        holder.AcceptButton.setVisibility(View.VISIBLE);
        holder.CancelButton.setVisibility(View.VISIBLE);
        holder.AcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingBar.setTitle("Request");
                loadingBar.setMessage("Accepting the request...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);
                acceptGroupJoinRequest(position,groupIdRequestedtoJoin, groupName[0]);
            }
        });

        holder.CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingBar.setTitle("Request");
                loadingBar.setMessage("Rejecting the request...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);
                rejectGroupJoinRequest(position,groupIdRequestedtoJoin);
            }
        });


    }




    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    private void acceptGroupJoinRequest(final int position, final String groupIdRequestedtoJoin, final String groupName) {

        if(groupName.equals(""))
        {
            Toast.makeText(context,"Unable to load data..Please try again.",Toast.LENGTH_SHORT).show();
            return;
        }

        final String sendersUid= requestsList.get(position).getUid();

        groupsInfoRef.child(groupIdRequestedtoJoin).child(sendersUid).setValue("M")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        //add into User In Groups
                        userInGroupsRef.child(sendersUid).child(groupIdRequestedtoJoin).setValue(groupName).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                String requestKeyToRemove=sendersUid+"₹"+groupIdRequestedtoJoin;

                                groupRequestsRef.child(requestKeyToRemove).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context,requestsList.get(position).getName()+" added to Group "+groupIdRequestedtoJoin,Toast.LENGTH_SHORT).show();
                                        requestsList.remove(position);
                                        notifyDataSetChanged();
                                        loadingBar.dismiss();

                                        //send notification for joining
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingBar.dismiss();
                                        Toast.makeText(context,"Oops..Please try again!",Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(context,"Oops..Please try again!",Toast.LENGTH_SHORT).show();
                            }
                        });

                        //remove from request


                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(context,"Oops..Please try again!",Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void rejectGroupJoinRequest(final int position, final String groupIdRequestedtoJoin) {

        final String sendersUid= requestsList.get(position).getUid();

        String requestKeyToRemove=sendersUid+"₹"+groupIdRequestedtoJoin;

        groupRequestsRef.child(requestKeyToRemove).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,requestsList.get(position).getName()+" added to Group "+groupIdRequestedtoJoin,Toast.LENGTH_SHORT).show();
                requestsList.remove(position);
                notifyDataSetChanged();
                loadingBar.dismiss();
                //send notification for cancellation
                Toast.makeText(context,"Request Rejected Successfully",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(context,"Oops..Please try again!",Toast.LENGTH_SHORT).show();
            }
        });


    }



}
