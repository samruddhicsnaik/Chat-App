package com.example.letschat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>
{
    private Context context;
    private List<Comments> commentsList;
    private DatabaseReference userRef;

    public CommentsAdapter(Context context, List<Comments> commentsList)
    {
        this.context = context;
        this.commentsList = commentsList;
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView personImage;
        TextView name, comment;
        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            personImage = itemView.findViewById(R.id.user_image);
            name = itemView.findViewById(R.id.user_name);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_display, parent, false);
        CommentsViewHolder viewHolder = new CommentsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsViewHolder commentsViewHolder, final int i)
    {
        final Comments comments = commentsList.get(i);
        final String userID = comments.getUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("image").exists())
                {
                    final String image = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(image).into(commentsViewHolder.personImage);
                }
                else
                {
                    Toast.makeText(context, "No Image", Toast.LENGTH_SHORT).show();
                    commentsViewHolder.personImage.setImageResource(R.drawable.user);
                }
                commentsViewHolder.comment.setText(comments.getComment());
                userRef.child(comments.getUser()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("name").getValue().toString();
                        commentsViewHolder.name.setText(username);
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
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }
}
