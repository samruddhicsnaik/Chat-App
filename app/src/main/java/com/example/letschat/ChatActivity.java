package com.example.letschat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
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
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements TimePickerFragment.onTimeSetTP
{
    private String receiverID, currentUserID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private DatabaseReference userRef;
    private Toolbar toolbar;
    private View actionBarView;
    private LayoutInflater layoutInflater;
    private ImageButton sendMessage;
    private EditText typeMessage;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef, notificationRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private final List<Chats> chatList = new ArrayList<>();
    private final List<Chats> chatsList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private ImageButton sendFile;
    private String saveCurrentDate, saveCurrentTime;
    private String checker, url;
    private StorageTask uploadTask;
    private Uri fileUri;
    private StorageReference filePath;
    private ProgressDialog loadingBar;
    private ValueEventListener seenListener;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        receiverID = getIntent().getExtras().get("chat_person_id").toString();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        initializeFields();

        userRef.child(receiverID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if((dataSnapshot.exists() && dataSnapshot.hasChild("image")))
                {
                    final String name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    userName.setText(name);
                    Picasso.get().load(image).into(userImage);
                }
                else
                {
                    final String name = dataSnapshot.child("name").getValue().toString();
                    userName.setText(name);
                    Picasso.get().load(R.drawable.request_icon).into(userImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CharSequence options[] = new CharSequence[]
                        {
                                "Share Image",
                                "Share Pdf",
                                "Share Word Document",
                                "Extract Message from Image",
                                "Schedule Message",
                                "Translate Message"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select");
                builder.setItems(options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int i)
                    {
                        if(i==0)
                        {
                            checker="image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);

                        }
                        else if(i==1)
                        {
                            checker="pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF file"), 438);
                        }
                        else if(i==2)
                        {
                            checker="doc";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select word document"), 438);
                        }
                        else if(i==3)
                        {
                            dispatchTakePictureIntent();
                        }
                        else if(i==4)
                        {
                            String message = typeMessage.getText().toString();
                            if(message.isEmpty())
                            {
                                Toast.makeText(ChatActivity.this, "Type a message", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                DialogFragment newFragment = new TimePickerFragment();
                                newFragment.show(getSupportFragmentManager(), "timePicker");
                            }
                        }
                        else if(i==5)
                        {
                            final String message = typeMessage.getText().toString();
                            if(message.isEmpty())
                            {
                                Toast.makeText(ChatActivity.this, "Type a message", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                CharSequence lang[] = new CharSequence[]
                                        {
                                                "Hindi",
                                                "Marathi",
                                                "Gujarati",
                                                "French",
                                                "German",
                                                "Japanese"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                                builder.setTitle("Select Language");
                                builder.setItems(lang, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i)
                                    {
                                        if(i==0)
                                        {
                                            checker="Hindi";
                                            String code = "HI";

                                            FirebaseTranslatorOptions options =
                                                    new FirebaseTranslatorOptions.Builder()
                                                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                                                            .setTargetLanguage(FirebaseTranslateLanguage.HI)
                                                            .build();

                                            translateMessage(message, options);
                                        }
                                        else if(i==1)
                                        {
                                            checker="Marathi";
                                            String code = "MR";
                                            FirebaseTranslatorOptions options =
                                                    new FirebaseTranslatorOptions.Builder()
                                                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                                                            .setTargetLanguage(FirebaseTranslateLanguage.MR)
                                                            .build();

                                            translateMessage(message, options);
                                        }
                                        else if(i==2)
                                        {
                                            checker="Gujarati";
                                            String code = "GU";
                                            FirebaseTranslatorOptions options =
                                                    new FirebaseTranslatorOptions.Builder()
                                                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                                                            .setTargetLanguage(FirebaseTranslateLanguage.GU)
                                                            .build();

                                            translateMessage(message, options);
                                        }
                                        else if(i==3)
                                        {
                                            checker="French";
                                            String code = "FR";
                                            FirebaseTranslatorOptions options =
                                                    new FirebaseTranslatorOptions.Builder()
                                                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                                                            .setTargetLanguage(FirebaseTranslateLanguage.FR)
                                                            .build();

                                            translateMessage(message, options);
                                        }
                                        else if(i==4)
                                        {
                                            checker="German";
                                            String code = "DE";
                                            FirebaseTranslatorOptions options =
                                                    new FirebaseTranslatorOptions.Builder()
                                                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                                                            .setTargetLanguage(FirebaseTranslateLanguage.DE)
                                                            .build();

                                            translateMessage(message, options);
                                        }
                                        else if(i==5)
                                        {
                                            checker="Japanese";
                                            String code = "JA";
                                            FirebaseTranslatorOptions options =
                                                    new FirebaseTranslatorOptions.Builder()
                                                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                                                            .setTargetLanguage(FirebaseTranslateLanguage.JA)
                                                            .build();

                                            translateMessage(message, options);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        }
                    }
                });
                builder.show();
            }
        });

        rootRef.child("Messages").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*messagesList.clear();*/
                Messages messages = dataSnapshot.getValue(Messages.class);

                String status = "not deleted";

                if (messages.getTo().equals(currentUserID) && messages.getFrom().equals(receiverID)) {
                    Toast.makeText(ChatActivity.this, "" + messages.getReceiverStatus(), Toast.LENGTH_SHORT).show();
                    if (status.equals(messages.getReceiverStatus())) {
                        Toast.makeText(ChatActivity.this, "good", Toast.LENGTH_SHORT).show();
                        messagesList.add(messages);
                        //messageAdapter.notifyDataSetChanged();
                        //recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }

                }

                if (messages.getTo().equals(receiverID) && messages.getFrom().equals(currentUserID)) {
                    if (status.equals(messages.getSenderStatus())) {
                        Toast.makeText(ChatActivity.this, "bad", Toast.LENGTH_SHORT).show();
                        messagesList.add(messages);
                        //messageAdapter.notifyDataSetChanged();
                        //recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }

                }

                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

                /*if(messages.getTo().equals(currentUserID) && messages.getFrom().equals(receiverID) || messages.getTo().equals(receiverID) && messages.getFrom().equals(currentUserID))
                {
                    messagesList.add(messages);
                    messageAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                }*/
                    /*messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());*/

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);

                if(messages.getTo().equals(currentUserID) && messages.getFrom().equals(receiverID) || messages.getTo().equals(receiverID) && messages.getFrom().equals(currentUserID))
                {
                    for (int i = 0; i < messagesList.size(); i++) {
                        if (messagesList.get(i).getMessageID().equals(messages.getMessageID())) {
                            messagesList.remove(i);
                            messagesList.add(i, messages);
                            break;
                        }
                    }

                    for(int i = 0; i < messagesList.size(); i++) {
                        if (messagesList.get(i).getMessageID().equals(messages.getMessageID()) && messagesList.get(i).getTo().equals(currentUserID) && messagesList.get(i).getFrom().equals(receiverID)) {
                            Toast.makeText(ChatActivity.this, "1st condition", Toast.LENGTH_SHORT).show();
                            if (!"not deleted".equals(messages.getReceiverStatus())) {
                                //Toast.makeText(ChatActivity.this, "1st condition", Toast.LENGTH_SHORT).show();
                                messagesList.remove(i);
                                break;
                                //messageAdapter.notifyDataSetChanged();
                                //recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                            }
                        }
                    }

                    for(int i = 0; i < messagesList.size(); i++) {
                        if(messagesList.get(i).getMessageID().equals(messages.getMessageID()) && messagesList.get(i).getTo().equals(receiverID) && messagesList.get(i).getFrom().equals(currentUserID))
                        {
                            Toast.makeText(ChatActivity.this, "2nd condition", Toast.LENGTH_SHORT).show();
                            if(!"not deleted".equals(messages.getSenderStatus()))
                            {
                                //Toast.makeText(ChatActivity.this, "2nd condition", Toast.LENGTH_SHORT).show();
                                messagesList.remove(i);
                                break;
                                //messageAdapter.notifyDataSetChanged();
                                //recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                            }
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                if(messages.getTo().equals(currentUserID) && messages.getFrom().equals(receiverID) || messages.getTo().equals(receiverID) && messages.getFrom().equals(currentUserID))
                {
                    for (int i = 0; i < messagesList.size(); i++)
                    {
                        if(messagesList.get(i).getMessageID().equals(messages.getMessageID()))
                        {
                            messagesList.remove(i);
                            //messagesList.add(i, messages);
                            break;
                        }
                    }

                }

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /*rootRef.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Messages messages = snapshot.getValue(Messages.class);
                    if(!"not deleted".equals(messages.getSenderStatus()) && !"not deleted".equals(messages.getReceiverStatus()))
                    {
                        snapshot.getRef().removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/


        /*rootRef.child("Messages").child(currentUserID).child(receiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/
        displayLastSeen();

        //seenMessage(receiverID);
    }

    private void seenMessage(final String userID)
    {
        ref = FirebaseDatabase.getInstance().getReference("Messages");
        seenListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Messages messages = snapshot.getValue(Messages.class);
                    if(messages.getTo().equals(currentUserID) && messages.getFrom().equals(userID))
                    {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void translateMessage(String message, FirebaseTranslatorOptions options)
    {
        final FirebaseTranslator englishGermanTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
        englishGermanTranslator.translate(message)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                typeMessage.setText(translatedText);
                                Toast.makeText(ChatActivity.this, translatedText, Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof TimePickerFragment) {
            TimePickerFragment tp = (TimePickerFragment) fragment;
            tp.setOnTimeSetTPListner(this);
        }
    }

    @Override
    public void onTimeSetTP(long min, String minutes, String time) {
        if(min <0)
        {

        }
        else
        {
            ScheduleMsg(min, minutes, time);
        }
    }

    private void ScheduleMsg(long min, String minutes, String time){
        String message = typeMessage.getText().toString();
        if(message.isEmpty())
        {
            Toast.makeText(ChatActivity.this, "Type a message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calendar.getTime());

            //String messageSenderRef = "Messages/" + currentUserID + "/" + receiverID;
            //String messageReceiverRef = "Messages/" + receiverID + "/" + currentUserID;
            DatabaseReference messageKeyRef = rootRef.child("Messages").push();
            String messagePushID = messageKeyRef.getKey();

            Map messageBody = new HashMap();
            messageBody.put("message", message);
            messageBody.put("type", "text");
            messageBody.put("from", currentUserID);
            messageBody.put("to", receiverID);
            messageBody.put("messageID", messagePushID);
            messageBody.put("time", time);
            messageBody.put("date", minutes);
            messageBody.put("senderStatus", "not deleted");
            messageBody.put("receiverStatus", "not deleted");
            messageBody.put("seen", false);


            Map messageDetails = new HashMap();
            messageDetails.put("Messages" + "/" + messagePushID, messageBody);
            //messageDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);
            Data.Builder data = new  Data.Builder();
            Gson gson= new Gson();
            data.putString("msg",gson.toJson(messageDetails));  //("msg",messageDetails);
            OneTimeWorkRequest schedule = new OneTimeWorkRequest.Builder(MessageSchedular.class)
                    .setInputData(data.build())
                    .setInitialDelay(min, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(schedule);

            /*String messageSenderRef = "Messages/" + currentUserID + "/" + receiverID;
            String messageReceiverRef = "Messages/" + receiverID + "/" + currentUserID;
            DatabaseReference messageKeyRef = rootRef.child("Messages").child(currentUserID).child(receiverID).push();
            String messagePushID = messageKeyRef.getKey();
            Map messageBody = new HashMap();
            messageBody.put("message", message);
            messageBody.put("type", "text");
            messageBody.put("from", currentUserID);
            messageBody.put("to", receiverID);
            messageBody.put("messageID", messagePushID);
            messageBody.put("time", saveCurrentTime);
            messageBody.put("date", saveCurrentDate);
            Map messageDetails = new HashMap();
            messageDetails.put(messageSenderRef + "/" + messagePushID, messageBody);
            messageDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);
            Data.Builder data= new  Data.Builder();
            Gson gson= new Gson();
            data.putString("msg",gson.toJson(messageDetails));  //("msg",messageDetails);
            OneTimeWorkRequest schedule = new OneTimeWorkRequest.Builder(MessageSchedular.class)
                    .setInputData(data.build())
                    .setInitialDelay(minutes, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(schedule);*/
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 333;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void sendMessage()
    {
        final String message = typeMessage.getText().toString();

        final String secretKey = "AXXXXXXXXXXXXZ";
        String encryptedString = AES.encrypt(message, secretKey) ;
        if(message.isEmpty())
        {
            Toast.makeText(ChatActivity.this, "Type a message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calendar.getTime());

            long date = calendar.getTimeInMillis();

            DatabaseReference messageKeyRef = rootRef.child("Messages").push();
            String messagePushID = messageKeyRef.getKey();

            Map messageBody = new HashMap();
            messageBody.put("message", message);
            messageBody.put("type", "text");
            messageBody.put("from", currentUserID);
            messageBody.put("to", receiverID);
            messageBody.put("messageID", messagePushID);
            messageBody.put("time", saveCurrentTime);
            messageBody.put("date", Long.toString(date));
            messageBody.put("senderStatus", "not deleted");
            messageBody.put("receiverStatus", "not deleted");
            messageBody.put("seen", false);

            messageKeyRef.setValue(messageBody).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        HashMap<String, String> chatNotification = new HashMap<>();
                        chatNotification.put("from", currentUserID);
                        chatNotification.put("message", message);
                        chatNotification.put("time", saveCurrentTime);

                        rootRef.child("MessageNotifications").child(receiverID).push().setValue(chatNotification);
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            /*userRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("chats").exists())
                    {
                        readChats();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });*/

            userRef.child(receiverID).child("chats").addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        String chatID = snapshot.getKey();
                        String userID = dataSnapshot.child(chatID).child("user").getValue().toString();
                        if(userID.equals(currentUserID))
                        {
                            //Toast.makeText(ChatActivity.this, "yes equal", Toast.LENGTH_SHORT).show();
                            rootRef.child("Users").child(receiverID).child("chats").child(chatID).removeValue();
                        }
                        else
                        {
                            //Toast.makeText(ChatActivity.this, "not equal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final String chatID = rootRef.child("Users").child(currentUserID).child("chats").push().getKey();
            rootRef.child("Users").child(receiverID).child("chats").push();

            HashMap<String, Object> chats = new HashMap<>();
            HashMap<String, Object> users = new HashMap<>();
            chats.put("chat id", chatID);
            chats.put("sender id", currentUserID);
            chats.put("receiver id", receiverID);
            chats.put("user", currentUserID);
            users.put(chatID + "/", chats);

            rootRef.child("Users").child(receiverID).child("chats").updateChildren(users);

            userRef.child(currentUserID).child("chats").addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        String chatID = snapshot.getKey();
                        String userID = dataSnapshot.child(chatID).child("user").getValue().toString();
                        if(userID.equals(receiverID))
                        {
                            //Toast.makeText(ChatActivity.this, "yes equal", Toast.LENGTH_SHORT).show();
                            rootRef.child("Users").child(currentUserID).child("chats").child(chatID).removeValue();
                        }
                        else
                        {
                            //Toast.makeText(ChatActivity.this, "not equal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            HashMap<String, Object> chat = new HashMap<>();
            HashMap<String, Object> user = new HashMap<>();
            chat.put("chat id", chatID);
            chat.put("sender id", currentUserID);
            chat.put("receiver id", receiverID);
            chat.put("user", receiverID);
            user.put(chatID + "/", chat);
            rootRef.child("Users").child(currentUserID).child("chats").updateChildren(user);



            /*String messageSenderRef = "Messages/" + currentUserID + "/" + receiverID;
            String messageReceiverRef = "Messages/" + receiverID + "/" + currentUserID;

            DatabaseReference messageKeyRef = rootRef.child("Messages").child(currentUserID).child(receiverID).push();
            String messagePushID = messageKeyRef.getKey();
            final String chatID = rootRef.child("Users").child(currentUserID).child("chats").push().getKey();
            rootRef.child("Users").child(receiverID).child("chats").push();
            rootRef.child("Users").child(currentUserID).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        String chatid = ds.getKey();
                        String userID = dataSnapshot.child(chatid).child("user").getValue().toString();
                        if(userID.equals(receiverID))
                        {
                            Toast.makeText(ChatActivity.this, "yes equal", Toast.LENGTH_SHORT).show();
                            rootRef.child("Users").child(currentUserID).child("chats").child(chatid).removeValue();
                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this, "not equal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });

            HashMap<String, Object> chat = new HashMap<>();
            HashMap<String, Object> user = new HashMap<>();
            chat.put("chat id", chatID);
            chat.put("sender id", currentUserID);
            chat.put("receiver id", receiverID);
            chat.put("user", receiverID);
            user.put(chatID + "/", chat);

            HashMap<String, Object> chats = new HashMap<>();
            HashMap<String, Object> users = new HashMap<>();
            chats.put("chat id", chatID);
            chats.put("sender id", currentUserID);
            chats.put("receiver id", receiverID);
            chats.put("user", currentUserID);
            users.put(chatID + "/", chats);

            rootRef.child("Users").child(receiverID).child("chats").updateChildren(users);
            rootRef.child("Users").child(currentUserID).child("chats").updateChildren(user);

            Map messageBody = new HashMap();
            messageBody.put("message", message);
            messageBody.put("type", "text");
            messageBody.put("from", currentUserID);
            messageBody.put("to", receiverID);
            messageBody.put("messageID", messagePushID);
            messageBody.put("time", saveCurrentTime);
            messageBody.put("date", saveCurrentDate);
            messageBody.put("seen", false);
            Map messageDetails = new HashMap();
            messageDetails.put(messageSenderRef + "/" + messagePushID, messageBody);
            messageDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);

            rootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        HashMap<String, String> chatNotification = new HashMap<>();
                        chatNotification.put("from", currentUserID);
                        chatNotification.put("message", message);
                        chatNotification.put("time", saveCurrentTime);

                        rootRef.child("MessageNotifications").child(receiverID).push().setValue(chatNotification);
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    typeMessage.setText("");
                }
            });*/
        }
        typeMessage.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please Wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            fileUri = data.getData();

            if(!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                //final String messageSenderRef = "Messages/" + currentUserID + "/" + receiverID;
                //final String messageReceiverRef = "Messages/" + receiverID + "/" + currentUserID;
                final DatabaseReference messageKeyRef = rootRef.child("Messages").push();
                final String messagePushID = messageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                final String downloadUrl = uri.toString();

                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
                                saveCurrentDate = currentDate.format(calendar.getTime());
                                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                                saveCurrentTime = currentTime.format(calendar.getTime());

                                Map messageBody = new HashMap();
                                messageBody.put("message", downloadUrl);
                                messageBody.put("name", fileUri.getLastPathSegment());
                                messageBody.put("type", checker);
                                messageBody.put("from", currentUserID);
                                messageBody.put("to", receiverID);
                                messageBody.put("messageID", messagePushID);
                                messageBody.put("time", saveCurrentTime);
                                messageBody.put("date", saveCurrentDate);
                                messageBody.put("senderStatus", "not deleted");
                                messageBody.put("receiverStatus", "not deleted");
                                messageBody.put("seen", false);

                                //Map messageDetails = new HashMap();
                                //messageDetails.put(messageSenderRef + "/" + messagePushID, messageBody);
                                // messageDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);
                                //messageKeyRef.setValue(messageBody);
                                //rootRef.updateChildren(messageDetails);
                                loadingBar.dismiss();

                                messageKeyRef.setValue(messageBody).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            HashMap<String, String> chatNotification = new HashMap<>();
                                            chatNotification.put("from", currentUserID);
                                            chatNotification.put("message", "1 new document");
                                            chatNotification.put("time", saveCurrentTime);

                                            rootRef.child("MessageNotifications").child(receiverID).push().setValue(chatNotification);
                                        }
                                        else
                                        {
                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading....");
                    }
                });
                /*StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSenderRef = "Messages/" + currentUserID + "/" + receiverID;
                final String messageReceiverRef = "Messages/" + receiverID + "/" + currentUserID;
                DatabaseReference messageKeyRef = rootRef.child("Messages").child(currentUserID).child(receiverID).push();
                final String messagePushID = messageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                final String downloadUrl = uri.toString();
                                Map messageBody = new HashMap();
                                messageBody.put("message", downloadUrl);
                                messageBody.put("name", fileUri.getLastPathSegment());
                                messageBody.put("type", checker);
                                messageBody.put("from", currentUserID);
                                messageBody.put("to", receiverID);
                                messageBody.put("messageID", messagePushID);
                                messageBody.put("time", saveCurrentTime);
                                messageBody.put("date", saveCurrentDate);

                                Map messageDetails = new HashMap();
                                messageDetails.put(messageSenderRef + "/" + messagePushID, messageBody);
                                messageDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);

                                rootRef.updateChildren(messageDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading....");
                    }
                });*/
            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                //final String messageSenderRef = "Messages/" + currentUserID + "/" + receiverID;
                //final String messageReceiverRef = "Messages/" + receiverID + "/" + currentUserID;

                final DatabaseReference messageKeyRef = rootRef.child("Messages").push();
                final String messagePushID = messageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + ".jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation()
                {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            url = downloadUrl.toString();

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
                            saveCurrentDate = currentDate.format(calendar.getTime());
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                            saveCurrentTime = currentTime.format(calendar.getTime());


                            final Map messageBody = new HashMap();
                            messageBody.put("message", url);
                            messageBody.put("name", fileUri.getLastPathSegment());
                            messageBody.put("type", checker);
                            messageBody.put("from", currentUserID);
                            messageBody.put("to", receiverID);
                            messageBody.put("messageID", messagePushID);
                            messageBody.put("time", saveCurrentTime);
                            messageBody.put("date", saveCurrentDate);
                            messageBody.put("seen", false);
                            messageBody.put("senderStatus", "not deleted");
                            messageBody.put("receiverStatus", "not deleted");
                            //Map messageDetails = new HashMap();
                            //messageDetails.put(messageSenderRef + "/" + messagePushID, messageBody);
                            //messageDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);
                            messageKeyRef.setValue(messageBody).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        messageKeyRef.setValue(messageBody).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    HashMap<String, String> chatNotification = new HashMap<>();
                                                    chatNotification.put("from", currentUserID);
                                                    chatNotification.put("message", "1 new image");
                                                    chatNotification.put("time", saveCurrentTime);

                                                    rootRef.child("MessageNotifications").child(receiverID).push().setValue(chatNotification);
                                                }
                                                else
                                                {
                                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                    }
                                    typeMessage.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                loadingBar.dismiss();
            }



            userRef.child(receiverID).child("chats").addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        String chatID = snapshot.getKey();
                        String userID = dataSnapshot.child(chatID).child("user").getValue().toString();
                        if(userID.equals(currentUserID))
                        {
                            //Toast.makeText(ChatActivity.this, "yes equal", Toast.LENGTH_SHORT).show();
                            rootRef.child("Users").child(receiverID).child("chats").child(chatID).removeValue();
                        }
                        else
                        {
                            //Toast.makeText(ChatActivity.this, "not equal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final String chatID = rootRef.child("Users").child(currentUserID).child("chats").push().getKey();
            rootRef.child("Users").child(receiverID).child("chats").push();

            HashMap<String, Object> chats = new HashMap<>();
            HashMap<String, Object> users = new HashMap<>();
            chats.put("chat id", chatID);
            chats.put("sender id", currentUserID);
            chats.put("receiver id", receiverID);
            chats.put("user", currentUserID);
            users.put(chatID + "/", chats);

            rootRef.child("Users").child(receiverID).child("chats").updateChildren(users);

            userRef.child(currentUserID).child("chats").addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        String chatID = snapshot.getKey();
                        String userID = dataSnapshot.child(chatID).child("user").getValue().toString();
                        if(userID.equals(receiverID))
                        {
                            //Toast.makeText(ChatActivity.this, "yes equal", Toast.LENGTH_SHORT).show();
                            rootRef.child("Users").child(currentUserID).child("chats").child(chatID).removeValue();
                        }
                        else
                        {
                            //Toast.makeText(ChatActivity.this, "not equal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            HashMap<String, Object> chat = new HashMap<>();
            HashMap<String, Object> user = new HashMap<>();
            chat.put("chat id", chatID);
            chat.put("sender id", currentUserID);
            chat.put("receiver id", receiverID);
            chat.put("user", receiverID);
            user.put(chatID + "/", chat);
            rootRef.child("Users").child(currentUserID).child("chats").updateChildren(user);
        }

        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            Task<FirebaseVisionText> result =
                    detector.processImage(firebaseVisionImage)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    List<FirebaseVisionText.TextBlock> blocks= firebaseVisionText.getTextBlocks();
                                    String text="";
                                    for (FirebaseVisionText.TextBlock block:blocks){
                                         text= text + block.getText();

                                    }
                                    typeMessage.setText(text);
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
        }
    }

    private void displayLastSeen()
    {
        rootRef.child("Users").child(receiverID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("userState").exists() && dataSnapshot.child("userState").hasChild("state"))
                {
                    final String lastSeen = dataSnapshot.child("userState").child("state").getValue().toString();
                    final String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    final String time = dataSnapshot.child("userState").child("time").getValue().toString();
                    if(lastSeen.equals("online"))
                    {
                        userLastSeen.setText("online");
                    }
                    else
                    {
                        userLastSeen.setText("last seen on " +date +" " +time);
                    }
                }
                else
                {
                    userLastSeen.setText("offline");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields()
    {
        toolbar = findViewById(R.id.chats_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.name);
        userLastSeen = findViewById(R.id.last_seen);
        userImage = findViewById(R.id.image);
        typeMessage = findViewById(R.id.type_message);
        sendMessage = findViewById(R.id.send_message);
        sendFile = findViewById(R.id.send_file);
        recyclerView = findViewById(R.id.chats_list);
        messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(messageAdapter);
        loadingBar = new ProgressDialog(ChatActivity.this);
        notificationRef = FirebaseDatabase.getInstance().getReference("MessageNotification");
    }

    private void readMessages()
    {
        rootRef.child("Messages").addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            /*messagesList.clear();*/
            Messages messages = dataSnapshot.getValue(Messages.class);
            //Toast.makeText(ChatActivity.this, "hello"+messages.getTo(), Toast.LENGTH_SHORT).show();

            if(messages.getTo().equals(currentUserID) && messages.getFrom().equals(receiverID) || messages.getTo().equals(receiverID) && messages.getFrom().equals(currentUserID))
            {
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }
                    /*messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());*/
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            messageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

        /*rootRef.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Messages messages = snapshot.getValue(Messages.class);
                    if (messages.getTo().equals(currentUserID) && messages.getFrom().equals(receiverID) || messages.getTo().equals(receiverID) && messages.getFrom().equals(currentUserID))
                    {
                        messagesList.add(messages);
                        messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);
                        recyclerView.setAdapter(messageAdapter);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }
                    *//*messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());*//*
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        ref.removeEventListener(seenListener);
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        updateUserStatus("offline");

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        seenMessage(receiverID);
        updateUserStatus("online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }



    /*private void readChats()
    {
        userRef.child(currentUserID).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String chatID = snapshot.getKey();
                    String userID = dataSnapshot.child(chatID).child("user").getValue().toString();
                    if(userID.equals(receiverID))
                    {
                        Toast.makeText(ChatActivity.this, "yes equal", Toast.LENGTH_SHORT).show();
                        rootRef.child("Users").child(currentUserID).child("chats").child(chatID).removeValue();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "not equal", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCurrentDate);
        onlineState.put("state", state);

        currentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineState);
    }

    public static class AES {

        private static SecretKeySpec secretKey;
        private static byte[] key;

        public static void setKey(String myKey)
        {
            MessageDigest sha = null;
            try {
                key = myKey.getBytes("UTF-8");
                sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                secretKey = new SecretKeySpec(key, "AES");
            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public static String encrypt(String strToEncrypt, String secret)
        {
            try
            {
                setKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
            }
            catch (Exception e)
            {
                System.out.println("Error while encrypting: " + e.toString());
            }
            return null;
        }

        public static String decrypt(String strToDecrypt, String secret)
        {
            try
            {
                setKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            }
            catch (Exception e)
            {
                System.out.println("Error while decrypting: " + e.toString());
            }
            return null;
        }
    }
}
