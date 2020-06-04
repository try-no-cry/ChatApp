package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircularImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String checker="",myUrL;
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;
    private String saveCurrentTime, saveCurrentDate;
    private String fileName = "";
    private String currentUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();


        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage= getIntent().getExtras().get("visit_image").toString();

        RootRef.child("Users").child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.hasChild("senderName"))
                        currentUserName=dataSnapshot.child("senderName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        IntializeControllers();


        userName.setText(messageReceiverName);
        if(messageReceiverImage!=null && !messageReceiverImage.equals(""))
            Glide.with(ChatActivity.this).load(messageReceiverImage).into(userImage);


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendMessage();
            }
        });

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]{
                        "Images",
                        "PDF File"
                        ,"Docs file"
                };

                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);

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


        DisplayLastSeen();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
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
    }




    private void IntializeControllers()
    {
        ChatToolBar =findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = (TextView) findViewById(R.id.custom_user_name);
         userImage =  findViewById(R.id.custom_profile_image);

        SendMessageButton =  findViewById(R.id.send_message_btn);
        SendFilesButton =  findViewById(R.id.send_files_btn);
        MessageInputText =findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList,ChatActivity.this);
        userMessagesList =  findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        loadingBar=new ProgressDialog(this);
    }



    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                            else
                            {
                                userLastSeen.setText("offline");
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }






    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "First write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("name","") ;
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("senderName",currentUserName);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Messages Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
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

                     say("Place 3 pdf/docx");
                     StorageReference reference= FirebaseStorage.getInstance().getReference().child("Document Files");
                     final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                     final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                     DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                             .child(messageSenderID).child(messageReceiverID).push();

                     final String messagePushID = userMessageKeyRef.getKey();

                     say("MessagePushID"+messagePushID);

                     final StorageReference filPath=reference.child(messagePushID  +"." + checker);

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
                                             say("Uri "+download.toString());
                                             myUrL=download.toString();
                                             Map messageTextBody = new HashMap();
                                             messageTextBody.put("message",myUrL);
                                             messageTextBody.put("name",fileName) ;
                                             messageTextBody.put("type", checker);
                                             messageTextBody.put("from", messageSenderID);
                                             messageTextBody.put("senderName",currentUserName);
                                             messageTextBody.put("to", messageReceiverID);
                                             messageTextBody.put("messageID", messagePushID);
                                             messageTextBody.put("time", saveCurrentTime);
                                             messageTextBody.put("date", saveCurrentDate);
//                                             messageTextBody.put("fileName",fileName);

                                             say(messageTextBody.toString());

                                             Map messageBodyDetails = new HashMap();
                                             messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                             messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                             RootRef.updateChildren(messageBodyDetails);
                                             loadingBar.dismiss();
                                         }
                                     });




                                 }
                             })
                    .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             loadingBar.dismiss();
                             say(e.getMessage().toString());
                         }
                     })
                     ;

                 }
                 else if(checker.equals("image"))
                 {
                     say("Place 1");
                     StorageReference reference= FirebaseStorage.getInstance().getReference().child("Image Files");
                     final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                     final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                     DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                             .child(messageSenderID).child(messageReceiverID).push();

                     final String messagePushID = userMessageKeyRef.getKey();

                     say("MessagePushID"+messagePushID);

                     final StorageReference filPath=reference.child(messagePushID  +".jpg");

                     say("FilePath: "+filPath);

                     uploadTask=filPath.putFile(fileUri);

                     say("UploadTask "+ uploadTask);

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
                                 say("Place2");
                                 Uri download= task.getResult();
                                 myUrL=download.toString();

                                 Map messageTextBody = new HashMap();
                                 messageTextBody.put("message", myUrL);
                                 messageTextBody.put("name", fileUri.getLastPathSegment());
                                 messageTextBody.put("type", checker);
                                 messageTextBody.put("from", messageSenderID);
                                 messageTextBody.put("senderName",currentUserName);
                                 messageTextBody.put("to", messageReceiverID);
                                 messageTextBody.put("messageID", messagePushID);
                                 messageTextBody.put("time", saveCurrentTime);
                                 messageTextBody.put("date", saveCurrentDate);

                                 say(messageTextBody.toString());

                                 Map messageBodyDetails = new HashMap();
                                 messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                 messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                 RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                     @Override
                                     public void onComplete(@NonNull Task task)
                                     {
                                         if (task.isSuccessful())
                                         {

                                             Toast.makeText(ChatActivity.this, "Messages Sent Successfully...", Toast.LENGTH_SHORT).show();
                                         }
                                         else
                                         {
                                             Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                         }
                                         loadingBar.dismiss();
                                         MessageInputText.setText("");
                                     }
                                 });

                             }
                         }
                     });
                 }
                 else
                 {
                     say("Nothing selected");
                 }
        }
    }

    public void say(String s)
    {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }


}
