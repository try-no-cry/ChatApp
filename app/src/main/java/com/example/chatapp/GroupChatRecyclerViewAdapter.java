package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class GroupChatRecyclerViewAdapter extends RecyclerView.Adapter<GroupChatRecyclerViewAdapter.MyViewHolder> {

    ArrayList<SingleChat> chats;
    Context context;

    public GroupChatRecyclerViewAdapter(ArrayList<SingleChat> chats, Context context) {
        this.chats = chats;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

    CircularImageView group_dp_list;
    TextView group_Name,group_Chat_hint,timeUpdate;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            group_dp_list=itemView.findViewById(R.id.group_dp_list);
            group_Name=itemView.findViewById(R.id.group_Name);
            group_Chat_hint=itemView.findViewById(R.id.group_Chat_hint);
            timeUpdate=itemView.findViewById(R.id.timeUpdate);



        }
    }


    @NonNull
    @Override
    public GroupChatRecyclerViewAdapter.MyViewHolder  onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(context).inflate(R.layout.single_chat,parent,false);
//        View v=LayoutInflater.from(context).inflate(R.layout.custom_messages_layout)
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatRecyclerViewAdapter.MyViewHolder holder, int position) {
       final String groupID=chats.get(position).getGroupID();
       final String groupName=chats.get(position).getGroupName();
//       holder.itemView.setTag((groupID));

       holder.group_Name.setText(groupName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToChat(groupID,groupName);

            }
        });



    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    private void goToChat(String groupID,String group_name) {

        if(groupID.isEmpty())
            return;

        Intent intent=new Intent(context,GroupChatActivity.class);
        intent.putExtra("groupName",  group_name);
        intent.putExtra("groupID",groupID);
        context.startActivity(intent);

    }


}
