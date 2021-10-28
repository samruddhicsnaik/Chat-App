package com.example.letschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class SelectGroupMembersActivity extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private RecyclerView recyclerView;
    private DatabaseReference friendsRef, userRef, rootRef;
    private Toolbar toolbar;
    private FloatingActionButton createGroup;
    private MemberAdapter memberAdapter;
    List<String> peopleList = new ArrayList<>();
    private TextInputLayout groupName;
    private String saveCurrentDate, saveCurrentTime;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_members);
        recyclerView = findViewById(R.id.select_group_members);
        recyclerView.setLayoutManager(new LinearLayoutManager(SelectGroupMembersActivity.this));
        rootRef = FirebaseDatabase.getInstance().getReference();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        groupName = findViewById(R.id.group_name);
        createGroup = findViewById(R.id.create_group);
        loadingBar = new ProgressDialog(SelectGroupMembersActivity.this);
        toolbar = findViewById(R.id.select_group_members_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<People> options = new FirebaseRecyclerOptions.Builder<People>()
                .setQuery(friendsRef.child(currentUserID).child("friends"), People.class)
                .build();

        FirebaseRecyclerAdapter<People, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<People, FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, final int i, @NonNull final People people)
            {
                final String userID = getRef(i).getKey();
                userRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        friendsViewHolder.check.setVisibility(View.INVISIBLE);
                        final String name, image, about;
                        if (dataSnapshot.hasChild("image")) {
                            name = dataSnapshot.child("name").getValue().toString();
                            image = dataSnapshot.child("image").getValue().toString();
                            friendsViewHolder.personName.setText(name);
                            Picasso.get().load(image).into(friendsViewHolder.personImage);
                        }
                        else
                        {
                            name = dataSnapshot.child("name").getValue().toString();
                            friendsViewHolder.personName.setText(name);
                        }
                        friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                friendsViewHolder.check.setVisibility(View.VISIBLE);
                                peopleList.add(userID);
                                peopleList.add(currentUserID);
                            }
                        });
                        createGroup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                final String groupname = groupName.getEditText().getText().toString().trim();
                                if(groupname.isEmpty())
                                {
                                    Toast.makeText(SelectGroupMembersActivity.this, "Enter group name", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    loadingBar.setTitle("Sending File");
                                    loadingBar.setMessage("Please Wait");
                                    loadingBar.setCanceledOnTouchOutside(true);
                                    loadingBar.show();
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                                    saveCurrentDate = currentDate.format(calendar.getTime());
                                    SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                                    saveCurrentTime = currentTime.format(calendar.getTime());
                                    rootRef.child("Groups").child(groupname).push();
                                    final String groupID = rootRef.child("Groups").child(groupname).push().getKey();
                                    for(String s : peopleList)
                                    {
                                        HashMap<String, Object> userGroup = new HashMap();
                                        HashMap<String, Object> user = new HashMap<>();
                                        userGroup.put("gid", groupID);
                                        user.put(groupID + "/", userGroup);
                                        rootRef.child("Users").child(s).child("groups").updateChildren(user);
                                    }
                                    HashMap<String, Object> groupBody = new HashMap();
                                    groupBody.put("name", groupname);
                                    groupBody.put("created by", currentUserID);
                                    groupBody.put("date", saveCurrentDate);
                                    groupBody.put("time", saveCurrentTime);
                                    groupBody.put("gid", groupID);
                                    final String groupRef = "Groups/" + groupID;
                                    Map groupDetails = new HashMap();
                                    groupDetails.put(groupRef, groupBody);
                                    rootRef.updateChildren(groupDetails)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(SelectGroupMembersActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                                                        for(String s : peopleList)
                                                        {
                                                            HashMap<String, Object> memberDetails = new HashMap<>();
                                                            HashMap<String, Object> groupMembers = new HashMap<>();
                                                            memberDetails.put("uid", s);
                                                            groupMembers.put(s + "/", memberDetails);
                                                            rootRef.child("Groups").child(groupID).child("members").updateChildren(groupMembers);
                                                        }

                                                        Intent intent = new Intent(SelectGroupMembersActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_group_member, parent, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView personName;
        CircleImageView personImage;
        ImageButton check;
        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            personName = itemView.findViewById(R.id.user_name);
            personImage = itemView.findViewById(R.id.user_image);
            check = itemView.findViewById(R.id.check);
        }
    }
}
