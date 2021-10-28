package com.example.letschat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference groupRef, userRef, rootRef;
    private String groupID;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        groupRef = FirebaseDatabase.getInstance().getReference("Groups");
        recyclerView = findViewById(R.id.group_members_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(GroupMembersActivity.this));
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        toolbar = findViewById(R.id.group_member_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Group Members");
        groupID = getIntent().getExtras().get("group_id").toString();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<People> options = new FirebaseRecyclerOptions.Builder<People>()
                .setQuery(groupRef.child(groupID).child("members"), People.class)
                .build();
        FirebaseRecyclerAdapter<People, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<People, FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, final int i, @NonNull People people)
            {
                final String userID = getRef(i).getKey();

                userRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        final String name, image, about;
                        if (dataSnapshot.hasChild("image")) {
                            name = dataSnapshot.child("name").getValue().toString();
                            about = dataSnapshot.child("about").getValue().toString();
                            image = dataSnapshot.child("image").getValue().toString();
                            friendsViewHolder.personName.setText(name);
                            friendsViewHolder.personAbout.setText(about);
                            Picasso.get().load(image).into(friendsViewHolder.personImage);
                        }
                        else
                        {
                            name = dataSnapshot.child("name").getValue().toString();
                            about = dataSnapshot.child("about").getValue().toString();
                            friendsViewHolder.personName.setText(name);
                            friendsViewHolder.personAbout.setText(about);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
                friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Message",
                                        "Remove"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupMembersActivity.this);
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0)
                                {
                                    Intent intent = new Intent(GroupMembersActivity.this, ChatActivity.class);
                                    intent.putExtra("chat_person_id", userID);
                                    startActivity(intent);
                                }
                                else if(i==1)
                                {
                                    groupRef.child(groupID).child("created by").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            final String admin = dataSnapshot.getValue().toString();
                                            if(currentUserID.equals(admin))
                                            {
                                                if(!userID.equals(admin))
                                                {
                                                    groupRef.child(groupID).child("members").child(userID).removeValue();
                                                }
                                                else
                                                {
                                                    Toast.makeText(GroupMembersActivity.this, "You are the admin", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else
                                            {
                                                Toast.makeText(GroupMembersActivity.this, "Ask the Admin to remove the member", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }
            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trial, parent, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView personName, personAbout;
        CircleImageView personImage;
        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            personName = itemView.findViewById(R.id.user_name);
            personAbout = itemView.findViewById(R.id.user_about);
            personImage = itemView.findViewById(R.id.user_image);
        }
    }
}
