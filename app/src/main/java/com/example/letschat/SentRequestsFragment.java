package com.example.letschat;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class SentRequestsFragment extends Fragment
{
    private View view;
    private RecyclerView recyclerView;
    private DatabaseReference chatRequestsRef, userRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;

    public SentRequestsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sent_requests, container, false);
        recyclerView = view.findViewById(R.id.sent_chat_request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<People> options = new FirebaseRecyclerOptions.Builder<People>()
                .setQuery(chatRequestsRef.child(currentUserID).child("sent"), People.class)
                .build();
        FirebaseRecyclerAdapter<People, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<People, RequestsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder requestsViewHolder, int i, @NonNull People people)
                    {
                        requestsViewHolder.accept.setVisibility(View.INVISIBLE);
                        final String userID = getRef(i).getKey();
                        final DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        requestRef.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                requestRef.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image"))
                                        {
                                            final String name = dataSnapshot.child("name").getValue().toString();
                                            final String about = dataSnapshot.child("about").getValue().toString();
                                            final String image = dataSnapshot.child("image").getValue().toString();
                                            requestsViewHolder.personName.setText(name);
                                            requestsViewHolder.personAbout.setText(about);
                                            Picasso.get().load(image).into(requestsViewHolder.personImage);
                                        }
                                        else
                                        {
                                            final String name = dataSnapshot.child("name").getValue().toString();
                                            final String about = dataSnapshot.child("about").getValue().toString();
                                            requestsViewHolder.personName.setText(name);
                                            requestsViewHolder.personAbout.setText(about);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        requestsViewHolder.personName.setText(people.getName());
                        requestsViewHolder.personAbout.setText(people.getAbout());
                        Picasso.get().load(people.getImage()).into(requestsViewHolder.personImage);
                        requestsViewHolder.delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chatRequestsRef.child(currentUserID).child("sent").child(userID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                chatRequestsRef.child(userID).child("received").child(currentUserID).removeValue();
                                            }
                                        });
                            }
                        });
                    }
                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_layout, parent, false);
                        RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView personName, personAbout;
        CircleImageView personImage;
        Button accept, delete;
        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            personName = itemView.findViewById(R.id.person_name);
            personAbout = itemView.findViewById(R.id.person_about);
            personImage = itemView.findViewById(R.id.person_profile_image);
            accept = itemView.findViewById(R.id.accept_request);
            delete = itemView.findViewById(R.id.delete_request);
        }
    }
}
