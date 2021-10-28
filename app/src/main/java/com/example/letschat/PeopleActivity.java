package com.example.letschat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PeopleActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference usersRef;
    private EditText search;
    private final List<People> peopleList = new ArrayList<>();
    private PeopleAdapter peopleAdapter;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        firebaseAuth = FirebaseAuth.getInstance();

        search = findViewById(R.id.search_people);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = findViewById(R.id.people_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(PeopleActivity.this));

        toolbar = findViewById(R.id.people_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("People");

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPeople(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(search.getText().toString().equals(""))
                {
                    peopleList.clear();
                    for(DataSnapshot postSnapShot : dataSnapshot.getChildren())
                    {
                        People people = postSnapShot.getValue(People.class);
                        peopleList.add(people);
                    }
                    //Toast.makeText(PeopleActivity.this, ""+peopleList.size(), Toast.LENGTH_SHORT).show();
                    peopleAdapter = new PeopleAdapter(PeopleActivity.this, peopleList);
                    recyclerView.setAdapter(peopleAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }

    private void searchPeople(String s) {

        currentUserID = firebaseAuth.getCurrentUser().getUid();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                peopleList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    People people = snapshot.getValue(People.class);
                    if(!people.getUid().equals(currentUserID))
                    {
                        peopleList.add(people);
                    }
                }
                peopleAdapter = new PeopleAdapter(PeopleActivity.this, peopleList);
                recyclerView.setAdapter(peopleAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
            usersRef.child(currentUserID).child("userState")
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

    /*@Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<People> options = new FirebaseRecyclerOptions.Builder<People>()
                .setQuery(usersRef, People.class)
                .build();
        FirebaseRecyclerAdapter<People, PeopleViewHolder> adapter = new FirebaseRecyclerAdapter<People, PeopleViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull PeopleViewHolder holder, final int position, @NonNull People model)
            {
                holder.name.setText(model.getName());
                holder.about.setText(model.getAbout());
                Picasso.get().load(model.getImage()).into(holder.image);
                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String view_person = getRef(position).getKey();
                        Intent intent = new Intent(PeopleActivity.this, ProfileActivity.class);
                        intent.putExtra("view_person", view_person);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public PeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trial, parent, false);
                PeopleViewHolder viewHolder = new PeopleViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class PeopleViewHolder extends RecyclerView.ViewHolder
    {
        TextView name, about;
        CircleImageView image;
        public PeopleViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            about = itemView.findViewById(R.id.user_about);
            image = itemView.findViewById(R.id.user_image);
        }
    }*/
}
