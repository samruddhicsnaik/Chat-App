package com.example.letschat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RequestsActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private String currentUserID;
    private DatabaseReference userRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        toolbar = findViewById(R.id.requests_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Requests");

        viewPager = findViewById(R.id.requests_tabs_pager);
        RequestsTabsAccessorAdapter requestsTabsAccessorAdapter = new RequestsTabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(requestsTabsAccessorAdapter);
        tabLayout = findViewById(R.id.requests_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void updateUserStatus(String state)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
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
            userRef.child(currentUserID).child("userState")
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
}
