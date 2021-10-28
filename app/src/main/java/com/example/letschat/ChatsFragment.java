package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ChatsFragment extends Fragment
{
    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, messageRef;
    private final List<Chats> chatsList = new ArrayList<>();
    private ChatsAdapter chatsAdapter;
    private View view;

    public ChatsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.chats_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();



        if(firebaseUser!=null)
        {
            currentUserID = firebaseAuth.getCurrentUser().getUid();
        }
        else
        {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        messageRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        chatsAdapter = new ChatsAdapter(getContext(), chatsList);
        recyclerView.setAdapter(chatsAdapter);
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("chats").exists())
                {
                    chatsList.clear();
                    for(DataSnapshot postSnapShot : dataSnapshot.child("chats").getChildren())
                    {
                        Chats chats = postSnapShot.getValue(Chats.class);
                        chatsList.add(chats);
                    }
                    Collections.reverse(chatsList);
                    chatsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //updateUserStatus("online");

        return view;
    }


    /*private void updateUserStatus(String state)
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }*/
}
