package com.example.letschat;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment
{
    private RecyclerView recyclerView;
    private DatabaseReference friendsRef, userRef, rootRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private View view;
    public FriendsFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView = view.findViewById(R.id.friends_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        rootRef = FirebaseDatabase.getInstance().getReference();
        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateUserStatus("online");
        FirebaseRecyclerOptions<People> options = new FirebaseRecyclerOptions.Builder<People>()
                .setQuery(friendsRef.child(currentUserID).child("friends"), People.class)
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
                        if (dataSnapshot.hasChild("image"))
                        {
                            image = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(image).into(friendsViewHolder.personImage);
                        }
                        else
                        {
                            friendsViewHolder.personImage.setImageResource(R.drawable.user);
                        }
                        name = dataSnapshot.child("name").getValue().toString();
                        about = dataSnapshot.child("about").getValue().toString();
                        friendsViewHolder.personName.setText(name);
                        friendsViewHolder.personAbout.setText(about);
                        friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                "Message",
                                                "Block"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        if(i==0)
                                        {
                                            Intent intent = new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("chat_person_id", userID);
                                            startActivity(intent);
                                        }
                                        else if(i==1)
                                        {
                                            rootRef.child("Friends").child(currentUserID).child("friends").child(userID)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                                                            rootRef.child("Friends").child(currentUserID).child("friends").child(userID).removeValue();
                                                            rootRef.child("Friends").child(userID).child("friends").child(currentUserID).removeValue();
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
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void updateUserStatus(String state)
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
        rootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineState);
    }
}
