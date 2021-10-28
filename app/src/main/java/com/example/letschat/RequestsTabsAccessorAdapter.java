package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class RequestsTabsAccessorAdapter extends FragmentPagerAdapter
{
    public RequestsTabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                ReceivedRequestsFragment receivedRequestsFragment = new ReceivedRequestsFragment();
                return receivedRequestsFragment;
            case 1:
                SentRequestsFragment sentRequestsFragment = new SentRequestsFragment();
                return sentRequestsFragment;
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
                return "Received";
            case 1:
                return "Sent";
            default:
                return null;
        }
    }
}
