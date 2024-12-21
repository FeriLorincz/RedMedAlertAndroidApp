package com.feri.redmedalertandroidapp.dashboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.feri.redmedalertandroidapp.R;

public class DashboardActivity extends AppCompatActivity{

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        setupViewPager();
        setupTabs();
    }

    private void setupViewPager() {
        DashboardPagerAdapter pagerAdapter = new DashboardPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    private void setupTabs() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Date Senzori");
                    break;
                case 1:
                    tab.setText("Profil Medical");
                    break;
                case 2:
                    tab.setText("Date Personale");
                    break;
                case 3:
                    tab.setText("Contacte Urgență");
                    break;
            }
        }).attach();
    }
}
