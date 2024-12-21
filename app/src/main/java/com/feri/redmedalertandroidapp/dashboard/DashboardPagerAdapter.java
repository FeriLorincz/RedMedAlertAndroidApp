package com.feri.redmedalertandroidapp.dashboard;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.feri.redmedalertandroidapp.dashboard.fragments.SensorDataFragment;
import com.feri.redmedalertandroidapp.dashboard.fragments.MedicalProfileFragment;
import com.feri.redmedalertandroidapp.dashboard.fragments.PersonalDataFragment;
import com.feri.redmedalertandroidapp.dashboard.fragments.EmergencyContactsFragment;

public class DashboardPagerAdapter extends FragmentStateAdapter{

    public DashboardPagerAdapter(FragmentActivity activity) {
        super(activity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SensorDataFragment();
            case 1:
                return new MedicalProfileFragment();
            case 2:
                return new PersonalDataFragment();
            case 3:
                return new EmergencyContactsFragment();
            default:
                return new SensorDataFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
