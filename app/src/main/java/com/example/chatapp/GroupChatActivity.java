package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private TextView groupNametv;
    private CircularImageView groupImage;
    private ScrollView group_scroll_view;
    private Toolbar ChatToolBar;
    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;

    DatabaseReference groupIDRef,groupMessageKeyRef;
    private  String groupName,groupID,currentUserID,currentUserName,currentDate,currentTime;  //receive grp name from bundle
    FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
//    private LinearLayout chat_messages_layout;
    private RecyclerView GroupListRecyler;
    private MessageAdapter messageAdapter;
    private final List<Messages> messagesList = new ArrayList<>();
    private String checker="",myUrL;
    private Uri fileUri;
    private StorageTask uploadTask;
    private String fileName="";
    private ProgressDialog loadingBar;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupID=getIntent().getExtras().getString("groupID");
        groupName=getIntent().getExtras().getString("groupName");
        firebaseAuth=FirebaseAuth.getInstance();
        currentUserID=firebaseAuth.getCurrentUser().getUid();



        groupIDRef= FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");

//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getUserInfo();
        intialize();

        groupNametv.setText(groupName);

        groupNametv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(GroupChatActivity.this,GroupDetailsActivity.class);
                intent.putExtra("groupID",groupID);
                intent.putExtra("groupName",groupName);
                startActivity(intent);
            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
//                scrollDown();

            }
        });

        Toast.makeText(this,groupName,Toast.LENGTH_SHORT).show();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]{
                        "Images",
                        "PDF File"
                        ,"Docs file"
                };

                AlertDialog.Builder builder=new AlertDialog.Builder(GroupChatActivity.this);

                builder.setTitle("Select the file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                        {
                            checker="image";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select image.."),1);
                        }
                        else if(i==1)
                        {
                            checker="pdf";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select pdf file.."),1);

                        }
                        else if(i==2)
                        {
                            checker="docx";


                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                            intent.setType("application/docx");
                            startActivityForResult(intent.createChooser(intent,"Select MS word.."),1);
                        }
                    }
                });

                builder.show();
            }
        });



//
//        String messageKey=groupIDRef.push().getKey();
//        groupMessageKeyRef=groupIDRef.child(messageKey);

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                 }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupIDRef
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
//
                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        GroupListRecyler.smoothScrollToPosition(GroupListRecyler.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


//        btn_send_msg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendMessage();
//            }
//        });
    }

//    private void scrollDown() {
//        group_scroll_view.post(new Runnable() {
//            @Override
//            public void run() {
//                group_scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
//            }
//        });
//    }



    @Override
    protected void onStart() {
        super.onStart();

        groupIDRef.keepSynced(true);
//        groupIDRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if(dataSnapshot.exists())
//                {
//
//
//                    getAllMessages(dataSnapshot);
//
//
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if(dataSnapshot.exists())
//                {
//
//
//                    getAllMessages(dataSnapshot);
//
//
//                }
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//        scrollDown();
        groupIDRef.keepSynced(true);
    }

//    private void getAllMessages(DataSnapshot dataSnapshot) {
//
//
//
//        Iterator iterator=dataSnapshot.getChildren().iterator();
//        String uid,date,time,message;
//
//        while ((iterator.hasNext()))
//        {
//
//            date= ((DataSnapshot)iterator.next()).getValue().toString();
//            message= ((DataSnapshot)iterator.next()).getValue().toString();
//            time= ((DataSnapshot)iterator.next()).getValue().toString();
//            uid= ((DataSnapshot)iterator.next()).getValue().toString();
//            getUserInfo(uid,date,message,time);   //the variable of username gets set with this
//
//
//        }
//
//}

//    private void getUserInfo(final String uid, final String date, final String message, final String time) {
//
////I commented below code since we don't need name as it will change with time. However we'll store uid as it is irrelevant of name change but now ok
//
//        userRef.child(uid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists())
//                {
//                    currentUserName=dataSnapshot.child("name").getValue().toString();
//                    setChatData(uid,date,message,time);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//
//    }

//    private void setChatData(String uid, String date, String message, String time) {
//
//        if(uid.equals(currentUserID))
//        {
//            //that means this is my message
//            View v=LayoutInflater.from(GroupChatActivity.this).inflate(R.layout.my_single_message,chat_messages_layout,false);
//            TextView tv_my_single_message=v.findViewById(R.id.tv_my_single_message);
//            TextView time_my_single_message=v.findViewById(R.id.time_my_single_message);
//            TextView my_chat_User=v.findViewById(R.id.my_chat_User);
//
//            my_chat_User.setText(currentUserName+":");
//            tv_my_single_message.setText(message);
//            time_my_single_message.setText(time);  //didnt show dates as they looked odd
//
//
//
//            chat_messages_layout.addView(v);
//
//
//
//        }
//        else
//        {
//            //that means this is others message
//            View v=LayoutInflater.from(GroupChatActivity.this).inflate(R.layout.others_single_message,chat_messages_layout,false);
//            TextView tv_others_single_message=v.findViewById(R.id.tv_others_single_message);
//            TextView time_others_single_message=v.findViewById(R.id.time_others_single_message);
//            TextView others_chat_User=v.findViewById(R.id.others_chat_User);
//
//           others_chat_User.setText(currentUserName+":");
//            tv_others_single_message.setText( message);
//            time_others_single_message.setText(time);
//
//            chat_messages_layout.addView(v);
//        }
//
//        scrollDown();
//    }

    private void intialize() {
//        toolbar=findViewById(R.id.group_chat_bar);
//        group_scroll_view=findViewById(R.id.group_scroll_view);
        MessageInputText=findViewById(R.id.add_new_msg);
        SendMessageButton=findViewById(R.id.btn_send_msg);
        SendFilesButton=findViewById(R.id.files_send_btn);
        loadingBar=new ProgressDialog(this);

//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle(groupName);

        ChatToolBar =findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        groupNametv = (TextView) findViewById(R.id.custom_user_name);
        groupImage =  findViewById(R.id.custom_profile_image);

        messageAdapter = new MessageAdapter(messagesList,GroupChatActivity.this);
        GroupListRecyler=findViewById(R.id.group_messages_list);
        LinearLayoutManager  linearLayoutManager = new LinearLayoutManager(this);
        GroupListRecyler.setLayoutManager(linearLayoutManager);
        GroupListRecyler.setAdapter(messageAdapter);
//        chat_messages_layout=findViewById(R.id.chat_messages_layout);2

    }

//    private void sendMessage() {
//        String msg=add_new_msg.getText().toString().trim();
//        String messageKey=groupIDRef.push().getKey();
//        if(!msg.isEmpty())
//        {
//            Calendar calendarDate=Calendar.getInstance();
//            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd MMM YYYY");
//            currentDate=simpleDateFormat.format(calendarDate.getTime());
//
//            Calendar calendarTime=Calendar.getInstance();
//            SimpleDateFormat simpleTimeFormat=new SimpleDateFormat("hh:mm a");
//            currentTime=simpleTimeFormat.format(calendarTime.getTime());
//
//            HashMap<String,Object> groupMessageKey=new HashMap<>();
//            groupIDRef.updateChildren(groupMessageKey);
//
//            groupMessageKeyRef=groupIDRef.child(messageKey);
//            HashMap<String,Object> messageInfoMap=new HashMap<>();
//
//                messageInfoMap.put("uid",currentUserID.toString());
//                messageInfoMap.put("message",msg);
//                messageInfoMap.put("date",currentDate);
//                messageInfoMap.put("time",currentTime);
//
//            groupMessageKeyRef.updateChildren(messageInfoMap);
//
//            add_new_msg.setText("");
//            scrollDown();
//
//
//        }
//    }
    private void SendMessage()
    {
        final String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "First write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calendarDate=Calendar.getInstance();
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd MMM YYYY");
            currentDate=simpleDateFormat.format(calendarDate.getTime());

            Calendar calendarTime=Calendar.getInstance();
            SimpleDateFormat simpleTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=simpleTimeFormat.format(calendarTime.getTime());

            String messageKey=groupIDRef.push().getKey();
            groupMessageKeyRef=groupIDRef.child(messageKey);

//            Toast.makeText(this,"MSG kEy : "+  groupMessageKeyRef.toString(),Toast.LENGTH_LONG).show();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", currentUserID);
            messageTextBody.put("senderName",currentUserName);
            messageTextBody.put("to", "");
            messageTextBody.put("messageID", "");
            messageTextBody.put("time", currentDate);
            messageTextBody.put("date", currentDate);


            groupMessageKeyRef.updateChildren(messageTextBody).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    MessageInputText.setText("");
                }
            });

//            scrollDown();

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait..we are sending the file");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri=data.getData();
//                String uriString=getRealPathFromUri(fileUri);
//                say("uriString: "+uriString);
//                File file=new File(uriString);
//                String s=file.getName();

            String s=fileUri.getLastPathSegment();
            int index=s.lastIndexOf("/");
            if(index==-1)
                fileName=s;
            else fileName=s.substring(index);

            Toast.makeText(this,"FILENAME 1: "+ fileName,Toast.LENGTH_LONG).show();

//                if(uriString.startsWith("/content:/"))
//                {
//                    Cursor cursor=null;
//
//                    cursor=this.getContentResolver().query(fileUri,null,null,null,null);
//
//                    try {
//                        cursor = this.getContentResolver().query(fileUri, null, null, null, null);
//                        if (cursor != null && cursor.moveToFirst()) {
//                            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                        }
//                    } finally {
//                        cursor.close();
//                    }
//                } else if (uriString.startsWith("file://")) {
//                    fileName = file.getName();
//                }

            Toast.makeText(this,"FILENAME: "+fileName,Toast.LENGTH_LONG).show();

            if(!checker.equals("image"))
            {

                StorageReference reference= FirebaseStorage.getInstance().getReference().child("Document Files");





                final String messageKey=groupIDRef.push().getKey();
                 groupMessageKeyRef=groupIDRef.child(messageKey);



                final StorageReference filPath=reference.child(messageKey  +"." + checker);

                filPath.putFile(fileUri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                double p=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                loadingBar.setMessage( (int)p +"% Uploaded.");
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                filPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Uri download=uri;
                                        Log.d("ChatActivity",download.toString());
                                         myUrL=download.toString();
                                        Map messageTextBody = new HashMap();
                                        messageTextBody.put("message",myUrL);
                                        messageTextBody.put("name",fileName) ;
                                        messageTextBody.put("senderName",currentUserName);
                                        messageTextBody.put("type", checker);
                                        messageTextBody.put("from", currentUserID);
                                        messageTextBody.put("to", "");
                                        messageTextBody.put("messageID", messageKey);
                                        messageTextBody.put("time", currentTime);
                                        messageTextBody.put("date", currentDate);
//                                             messageTextBody.put("fileName",fileName);



                                        groupMessageKeyRef.updateChildren(messageTextBody);
                                        MessageInputText.setText("");
                                        loadingBar.dismiss();
                                    }
                                });




                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupChatActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                            }
                        })
                ;

            }
            else if(checker.equals("image"))
            {
                 StorageReference reference= FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageKey=groupIDRef.push().getKey();
                groupMessageKeyRef=groupIDRef.child(messageKey);

                final StorageReference filPath=reference.child(messageKey  +".jpg");


                uploadTask=filPath.putFile(fileUri);


                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filPath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                             Uri download= task.getResult();
                            myUrL=download.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrL);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("senderName",currentUserName);
                            messageTextBody.put("from", currentUserID);
                            messageTextBody.put("to", "");
                            messageTextBody.put("messageID", messageKey);
                            messageTextBody.put("time", currentTime);
                            messageTextBody.put("date", currentDate);


                            groupMessageKeyRef.updateChildren(messageTextBody).addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    loadingBar.dismiss();

                                    MessageInputText.setText("");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(GroupChatActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                                }
                            });






                        }
                    }
                });
            }
            else
            {
                Toast.makeText(GroupChatActivity.this,"Nothing selected",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
