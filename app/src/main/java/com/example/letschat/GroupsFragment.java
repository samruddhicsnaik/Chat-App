package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupsFragment extends Fragment
{
    private FloatingActionButton addGroup;
    private StorageReference ref;
    private FirebaseAuth firebaseAuth;
    private final List<Group> groupsList = new ArrayList<>();
    private GroupsAdapter groupsAdapter;
    private String currentUserID;
    private ImageView image;
    private DatabaseReference groupRef, userRef, rootRef;
    private RecyclerView recyclerView;

    public GroupsFragment() {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        groupRef = rootRef.child("Groups");
        recyclerView = view.findViewById(R.id.group_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rootRef.child("Users").child(currentUserID).child("groups").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                groupsList.clear();
                for(DataSnapshot postSnapShot : dataSnapshot.getChildren())
                {
                    Group group = postSnapShot.getValue(Group.class);
                    groupsList.add(group);
                }
                Collections.reverse(groupsList);
                groupsAdapter = new GroupsAdapter(getContext(), groupsList);
                recyclerView.setAdapter(groupsAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        addGroup = view.findViewById(R.id.add_group);
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SelectGroupMembersActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
