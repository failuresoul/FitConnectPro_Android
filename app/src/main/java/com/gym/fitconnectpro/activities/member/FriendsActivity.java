package com.gym.fitconnectpro.activities.member;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.fragments.social.FriendRequestsFragment;
import com.gym.fitconnectpro.fragments.social.FriendsListFragment;

public class FriendsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tabLayout = findViewById(R.id.tabLayoutFriends);
        viewPager = findViewById(R.id.viewPagerFriends);

        setupViewPager();
    }

    private void setupViewPager() {
        FriendsPagerAdapter adapter = new FriendsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("My Friends");
                    break;
                case 1:
                    tab.setText("Requests");
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class FriendsPagerAdapter extends FragmentStateAdapter {

        public FriendsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new FriendsListFragment();
            } else {
                return new FriendRequestsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
