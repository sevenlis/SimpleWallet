package by.sevenlis.simplewallet.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import by.sevenlis.simplewallet.fragment.DateOperationsFragment;

public class DateOperationsPagerAdapter extends FragmentStatePagerAdapter {
    private List<Long> dateLongs;
    
    public DateOperationsPagerAdapter(FragmentManager fragmentManager, List<Long> dateLongs) {
        super(fragmentManager);
        this.dateLongs = dateLongs;
    }
    
    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putLong("DATE_MILLIS",dateLongs.get(position));
        DateOperationsFragment fragment = new DateOperationsFragment();
        fragment.setDate(new Date(dateLongs.get(position)));
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public int getCount() {
        return dateLongs.size();
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("EE. dd.MM.yyyy", Locale.getDefault());
        Date date = new Date(dateLongs.get(position));
        return sdf.format(date);
    }
    
    @Override
    public int getItemPosition(Object object) {
        DateOperationsFragment fragment = DateOperationsFragment.class.cast(object);
        if (fragment != null) fragment.update();
        
        return super.getItemPosition(object);
        //return POSITION_NONE;
    }
}
