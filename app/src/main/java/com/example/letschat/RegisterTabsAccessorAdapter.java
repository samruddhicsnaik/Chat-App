package com.example.letschat;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class RegisterTabsAccessorAdapter extends FragmentPagerAdapter
{

    public RegisterTabsAccessorAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                PhoneRegisterFragment phoneRegisterFragment = new PhoneRegisterFragment();
                return phoneRegisterFragment;
            case 1:
                EmailRegisterFragment emailRegisterFragment = new EmailRegisterFragment();
                return emailRegisterFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Phone Number";
            case 1:
                return "Email Address";
            default:
                return null;
        }
    }
}
