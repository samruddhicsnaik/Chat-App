package com.example.letschat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder>
{
    private DatabaseReference userRef;
    private Context context;
    private List<Posts> postsList;
    private DatabaseReference postsRef;
    int likeState = 0;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private DatabaseReference rootRef;

    public PostsAdapter(Context context, List<Posts> postsList)
    {
        this.context = context;
        this.postsList = postsList;
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView personImage;
        ImageView postImage;
        TextView personName;
        ImageButton like, comment, save;
        TextView likes;
        TextView textLikes;
        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            personImage = itemView.findViewById(R.id.user_image);
            postImage = itemView.findViewById(R.id.post);
            personName = itemView.findViewById(R.id.user_name);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            likes = itemView.findViewById(R.id.likes);
            textLikes = itemView.findViewById(R.id.text_likes);
        }
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_display, parent, false);
        PostsViewHolder viewHolder = new PostsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, final int i)
    {
        final Posts posts = postsList.get(i);
        final String userID = posts.getUser();
        final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Picasso.get().load(posts.getPost()).into(postsViewHolder.postImage);
        postsViewHolder.likes.setText("");
        postsViewHolder.like.setBackgroundResource(R.drawable.like);
        postsViewHolder.textLikes.setVisibility(View.GONE);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("saved").child(posts.getPostID()).exists())
                {
                    postsViewHolder.save.setBackgroundResource(R.drawable.save);
                    postsViewHolder.save.setEnabled(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        postsViewHolder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("postID", posts.getPostID());
                context.startActivity(intent);
            }
        });
        rootRef = FirebaseDatabase.getInstance().getReference();
        postsViewHolder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                postsViewHolder.save.setBackgroundResource(R.drawable.save);
                postsViewHolder.save.setEnabled(false);
                HashMap<String, Object> savedPosts = new HashMap<>();
                HashMap<String, Object> user = new HashMap<>();
                savedPosts.put("post", posts.getPost());
                user.put(posts.getPostID() + "/", savedPosts);
                rootRef.child("Users").child(currentUserID).child("saved").updateChildren(user);
            }
        });
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        postsRef.child(posts.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("likes").exists())
                {
                    final long count = dataSnapshot.child("likes").getChildrenCount();
                    postsViewHolder.textLikes.setVisibility(View.VISIBLE);
                    postsViewHolder.likes.setText(""+count);
                }
                if(dataSnapshot.child("likes").child(currentUserID).exists())
                {
                    postsViewHolder.like.setBackgroundResource(R.drawable.like_button);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        postsViewHolder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(likeState==0)
                {
                    final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap<String, Object> like = new HashMap<>();
                    HashMap<String, Object> user = new HashMap<>();

                    like.put("user", currentUserID);
                    user.put(currentUserID + "/", like);
                    postsRef.child(posts.getPostID()).child("likes")
                            .updateChildren(user);

                    likeState=1;
                }
                else if(likeState==1)
                {
                    postsRef.child(posts.getPostID()).child("likes").child(currentUserID).removeValue();
                    likeState = 0;
                }
            }
        });
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("image").exists())
                {
                    final String image = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(image).into(postsViewHolder.personImage);
                }
                else
                {
                    Toast.makeText(context, "No Image", Toast.LENGTH_SHORT).show();
                    postsViewHolder.personImage.setImageResource(R.drawable.user);
                }
                final String name = dataSnapshot.child("name").getValue().toString();
                postsViewHolder.personName.setText(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        postsViewHolder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageViewerActivity.class);
                intent.putExtra("url", posts.getPost());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }
}

