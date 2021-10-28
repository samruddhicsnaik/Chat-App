package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddGroupMembersActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private AddGroupMembersAdapter addGroupMembersAdapter;
    private final List<String> membersList = new ArrayList<>();
    private final List<String> groupMembersList = new ArrayList<>();
    private DatabaseReference groupRef;
    private String groupID;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);

        recyclerView = findViewById(R.id.add_member_list);

        groupRef = FirebaseDatabase.getInstance().getReference("Groups");

        groupID = getIntent().getExtras().get("groupID").toString();

        groupRef.child(groupID).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot memberSnapShot : dataSnapshot.getChildren())
                {
                    groupMembersList.add(memberSnapShot.getKey());
                }



                Toast.makeText(AddGroupMembersActivity.this, ""+membersList.size(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addGroupMembersAdapter = new AddGroupMembersAdapter(AddGroupMembersActivity.this, membersList);
        recyclerView.setAdapter(addGroupMembersAdapter);
    }
}
