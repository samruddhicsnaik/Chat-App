package com.example.letschat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GroupActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private String groupID="";
    private TextView groupName, messages;
    private EditText typeMessage;
    private ImageButton sendFile, sendMessage;
    private DatabaseReference rootRef, userRef, groupRef, groupMessageKeyRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private String saveCurrentDate;
    private String saveCurrentTime;
    private RecyclerView recyclerView;
    private GroupMessageAdapter groupMessageAdapter;
    private final List<GroupMessages> messagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        groupID = getIntent().getExtras().get("group_id").toString();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users");
        groupRef = rootRef.child("Groups").child(groupID);
        initializeFields();
        rootRef.child("Groups").child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String name;
                if((dataSnapshot.exists() && dataSnapshot.hasChild("image")))
                {

                }

                name = dataSnapshot.child("name").getValue().toString();
                groupName.setText(name);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        toolbar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GroupActivity.this, GroupMembersActivity.class);
                intent.putExtra("group_id", groupID);
                startActivity(intent);
            }
        });
        getSenderInfo();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                typeMessage.setText("");
            }
        });
        groupRef.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                GroupMessages messages = dataSnapshot.getValue(GroupMessages.class);
                messagesList.add(messages);
                groupMessageAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

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
    }

    private void initializeFields()
    {
        toolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.group_bar, null);
        actionBar.setCustomView(actionBarView);
        groupName = findViewById(R.id.name);
        typeMessage = findViewById(R.id.type_message);
        sendMessage = findViewById(R.id.send_message);
        sendFile = findViewById(R.id.send_file);
        messages = findViewById(R.id.sender_message);
        recyclerView = findViewById(R.id.group_chat_list);
        groupMessageAdapter = new GroupMessageAdapter(messagesList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(groupMessageAdapter);
    }

    private void getSenderInfo()
    {
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    final String name = dataSnapshot.child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage()
    {
        String message = typeMessage.getText().toString();
        String messageKey = groupRef.child("messages").push().getKey();
        if(message.isEmpty())
        {
            Toast.makeText(GroupActivity.this, "type a message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calendar.getTime());

            long date = calendar.getTimeInMillis();

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupRef.child("messages").updateChildren(groupMessageKey);
            groupMessageKeyRef = groupRef.child("messages").child(messageKey);
            HashMap<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("user", currentUserID);
            messageInfo.put("message", message);
            messageInfo.put("date", Long.toString(date));
            messageInfo.put("time", saveCurrentTime);
            messageInfo.put("seen", false);
            messageInfo.put("type", "text");
            groupMessageKeyRef.updateChildren(messageInfo);
        }
    }
}
