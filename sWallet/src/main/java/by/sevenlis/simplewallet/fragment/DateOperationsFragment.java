package by.sevenlis.simplewallet.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import by.sevenlis.simplewallet.R;
import by.sevenlis.simplewallet.activity.OperationActivity;
import by.sevenlis.simplewallet.adapter.ExpListViewAdapter;
import by.sevenlis.simplewallet.classes.ExpListGroup;
import by.sevenlis.simplewallet.classes.Operation;
import by.sevenlis.simplewallet.classes.Settings;
import by.sevenlis.simplewallet.database.DBLocal;

import static android.app.Activity.RESULT_OK;

public class DateOperationsFragment extends Fragment implements IUpdatable {
    public static final int EDIT_OPERATION_REQUEST_CODE = 0;
    private static final int POPUP_MENU_OPERATION_DELETE = 0;
    private static final int POPUP_MENU_OPERATION_EDIT = 1;
    
    Context context;
    DBLocal dbLocal;
    Date date;
    List<ExpListGroup> expListViewGroups;
    
    ExpandableListView expListView;
    
    ExpListViewAdapter expListViewAdapter;
    View expListViewHeader;
    View expListViewFooter;
    
    int listFontSize;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        dbLocal = new DBLocal(context);
        date = new Date();
        listFontSize = Settings.getListFontSize(context);
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            setDate(new Date(args.getLong("DATE_MILLIS")));
        }
        return inflater.inflate(R.layout.date_operations_fragment,container,false);
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expListView = view.findViewById(R.id.expListView);
        setupExpListView();
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    private void showChildPopupMenu(View view, final Operation operation) {
        final PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add(0, POPUP_MENU_OPERATION_DELETE, 0, getString(R.string.del_operation));
        popupMenu.getMenu().add(0, POPUP_MENU_OPERATION_EDIT, 0, getString(R.string.edit_operation));
        popupMenu.setGravity(Gravity.END);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == POPUP_MENU_OPERATION_DELETE) {
                    deleteOperation(operation);
                } else if (itemId == POPUP_MENU_OPERATION_EDIT) {
                    editOperation(operation);
                }
                return false;
            }
        });
        popupMenu.show();
    }
    
    private void deleteOperation(Operation operation) {
        dbLocal.deleteOperation(operation);
        setupExpListView();
    }
    
    private void editOperation(Operation operation) {
        Intent intent = new Intent(context, OperationActivity.class);
        intent.setAction("EDIT_OPERATION");
        intent.putExtra("OPERATION_ID",operation.getId());
        startActivityForResult(intent,EDIT_OPERATION_REQUEST_CODE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_OPERATION_REQUEST_CODE && resultCode == RESULT_OK) {
            setupExpListView();
        }
    }
    
    public void setupExpListView() {
        expListViewGroups = dbLocal.getExpListGroups(date, date);
        expListViewAdapter = new ExpListViewAdapter(context,expListViewGroups);
        expListView.setAdapter(expListViewAdapter);
        
        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);
                if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    
                    Operation operation = (Operation) expListViewAdapter.getChild(groupPosition,childPosition);
                    showChildPopupMenu(view,operation);

                    return true;

                } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int count = expListViewAdapter.getGroupCount();
                    for (int i = 0; i < count; i++) {
                        if (expListView.isGroupExpanded(i)) {
                            expListView.collapseGroup(i);
                        } else {
                            expListView.expandGroup(i);
                        }
                    }
                    return true;

                } else {
                    return false;
                }
            }
        });
        
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                expListView.collapseGroup(groupPosition);
                return true;
            }
        });
        
        if (expListViewHeader != null) {
            expListView.removeHeaderView(expListViewHeader);
        }
        expListViewHeader = getExpListHeader();
        
        if (expListViewFooter != null) {
            expListView.removeFooterView(expListViewFooter);
        }
        expListViewFooter = getExpListFooter();
        
        expListView.addHeaderView(expListViewHeader);
        expListView.addFooterView(expListViewFooter);
        
        boolean expandOnLoad = Settings.getExpandListOnLoad(context);
        if (expandOnLoad) {
            int count = expListViewAdapter.getGroupCount();
            for (int i = 0; i < count; i++) {
                expListView.expandGroup(i);
            }
        }
    }
    
    @SuppressLint("InflateParams")
    private View getExpListHeader() {
        double inBalance = dbLocal.getInBalance(date);
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
    private View getExpListFooter() {
        double outBalance = dbLocal.getOutBalance(date);
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
    
    @Override
    public void update() {
        setupExpListView();
    }
}

