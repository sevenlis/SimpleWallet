package by.sevenlis.simplewallet.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import by.sevenlis.simplewallet.R;
import by.sevenlis.simplewallet.classes.Category;
import by.sevenlis.simplewallet.classes.Operation;
import by.sevenlis.simplewallet.database.DBLocal;

public class OperationActivity extends AppCompatActivity {
    Context context;
    DBLocal dbLocal;
    SpinnerCategoryAdapter spinnerCategoryAdapter;
    Category selectedCategory;
    
    Spinner spinnerCategory;
    EditText editTextSum;
    EditText editTextComment;
    Button buttonSubmit;
    List<Category> categoryList;
    
    Operation mOperation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        
        context = OperationActivity.this;
        
        spinnerCategory = findViewById(R.id.spinnerCategory);
        
        dbLocal = new DBLocal(context);
        categoryList = dbLocal.getCategories();
        categoryList.add(new Category(-1,0,getString(R.string.add_new_category)));
        
        spinnerCategoryAdapter = new SpinnerCategoryAdapter(categoryList);
        spinnerCategory.setSelection(0);
        spinnerCategory.setAdapter(spinnerCategoryAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long tag) {
                Category category = (Category) spinnerCategoryAdapter.getItem(position);
                if (category.getId() == -1) {
                    AlertDialog dialog = (AlertDialog) getCategoryDialog();
                    dialog.show();
                    if (dialog.getWindow() != null)
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                } else {
                    selectedCategory = category;
                }
            }
    
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        
        mOperation = new Operation();
        String action = getIntent().getAction();
        if (action != null && action.equals("EDIT_OPERATION")) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mOperation = dbLocal.getOperation(extras.getInt("OPERATION_ID",-1));
                spinnerCategory.setSelection(spinnerCategoryAdapter.findCategoryPosition(mOperation.getCategory().getId()));
            }
        }
        
        editTextSum = findViewById(R.id.editTextSum);
        editTextComment = findViewById(R.id.editTextComment);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(buttonSubmitOnClickListener);
        TextView textViewDate = findViewById(R.id.textViewDate);
        SimpleDateFormat sdf = new SimpleDateFormat("EE. dd.MM.yyyy", Locale.getDefault());
        textViewDate.setText(sdf.format(mOperation.getDate()));
        
        editTextSum.setText(String.valueOf(mOperation.getSum()));
        editTextComment.setText(mOperation.getComment());
    
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
    
    View.OnClickListener buttonSubmitOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent result = new Intent();
            
            String comment = editTextComment.getText().toString();
            double sum = Double.valueOf(editTextSum.getText().toString());
            
            if (sum == 0.0D) {
                Toast.makeText(context, getString(R.string.input_sum_or_press_back), Toast.LENGTH_SHORT).show();
                return;
            }
            
            String action = getIntent().getAction();
            if (action != null && action.equals("NEW_OPERATION")) {
                dbLocal.addOperation(new Operation(-1, Calendar.getInstance().getTime(),selectedCategory,comment,sum,selectedCategory.getTypeCoef()));
                Calendar cal = Calendar.getInstance();
                cal = dbLocal.roundCalendarToStart(cal);
    
                result.putExtra("dateLong",cal.getTimeInMillis());
                
            } else if (action != null && action.equals("EDIT_OPERATION")) {
                mOperation.setCategory(selectedCategory);
                mOperation.setComment(comment);
                mOperation.setSum(sum);
                dbLocal.updateOperation(mOperation);
    
                result.putExtra("dateLong",mOperation.getDate().getTime());
            }
            
            setResult(RESULT_OK,result);
            
            finish();
        }
    };
    
    @SuppressLint("InflateParams")
    private Dialog getCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.add_category);
        builder.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.category_layout,null,false);
        final EditText editTextName = view.findViewById(R.id.editTextName);
        final RadioButton radioButtonExpense = view.findViewById(R.id.radioButtonExpense);
        radioButtonExpense.setChecked(true);
        final RadioButton radioButtonIncome = view.findViewById(R.id.radioButtonIncome);
        builder.setView(view);
        
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int coef = 0;
                if (radioButtonExpense.isChecked()) {
                    coef = -1;
                } else if (radioButtonIncome.isChecked()){
                    coef = 1;
                }
                Category category = dbLocal.getCategoryByNameAndTypeCoef(editTextName.getText().toString(), coef);
                if (category.getId() == -1) {
                    category.setName(editTextName.getText().toString());
                    category.setTypeCoef(coef);
                    dbLocal.addOrUpdateCategory(category);
                    
                    category = dbLocal.getCategoryByNameAndTypeCoef(category.getName(), category.getTypeCoef());
                    category = spinnerCategoryAdapter.addItem(category);
                    spinnerCategory.setSelection(spinnerCategoryAdapter.indexOf(category));
                } else {
                    spinnerCategory.setSelection(spinnerCategoryAdapter.findCategoryPosition(category));
                }
            }
        });
        
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                spinnerCategory.setSelection(0);
            }
        });
        
        spinnerCategory.setSelection(0);
        
        return builder.create();
    }
    
    class SpinnerCategoryAdapter implements SpinnerAdapter {
        List<Category> categoryList;
    
        SpinnerCategoryAdapter(List<Category> categoryList) {
            this.categoryList = categoryList;
        }
        
        Category addItem(Category category) {
            categoryList.add(categoryList.size() - 1, category);
            return category;
        }
        
        int indexOf(Category category) {
            return categoryList.indexOf(category);
        }
        
        int findCategoryPosition(Category category) {
            int pos = 0;
            for (int i = 0; i < categoryList.size(); i++) {
                Category cat = categoryList.get(i);
                if (cat.getName().equals(category.getName()) && cat.getTypeCoef() == category.getTypeCoef()) {
                    pos = i;
                    break;
                }
            }
            return pos;
        }
    
        int findCategoryPosition(int categoryId) {
            int pos = 0;
            for (int i = 0; i < categoryList.size(); i++) {
                Category cat = categoryList.get(i);
                if (cat.getId() == categoryId) {
                    pos = i;
                    break;
                }
            }
            return pos;
        }
    
        @Override
        public View getDropDownView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.spinner_category_dropdown_item, viewGroup,false);
            }
            ImageView imageView = view.findViewById(R.id.imageView);
            TextView textView = view.findViewById(R.id.textView);

            Category category = categoryList.get(i);
            imageView.setImageResource(category.getImageRes());
            textView.setText(category.getName());

            return view;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {}

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {}
    
        @Override
        public int getCount() {
            return categoryList.size();
        }
    
        @Override
        public Object getItem(int i) {
            return categoryList.get(i);
        }
    
        @Override
        public long getItemId(int i) {
            return i;
        }
    
        @Override
        public boolean hasStableIds() {
            return false;
        }
    
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.spinner_category_item, viewGroup,false);
            }
            ImageView imageView = view.findViewById(R.id.imageView);
            TextView textView = view.findViewById(R.id.textView);
    
            Category category = categoryList.get(i);
            imageView.setImageResource(category.getImageRes());
            textView.setText(category.getName());
    
            return view;
        }
    
        @Override
        public int getItemViewType(int i) {
            return i;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
