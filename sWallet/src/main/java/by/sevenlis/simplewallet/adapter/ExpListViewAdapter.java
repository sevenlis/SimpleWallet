package by.sevenlis.simplewallet.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import by.sevenlis.simplewallet.R;
import by.sevenlis.simplewallet.classes.ExpListGroup;
import by.sevenlis.simplewallet.classes.Operation;
import by.sevenlis.simplewallet.classes.Settings;

public class ExpListViewAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<ExpListGroup> mGroups;
    private LayoutInflater layoutInflater;
    private int listFontSize;
    
    public ExpListViewAdapter(Context context, List<ExpListGroup> mGroups) {
        this.context = context;
        this.mGroups = mGroups;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listFontSize = Settings.getListFontSize(context);
    }
    
    @Override
    public int getGroupCount() {
        return mGroups.size();
    }
    
    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroups.get(groupPosition).getCount();
    }
    
    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return ((ExpListGroup)getGroup(groupPosition)).getOperation(childPosition);
    }
    
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    
    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.exp_list_group,viewGroup,false);
        
        String text;
        
        ImageView imageCategory = view.findViewById(R.id.imageCategory);
        TextView textViewName = view.findViewById(R.id.textViewName);
        TextView textViewSum = view.findViewById(R.id.textViewSum);
        
        ExpListGroup group = (ExpListGroup) getGroup(groupPosition);
        
        imageCategory.setImageResource(group.getCategory().getImageRes());
        textViewName.setText(group.getCategory().getName());
        text = String.format(Locale.getDefault(),"%.2f",group.getTotal()).trim();
        textViewSum.setText(text);
        
        if (group.getCategory().getTypeCoef() < 0) {
            textViewSum.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
    
        textViewName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, listFontSize);
        textViewSum.setTextSize(TypedValue.COMPLEX_UNIT_DIP, listFontSize);
        
        return view;
    }
    
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.exp_list_child,viewGroup,false);
        
        String text;
        
        TextView textViewTime = view.findViewById(R.id.textViewTime);
        TextView textViewSum = view.findViewById(R.id.textViewSum);
        TextView textViewComment = view.findViewById(R.id.textViewComment);
    
        ExpListGroup group = (ExpListGroup) getGroup(groupPosition);
    
        Operation operation = (Operation) getChild(groupPosition, childPosition);
    
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss",Locale.getDefault());
        text = sdf.format(operation.getDate());
        textViewTime.setText(text);
        
        text = String.format(Locale.getDefault(),"%.2f",operation.getSum()).trim();
        textViewSum.setText(text);
    
        if (group.getCategory().getTypeCoef() < 0) {
            textViewSum.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        
        textViewComment.setText(operation.getComment());
        
        return view;
    }
    
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
