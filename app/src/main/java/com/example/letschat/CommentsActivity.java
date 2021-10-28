package com.example.letschat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private final List<Comments> commentsList = new ArrayList<>();
    private CommentsAdapter commentsAdapter;
    private DatabaseReference postRef, rootRef;
    private EditText typeComment;
    private String comment, postID;
    private Button addComment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        rootRef = FirebaseDatabase.getInstance().getReference();
        typeComment = findViewById(R.id.type_comment);
        addComment = findViewById(R.id.add_comment);
        postID = getIntent().getStringExtra("postID");
        recyclerView = findViewById(R.id.comments_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
        commentsAdapter = new CommentsAdapter(CommentsActivity.this, commentsList);
        recyclerView.setAdapter(commentsAdapter);
        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        addComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                 comment = typeComment.getText().toString().trim();
                 if(comment.isEmpty())
                 {
                     Toast.makeText(CommentsActivity.this, "Type a Comment", Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                     final DatabaseReference commentsRef = rootRef.child("Posts").child(postID).push();
                     final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                     HashMap<String, Object> comments = new HashMap<>();
                     HashMap<String, Object> user = new HashMap<>();
                     comments.put("user", currentUserID);
                     comments.put("comment", comment);
                     user.put(currentUserID + "/", comments);
                     postRef.child(postID).child("comments").updateChildren(user);
                     typeComment.setText("");
                 }
            }
        });
        postRef.child(postID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("comments").exists())
                {
                    commentsList.clear();
                    for(DataSnapshot postSnapShot : dataSnapshot.child("comments").getChildren())
                    {
                        Comments comment = postSnapShot.getValue(Comments.class);
                        commentsList.add(comment);
                    }
                }
                Collections.reverse(commentsList);
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
