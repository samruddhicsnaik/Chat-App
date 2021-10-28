package com.example.letschat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.StoriesViewHolder>
{
    private DatabaseReference userRef;
    private Context context;
    private List<Posts> postsList;

    public MemberAdapter(Context context, List<Posts> postsList)
    {
        this.context = context;
        this.postsList = postsList;
    }

    public class StoriesViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView personImage;
        public StoriesViewHolder(@NonNull View itemView)
        {
            super(itemView);
            personImage = itemView.findViewById(R.id.user_image);
        }
    }

    @NonNull
    @Override
    public StoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_display, parent, false);
        StoriesViewHolder viewHolder = new StoriesViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final StoriesViewHolder storiesViewHolder, final int i)
    {
        final Posts posts = postsList.get(i);
        final String userID = posts.getUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("image").exists())
                {
                    final String image = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(image).into(storiesViewHolder.personImage);
                }
                else
                {
                    Toast.makeText(context, "No Image", Toast.LENGTH_SHORT).show();
                    storiesViewHolder.personImage.setImageResource(R.drawable.user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        storiesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StoryActivity.class);
                intent.putExtra("post", posts.getPost());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }
}

