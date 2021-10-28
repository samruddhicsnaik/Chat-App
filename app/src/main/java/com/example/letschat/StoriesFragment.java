package com.example.letschat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import static android.app.Activity.RESULT_OK;

public class StoriesFragment extends Fragment
{
    private RecyclerView story, post;
    private DatabaseReference postsRef, userRef, storiesRef;
    private MemberAdapter memberAdapter;
    private PostsAdapter postsAdapter;
    private final List<Posts> postsList = new ArrayList<>();
    private final List<Posts> storyList = new ArrayList<>();
    private CircleImageView addStory;
    private Uri imageUri;
    private DatabaseReference rootRef;
    private String saveCurrentDate, saveCurrentTime;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private FloatingActionButton addPost;

    public StoriesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stories, container, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        story = view.findViewById(R.id.story_list);
        story.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        addStory = view.findViewById(R.id.add_story);
        addStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .getIntent(getContext());
                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
        addPost = view.findViewById(R.id.add_post);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .getIntent(getContext());
                startActivityForResult(intent, 1);
            }
        });
        post = view.findViewById(R.id.post_list);
        post.setLayoutManager(new LinearLayoutManager(getContext()));
        storiesRef = FirebaseDatabase.getInstance().getReference("Stories");
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postsAdapter = new PostsAdapter(getContext(), postsList);
        post.setAdapter(postsAdapter);
        storiesRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                storyList.clear();
                for(DataSnapshot postSnapShot : dataSnapshot.getChildren())
                {
                    Posts posts = postSnapShot.getValue(Posts.class);
                    storyList.add(posts);
                }
                Collections.reverse(storyList);
                memberAdapter = new MemberAdapter(getContext(), storyList);
                story.setAdapter(memberAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for(DataSnapshot postSnapShot : dataSnapshot.getChildren())
                {
                    Posts posts = postSnapShot.getValue(Posts.class);
                    postsList.add(posts);
                }
                Collections.reverse(postsList);
                postsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                imageUri = result.getUri();
                Toast.makeText(getContext(), ""+imageUri.toString(), Toast.LENGTH_SHORT).show();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Stories");
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                final String storyRef = "Stories/";
                final DatabaseReference storyKeyRef = rootRef.child("Stories").push();
                final String storyPushID = storyKeyRef.getKey();
                final StorageReference filePath = storageReference.child(storyPushID + ".jpg");
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
                                Map storyBody = new HashMap();
                                storyBody.put("user", currentUserID);
                                storyBody.put("post", downloadUrl);
                                storyBody.put("name", imageUri.getLastPathSegment());
                                storyBody.put("type", "image");
                                storyBody.put("postID", storyPushID);
                                storyBody.put("time", saveCurrentTime);
                                storyBody.put("date", saveCurrentDate);
                                Map storyDetails = new HashMap();
                                storyDetails.put(storyRef + "/" + storyPushID, storyBody);
                                rootRef.updateChildren(storyDetails);
                            }
                        });
                    }
                });
            }
        }
        if (requestCode == 1)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                imageUri = result.getUri();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Posts");
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                final String postRef = "Posts/";
                final DatabaseReference postKeyRef = rootRef.child("Posts").push();
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
        else
        {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
