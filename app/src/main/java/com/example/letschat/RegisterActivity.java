package com.example.letschat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;

public class RegisterActivity extends AppCompatActivity
{
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewPager = findViewById(R.id.register_tabs_pager);
        RegisterTabsAccessorAdapter registerTabsAccessorAdapter = new RegisterTabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(registerTabsAccessorAdapter);
        tabLayout = findViewById(R.id.register_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


}
