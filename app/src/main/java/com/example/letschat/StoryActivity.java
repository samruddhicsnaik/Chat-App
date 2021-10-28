package com.example.letschat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class StoryActivity extends AppCompatActivity
{
    private ProgressBar progressBar;
    private ImageView story;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        story = findViewById(R.id.story);
        String post = getIntent().getStringExtra("post");
        Picasso.get().load(post).into(story);
        progressBar = findViewById(R.id.progress_bar);
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 1000);
        animation.setDuration(5000); // 0.5 second
        animation.start();
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
            }
        });
    }
}
