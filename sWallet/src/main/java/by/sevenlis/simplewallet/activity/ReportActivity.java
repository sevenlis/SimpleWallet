package by.sevenlis.simplewallet.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import by.sevenlis.simplewallet.R;
import by.sevenlis.simplewallet.adapter.ExpListViewAdapter;
import by.sevenlis.simplewallet.classes.ExpListGroup;
import by.sevenlis.simplewallet.classes.Settings;
import by.sevenlis.simplewallet.database.DBLocal;

public class ReportActivity extends AppCompatActivity {
    Context context;
    Date startDate;
    Date endDate;
    DBLocal dbLocal;
    TextView textViewPeriod;
    
    ExpandableListView expListView;
    List<ExpListGroup> expListViewGroups;
    ExpListViewAdapter expListViewAdapter;
    
    View expListViewInBalance;
    View expListViewOutBalance;
    View expListViewTotalIncome;
    View expListViewTotalExpense;
    
    int listFontSize;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
    
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        
        context = ReportActivity.this;
        
        dbLocal = new DBLocal(context);
        
        listFontSize = Settings.getListFontSize(context);
        
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        
        Calendar end = Calendar.getInstance();
        end.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
        
        startDate = dbLocal.roundCalendarToStart(start).getTime();
        endDate = dbLocal.roundCalendarToEnd(end).getTime();
    
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        textViewPeriod = toolbar.findViewById(R.id.textViewPeriod);
        textViewPeriod.setText(getPeriodLabel());
        textViewPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = getPeriodSelectDialog();
                dialog.show();
            }
        });
        
        expListView = findViewById(R.id.expListView);
        setupExpListView();
    }
    
    public void setupExpListView() {
        expListViewGroups = dbLocal.getExpListGroups(startDate, endDate);
        expListViewAdapter = new ExpListViewAdapter(context,expListViewGroups);
        expListView.setAdapter(expListViewAdapter);
    
        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//                int itemType = ExpandableListView.getPackedPositionType(id);
//                if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
//                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
//                    int childPosition = ExpandableListView.getPackedPositionChild(id);
//
//                    //do your per-item callback here
//                    return true; //true if we consumed the click, false if not
//
//                } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
//                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
//                    //do your per-group callback here
//                    return true; //true if we consumed the click, false if not
//
//                } else {
//                    return false;
//                }
                int count = expListViewAdapter.getGroupCount();
                for (int i = 0; i < count; i++) {
                    if (expListView.isGroupExpanded(i)) {
                        expListView.collapseGroup(i);
                    } else {
                        expListView.expandGroup(i);
                    }
                }
                return true;
            }
        });
    
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                expListView.collapseGroup(groupPosition);
                return true;
            }
        });
    
        if (expListViewInBalance != null) {
            expListView.removeHeaderView(expListViewInBalance);
        }
        expListViewInBalance = getExpListInBalance();
        
        if (expListViewOutBalance != null) {
            expListView.removeFooterView(expListViewOutBalance);
        }
        expListViewOutBalance = getExpListOutBalance();
        
        if (expListViewTotalIncome != null) {
            expListView.removeFooterView(expListViewTotalIncome);
        }
        expListViewTotalIncome = getExpListViewTotalIncome();
        
        if (expListViewTotalExpense != null) {
            expListView.removeFooterView(expListViewTotalExpense);
        }
        expListViewTotalExpense = getExpListViewTotalExpense();
        
        expListView.addHeaderView(expListViewInBalance);
        expListView.addFooterView(expListViewTotalIncome);
        expListView.addFooterView(expListViewTotalExpense);
        expListView.addFooterView(expListViewOutBalance);
        
        boolean expandOnLoad = Settings.getExpandListOnLoad(context);
        if (expandOnLoad) {
            int count = expListViewAdapter.getGroupCount();
            for (int i = 0; i < count; i++) {
                expListView.expandGroup(i);
            }
        }
    }
    
    private String getPeriodLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("EE. dd.MM.yyyy", Locale.getDefault());
        return sdf.format(startDate) + " - " + sdf.format(endDate);
    }
    
    @SuppressLint("InflateParams")
    private AlertDialog getPeriodSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.report_dialog_period_select_layout,null,false);
        TextView textViewStart = view.findViewById(R.id.textViewStart);
        textViewStart.setText(getString(R.string.period_start_title));
        TextView textViewEnd = view.findViewById(R.id.textViewEnd);
        textViewEnd.setText(getString(R.string.period_end_title));
        
        builder.setView(view);
        
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        final DatePicker datePickerStart = view.findViewById(R.id.datePickerStart);
        datePickerStart.updateDate(start.get(Calendar.YEAR),start.get(Calendar.MONTH),start.get(Calendar.DAY_OF_MONTH));
    
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        final DatePicker datePickerEnd = view.findViewById(R.id.datePickerEnd);
        datePickerEnd.updateDate(end.get(Calendar.YEAR),end.get(Calendar.MONTH),end.get(Calendar.DAY_OF_MONTH));
    
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Calendar start = Calendar.getInstance();
                start.set(datePickerStart.getYear(),datePickerStart.getMonth(),datePickerStart.getDayOfMonth());
                startDate = dbLocal.roundCalendarToStart(start).getTime();
    
                Calendar end = Calendar.getInstance();
                end.set(datePickerEnd.getYear(),datePickerEnd.getMonth(),datePickerEnd.getDayOfMonth());
                endDate = dbLocal.roundCalendarToEnd(end).getTime();
                
                textViewPeriod.setText(getPeriodLabel());
                setupExpListView();
            }
        });
        
        builder.setNegativeButton(getString(R.string.cancel),null);
    
        return builder.create();
    }
    
    @SuppressLint("InflateParams")
    private View getExpListInBalance() {
        double inBalance = dbLocal.getInBalance(startDate);
        View view = getLayoutInflater().inflate(R.layout.exp_list_balance,null,false);
        TextView textViewBalance = view.findViewById(R.id.textViewBalance);
        String text = getString(R.string.in_balance_title) + " " + String.format(Locale.getDefault(),"%.2f",inBalance) + " " + getString(R.string.rub_kop);
        textViewBalance.setText(text);
    
        if (inBalance < 0) {
            textViewBalance.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
    
        textViewBalance.setTextSize(TypedValue.COMPLEX_UNIT_DIP, listFontSize);
    
        return view;
    }
    
    @SuppressLint("InflateParams")
    private View getExpListOutBalance() {
        double outBalance = dbLocal.getOutBalance(endDate);
        View view = getLayoutInflater().inflate(R.layout.exp_list_balance,null,false);
        TextView textViewBalance = view.findViewById(R.id.textViewBalance);
        String text = getString(R.string.out_balance_title) + " " + String.format(Locale.getDefault(),"%.2f",outBalance) + " " + getString(R.string.rub_kop);
        textViewBalance.setText(text);
    
        if (outBalance < 0) {
            textViewBalance.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
    
        textViewBalance.setTextSize(TypedValue.COMPLEX_UNIT_DIP, listFontSize);
    
        return view;
    }
    
    @SuppressLint("InflateParams")
    private View getExpListViewTotalIncome() {
        View view = getLayoutInflater().inflate(R.layout.exp_list_balance,null,false);
        TextView textViewBalance = view.findViewById(R.id.textViewBalance);
        String text = getString(R.string.total_income_title) + " " + String.format(Locale.getDefault(),"%.2f",dbLocal.getIncomeTurnover(startDate,endDate)) + " " + getString(R.string.rub_kop);
        textViewBalance.setText(text);
        textViewBalance.setTextSize(TypedValue.COMPLEX_UNIT_DIP, listFontSize);
        return view;
    }
    
    @SuppressLint("InflateParams")
    private View getExpListViewTotalExpense() {
        View view = getLayoutInflater().inflate(R.layout.exp_list_balance,null,false);
        TextView textViewBalance = view.findViewById(R.id.textViewBalance);
        String text = getString(R.string.total_expense_title) + " " + String.format(Locale.getDefault(),"%.2f",dbLocal.getExpenseTurnover(startDate,endDate)) + " " + getString(R.string.rub_kop);
        textViewBalance.setText(text);
        textViewBalance.setTextColor(context.getResources().getColor(R.color.colorRed));
        textViewBalance.setTextSize(TypedValue.COMPLEX_UNIT_DIP, listFontSize);
        return view;
    }
}
