package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private String personID, senderID, currentUserID;
    private Toolbar toolbar;
    private TextView personName, personAbout;
    private Button sendRequest;
    private CircleImageView personImage;
    private DatabaseReference userRef, chatRequestRef, friendsRef, notificationRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        personName = findViewById(R.id.person_name);
        personAbout = findViewById(R.id.person_about);
        sendRequest = findViewById(R.id.send_request);
        personImage = findViewById(R.id.person_image);
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        toolbar = findViewById(R.id.person_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        personID = getIntent().getExtras().get("view_person").toString();
        senderID = firebaseAuth.getCurrentUser().getUid();
        personProfileDetails();
    }

    private void personProfileDetails()
    {
        userRef.child(personID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("image").exists())
                {
                    String image = dataSnapshot.child("image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String about = dataSnapshot.child("about").getValue().toString();
                    Picasso.get().load(image).into(personImage);
                    personName.setText(name);
                    personAbout.setText(about);
                    manageChatRequest();
                }
                else
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String about = dataSnapshot.child("about").getValue().toString();
                    personName.setText(name);
                    personAbout.setText(about);
                    manageChatRequest();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void manageChatRequest()
    {
        if(!senderID.equals(personID))
        {
            friendsRef.child(senderID).child("friends").child(personID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        sendRequest.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        chatRequestRef.child(senderID).child("received").child(personID)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists())
                                        {
                                            sendRequest.setText("Accept Request");
                                            sendRequest.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    acceptChatRequest();
                                                }
                                            });
                                        }
                                        else
                                        {
                                            chatRequestRef.child(senderID).child("sent").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.child(personID).exists())
                                                    {
                                                        sendRequest.setText("Cancel Request");
                                                        sendRequest.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                cancelChatRequest();
                                                            }
                                                        });
                                                    }
                                                    else
                                                    {
                                                        sendRequest.setOnClickListener(new View.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(View v)
                                                            {
                                                                sendChatRequest();
                                                            }
                                                        });
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else
        {
            sendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void acceptChatRequest()
    {
        chatRequestRef.child(personID).child("sent").child(senderID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        chatRequestRef.child(senderID).child("received").child(personID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                friendsRef.child(senderID).child("friends").child(personID).child("request type").setValue("friends")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                friendsRef.child(personID).child("friends").child(senderID).child("request type").setValue("friends");
                                            }
                                        });
                            }
                        });
                    }
                });
    }

    private void sendChatRequest()
    {
        chatRequestRef.child(senderID).child("sent").child(personID).child("request type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(personID).child("received").child(senderID).child("request type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                HashMap<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from", senderID);
                                                chatNotification.put("type", "request");

                                                notificationRef.child(personID).push()
                                                        .setValue(chatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    sendRequest.setEnabled(true);
                                                                    sendRequest.setText("cancel request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest()
    {
        chatRequestRef.child(senderID).child("sent").child(personID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    chatRequestRef.child(personID).child("received").child(senderID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            sendRequest.setEnabled(true);
                            sendRequest.setText("send request");
                        }
                    });
                }
            }
        });
    }

    private void updateUserStatus(String state)
    {
        if(firebaseAuth.getCurrentUser().isEmailVerified())
        {
            String saveCurrentTime, saveCurrentDate;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(calendar.getTime());

            HashMap<String, Object> onlineState = new HashMap<>();
            onlineState.put("time", saveCurrentTime);
            onlineState.put("date", saveCurrentDate);
            onlineState.put("state", state);

            currentUserID = firebaseAuth.getCurrentUser().getUid();
            userRef.child(currentUserID).child("userState")
                    .updateChildren(onlineState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }
}
