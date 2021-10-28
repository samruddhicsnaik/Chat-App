package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class ReceivedRequestsActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference chatRequestsRef, userRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_requests);

        recyclerView = findViewById(R.id.received_requests_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(ReceivedRequestsActivity.this));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = findViewById(R.id.received_requests_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Received Requests");

    }

    @Override
    protected void onStart()
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
