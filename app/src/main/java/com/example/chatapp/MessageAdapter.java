package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;


import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Context context;

    public MessageAdapter (List<Messages> userMessagesList,Context context)
    {
        this.userMessagesList = userMessagesList;
        this.context=context;
//        Toast.makeText(context,"came here21 "+ userMessagesList.get(0).getMessage() ,Toast.LENGTH_LONG).show();
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {



        public TextView senderMessageText, receiverMessageText;
//        public CircularImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;
        public  TextView sender_file_name,receiver_file_name;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
             senderMessageText =  itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText =  itemView.findViewById(R.id.receiver_message_text);
//            receiverProfileImage =  itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            sender_file_name=itemView.findViewById(R.id.sender_file_name);
            receiver_file_name=itemView.findViewById(R.id.receiver_file_name);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

//        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

//        usersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//                if (dataSnapshot.hasChild("image"))
//                {
//                    String receiverImage = dataSnapshot.child("image").getValue().toString();
//
////                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        messageViewHolder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(messageViewHolder.messageSenderPicture.getVisibility()==View.VISIBLE)
                {
                    displayFullView(messages.getMessage());
                    if(!messages.getType().equals("image"))
                    {

                       String url=userMessagesList.get(i).getMessage();
                                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                messageViewHolder.itemView.getContext().startActivity(intent);

                    }
                }
            }
        });

        messageViewHolder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(messageViewHolder.messageReceiverPicture.getVisibility()==View.VISIBLE)
                {
                    displayFullView(messages.getMessage());

                    if(!messages.getType().equals("image"))
                    {

                        String url=userMessagesList.get(i).getMessage();
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        messageViewHolder.itemView.getContext().startActivity(intent);

                    }
                }
            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
//        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.sender_file_name.setVisibility(View.GONE);
        messageViewHolder.receiver_file_name.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);


                messageViewHolder.senderMessageText.setText(messages.getSenderName()+"\n"+messages.getMessage() + "\n" + messages.getTime() + " - " + messages.getDate());
            }
            else
            {
//                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getSenderName()+"\n"+messages.getMessage() + "\n" + messages.getTime() + " - " + messages.getDate());
            }
        }
        else if(fromMessageType.equals("image"))
        {
            if(fromUserID.equals(messageSenderId))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Glide.with(context).load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);

            }
            else
            {
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Glide.with(context).load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        }
        else
        {


            if(fromUserID.equals(messageSenderId))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.sender_file_name.setVisibility(View.VISIBLE);
                messageViewHolder.sender_file_name.setText(messages.getName());
                if(messages.getType().equals("pdf"))
                {
                    messageViewHolder.messageSenderPicture.setImageResource(R.drawable.pdf_icon);
                }
                else messageViewHolder.messageSenderPicture.setImageResource(R.drawable.docx_icon);


            }
            else
            {
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiver_file_name.setVisibility(View.VISIBLE);
                messageViewHolder.receiver_file_name.setText(messages.getName());
                if(messages.getType().equals("pdf"))
                {
                    messageViewHolder.messageReceiverPicture.setImageResource(R.drawable.pdf_icon);
                }
                else messageViewHolder.messageReceiverPicture.setImageResource(R.drawable.docx_icon);
            }



        }
    }

    private void displayFullView(String message) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }


    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
}
