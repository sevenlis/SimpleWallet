package by.sevenlis.simplewallet.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.ads.MobileAds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import by.sevenlis.simplewallet.R;
import by.sevenlis.simplewallet.adapter.DateOperationsPagerAdapter;
import by.sevenlis.simplewallet.database.DBLocal;
import by.sevenlis.simplewallet.update.CheckUpdateService;

public class MainActivity extends AppCompatActivity {
    public static final int ADD_OPERATION_REQUEST_CODE = 0;
    public static final int EDIT_CATEGORY_REQUEST_CODE = 1;
    
    private final int REQUEST_WRITE_EXT_STORAGE_CODE = 2;
    private final int CHANGE_SETTINGS_REQUEST_CODE = 3;
    
    Context context;
    List<Long> dateLongs = new ArrayList<>();
    String[] dateTitles = new String[]{};
    Date selectedDate;
    int selectedPosition;
    Spinner spDate;
    DBLocal dbLocal;
    
    ViewPager viewPager;
    PagerTabStrip pagerTabStrip;
    DateOperationsPagerAdapter dateOperationsPagerAdapter;
    ArrayAdapter<String> spDateAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4940093494596885~7975299737");
    
        context = MainActivity.this;
        
        dbLocal = new DBLocal(context);
    
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    
        viewPager = findViewById(R.id.viewPager);
        pagerTabStrip = findViewById(R.id.pagerTabStrip);
        pagerTabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        pagerTabStrip.setTextColor(getResources().getColor(R.color.colorBlue));
        
        spDate = toolbar.findViewById(R.id.spDate);
        
        spDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long tag) {
                selectedDate = new Date(dateLongs.get(position));
                selectedPosition = position;
                viewPager.setCurrentItem(position);
            }
    
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        
            @Override
            public void onPageSelected(int position) {
                selectedDate = new Date(dateLongs.get(position));
                selectedPosition = position;
                spDate.setSelection(position);
            }
        
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OperationActivity.class);
                intent.setAction("NEW_OPERATION");
                startActivityForResult(intent,ADD_OPERATION_REQUEST_CODE);
            }
        });
        
        FloatingActionButton fabReport = findViewById(R.id.fabReport);
        fabReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ReportActivity.class));
            }
        });
    
        initArrays();
        
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionWithRationale();
        } else {
            startService(new Intent(getApplicationContext(), CheckUpdateService.class));
        }
        
    }
    
    public void requestPermissionWithRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            final String message = getString(R.string.storage_permission_rationale);
            Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.grant), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestWriteExtStorage();
                        }
                    }).show();
        } else {
            requestWriteExtStorage();
        }
    }
    
    public void requestWriteExtStorage() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXT_STORAGE_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXT_STORAGE_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(getApplicationContext(), CheckUpdateService.class));
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_OPERATION_REQUEST_CODE && resultCode == RESULT_OK) {
            long dateMillis = dbLocal.roundCalendarToStart(Calendar.getInstance()).getTimeInMillis();
            if (data != null && data.getExtras() != null) {
                dateMillis = data.getLongExtra("dateLong",dateMillis);
            }
            if (!dateLongs.contains(dateMillis) || selectedDate == null) {
                initArrays();
                spDate.setSelection(0);
                dateOperationsPagerAdapter.notifyDataSetChanged();
            } else if (dateLongs.indexOf(dateMillis) != -1) {
                spDate.setSelection(dateLongs.indexOf(dateMillis));
                dateOperationsPagerAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == EDIT_CATEGORY_REQUEST_CODE && resultCode == RESULT_OK) {
            initArrays();
            if (selectedDate != null && dateLongs.indexOf(selectedDate.getTime()) != -1) {
                spDate.setSelection(dateLongs.indexOf(selectedDate.getTime()));
                dateOperationsPagerAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == CHANGE_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            initArrays();
            if (selectedDate != null && dateLongs.indexOf(selectedDate.getTime()) != -1) {
                spDate.setSelection(dateLongs.indexOf(selectedDate.getTime()));
            }
            dateOperationsPagerAdapter.notifyDataSetChanged();
        }
    }
    
    private void initArrays() {
        List<Date> dates = dbLocal.getOperationDatesDesc();
        dateLongs = new ArrayList<>();
        dateTitles = new String[dates.size()];
        SimpleDateFormat sdf = new SimpleDateFormat("EE. dd.MM.yyyy", Locale.getDefault());
        
        for (int i = 0; i < dates.size(); i++) {
            Date date = dates.get(i);
            dateLongs.add(date.getTime());
            dateTitles[i] = sdf.format(date);
        }
    
        spDateAdapter = new ArrayAdapter<>(context, R.layout.spinner_date_item, dateTitles);
        spDateAdapter.setDropDownViewResource(R.layout.spinner_date_dropdown_item);
        spDate.setAdapter(spDateAdapter);
    
        dateOperationsPagerAdapter = new DateOperationsPagerAdapter(getSupportFragmentManager(), dateLongs);
        viewPager.setAdapter(dateOperationsPagerAdapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(context,SettingsActivity.class);
            startActivityForResult(intent,CHANGE_SETTINGS_REQUEST_CODE);
            return true;
        } else if (id == R.id.action_edit_category) {
            Intent intent = new Intent(context,CategoryActivity.class);
            startActivityForResult(intent,EDIT_CATEGORY_REQUEST_CODE);
            return true;
        } else if (id == R.id.action_report) {
            Intent intent = new Intent(context,ReportActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(context,AboutActivity.class);
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,CheckUpdateService.class));
    }
}
