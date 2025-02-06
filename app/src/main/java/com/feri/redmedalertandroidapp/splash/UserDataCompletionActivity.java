package com.feri.redmedalertandroidapp.splash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.feri.redmedalertandroidapp.MainActivity;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.dashboard.fragments.EmergencyContactsFragment;
import com.feri.redmedalertandroidapp.dashboard.fragments.MedicalProfileFragment;
import com.feri.redmedalertandroidapp.dashboard.fragments.PersonalDataFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class UserDataCompletionActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button nextButton;
    private Button skipButton;
    private Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_completion);

        initializeViews();
        setupViewPager();
        setupButtons();
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);
        finishButton = findViewById(R.id.finishButton);
    }

    private void setupViewPager() {
        UserDataPagerAdapter pagerAdapter = new UserDataPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateButtonVisibility(position);
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Date Personale");
                    break;
                case 1:
                    tab.setText("Profil Medical");
                    break;
                case 2:
                    tab.setText("Contacte Urgență");
                    break;
            }
        }).attach();
    }

    private void setupButtons() {
        nextButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            viewPager.setCurrentItem(currentItem + 1);
        });

        skipButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            viewPager.setCurrentItem(currentItem + 1);
        });

        finishButton.setOnClickListener(v -> {
            // Salvăm starea și mergem la MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void updateButtonVisibility(int position) {
        boolean isLastPage = position == 2;
        nextButton.setVisibility(isLastPage ? View.GONE : View.VISIBLE);
        skipButton.setVisibility(isLastPage ? View.GONE : View.VISIBLE);
        finishButton.setVisibility(isLastPage ? View.VISIBLE : View.GONE);
    }

    public static class UserDataPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        public UserDataPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }
        @Override
        public androidx.fragment.app.Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new PersonalDataFragment();
                case 1:
                    return new MedicalProfileFragment();
                case 2:
                    return new EmergencyContactsFragment();
                default:
                    return new PersonalDataFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
