package com.example.letschat;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import static android.app.Activity.RESULT_OK;

public class SavedPostsFragment extends Fragment
{
    private FloatingActionButton addPost;
    private Uri imageUri;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private DatabaseReference rootRef, userRef, postRef;
    private String saveCurrentDate, saveCurrentTime;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private StorageReference userProfileImageRef;
    final int PIC_CROP = 1;
    private ImageView imgView;

    public SavedPostsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_saved_posts, container, false);
        imageView = view.findViewById(R.id.image);
        recyclerView = view.findViewById(R.id.saved_posts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(userRef.child(currentUserID).child("saved"), Posts.class)
                .build();
        FirebaseRecyclerAdapter<Posts, SavedPostsViewHolder> adapter = new FirebaseRecyclerAdapter<Posts, SavedPostsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final SavedPostsViewHolder savedPostsViewHolder, final int i, @NonNull Posts posts)
            {
                Picasso.get().load(posts.getPost()).into(savedPostsViewHolder.savedPost);
            }

            @NonNull
            @Override
            public SavedPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_post_display, parent, false);
                SavedPostsViewHolder viewHolder = new SavedPostsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        return view;
    }

    public static class SavedPostsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView savedPost;
        public SavedPostsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            savedPost = itemView.findViewById(R.id.saved_post);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            imageUri = data.getData();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Posts");
            final String postRef = "Posts/";
            DatabaseReference postKeyRef = rootRef.child("Posts").push();
            final String postPushID = postKeyRef.getKey();
            final StorageReference filePath = storageReference.child(postPushID + ".jpg");
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                            saveCurrentDate = currentDate.format(calendar.getTime());
                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            saveCurrentTime = currentTime.format(calendar.getTime());
                            Map postBody = new HashMap();
                            postBody.put("user", currentUserID);
                            postBody.put("post", downloadUrl);
                            postBody.put("name", imageUri.getLastPathSegment());
                            postBody.put("type", "image");
                            postBody.put("postID", postPushID);
                            postBody.put("time", saveCurrentTime);
                            postBody.put("date", saveCurrentDate);
                            Map postDetails = new HashMap();
                            postDetails.put(postRef + "/" + postPushID, postBody);
                            rootRef.updateChildren(postDetails);
                        }
                    });
                }
            });
        }
    }
}
