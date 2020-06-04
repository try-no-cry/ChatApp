package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class GroupDetailsRecylerAdapter extends RecyclerView.Adapter<GroupDetailsRecylerAdapter.ViewHolder> {


    private ArrayList<Contact> groupMembersList;
    private Context context;

    public GroupDetailsRecylerAdapter(ArrayList<Contact> groupMembersList, Context context) {
        this.groupMembersList = groupMembersList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircularImageView memberImage;
        TextView memberName,memberStatus,timeUpdate;
        Button removeMemberBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImage=itemView.findViewById(R.id.member_image_gm);
            memberName=itemView.findViewById(R.id.member_Name_gm);
            memberStatus=itemView.findViewById(R.id.member_status_gm);
            timeUpdate=itemView.findViewById(R.id.timeUpdate_gm);
            removeMemberBtn=itemView.findViewById(R.id.remove_group_member);



        }
    }

    @NonNull
    @Override
    public GroupDetailsRecylerAdapter.ViewHolder  onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.group_member_row,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupDetailsRecylerAdapter.ViewHolder holder, final int position) {

        if(groupMembersList.get(position).getProfileImage()!="")
        {
            Glide.with(context).load(groupMembersList.get(position).getProfileImage()).into(holder.memberImage);
        }

        holder.memberName.setText(groupMembersList.get(position).getName());

        Toast.makeText(context,"Status: "+groupMembersList.get(position).getStatus(),Toast.LENGTH_LONG).show();
        if(groupMembersList.get(position).getStatus()=="A")
        {
            holder.removeMemberBtn.setVisibility(View.VISIBLE);
            holder.memberStatus.setText("Admin");
        }
        else
        {
            holder.removeMemberBtn.setVisibility(View.GONE);
            holder.memberStatus.setText("Member");
        }

        holder.removeMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeThisMemberFromGroup(position);
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String visit_user_id = groupMembersList.get(position).getUid();
                Intent profileIntent = new Intent(context, ProfileActivity.class);
                profileIntent.putExtra("visit_user_id", visit_user_id);
                context.startActivity(profileIntent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return groupMembersList.size();
    }

    private void removeThisMemberFromGroup(int position) {
        Toast.makeText(context,"Removing: "+position,Toast.LENGTH_SHORT).show();
    }



}
