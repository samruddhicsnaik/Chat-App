package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;
    private String currentUserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified())
        {
            toolbar = findViewById(R.id.main_page_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("LetsChat");
            loadFragment(new ChatsFragment());
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item)
                {
                    Fragment fragment = null;
                    switch (item.getItemId())
                    {
                        case R.id.navigation_home:
                        {
                            fragment = new ChatsFragment();
                            break;
                        }
                        case R.id.navigation_saved_post:
                        {
                            fragment = new SavedPostsFragment();
                            break;
                        }
                        case R.id.navigation_groups:
                        {
                            fragment = new GroupsFragment();
                            break;
                        }
                        case R.id.navigation_posts:
                        {
                            fragment = new StoriesFragment();
                            break;
                        }
                        case R.id.navigation_friends:
                        {
                            fragment = new FriendsFragment();
                            break;
                        }
                    }
                    return loadFragment(fragment);
                }
            });
        }
        else
        {
            loginActivity();
        }
    }

    private boolean loadFragment(Fragment fragment)
    {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser==null && !currentUser.isEmailVerified())
        {
            loginActivity();
        }
        else
        {
            updateUserStatus("online");
            verifyLogin();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("online");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }

    private void verifyLogin()
    {
        if(firebaseAuth.getCurrentUser()!=null && firebaseAuth.getCurrentUser().isEmailVerified())
        {
            String currentUserID = firebaseAuth.getCurrentUser().getUid();
            rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if((dataSnapshot.child("name")).exists())
                    {

                    }
                    else
                    {
                        settingsActivity();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void loginActivity()
    {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void settingsActivity()
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void peopleActivity()
    {
        Intent intent = new Intent(MainActivity.this, PeopleActivity.class);
        startActivity(intent);
    }

    private void requestsActivity()
    {
        Intent intent = new Intent(MainActivity.this, RequestsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.people)
        {
            peopleActivity();
        }
        if(item.getItemId()==R.id.profile)
        {
            settingsActivity();
        }
        if(item.getItemId()==R.id.logout)
        {
            updateUserStatus("offline");
            rootRef.child("Users").child(currentUserID).child("device_token").removeValue();
            firebaseAuth.signOut();
            loginActivity();
        }
        if(item.getItemId()==R.id.requests)
        {
            requestsActivity();
        }
        return true;
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
            rootRef.child("Users").child(currentUserID).child("userState")
                    .updateChildren(onlineState);
        }
    }
}
