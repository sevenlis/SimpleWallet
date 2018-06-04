package by.sevenlis.simplewallet.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import by.sevenlis.simplewallet.R;
import by.sevenlis.simplewallet.classes.Category;
import by.sevenlis.simplewallet.classes.Settings;
import by.sevenlis.simplewallet.database.DBLocal;

public class CategoryActivity extends AppCompatActivity {
    Context context;
    DBLocal dbLocal;
    List<Category> categoryList;
    ListCategoryAdapter listCategoryAdapter;
    Category selectedCategory;
    int listFontSize;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        
        context = CategoryActivity.this;
        dbLocal = new DBLocal(context);
        categoryList = dbLocal.getCategories();
        
        listFontSize = Settings.getListFontSize(context);
    
        ListView listCategory = findViewById(R.id.listCategory);
        listCategoryAdapter = new ListCategoryAdapter();
        listCategory.setAdapter(listCategoryAdapter);
        listCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long tag) {
                selectedCategory = categoryList.get(position);
                if (selectedCategory.isDefault()) {
                    Toast.makeText(context, "Нельзя редактировать категории по умолчанию.", Toast.LENGTH_SHORT).show();
                    return;
                }
                getCategoryDialog().show();
            }
        });
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(R.string.edit_categories);
        }
        
        setResult(RESULT_CANCELED);
    }
    
    @SuppressLint("InflateParams")
    private Dialog getCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.edit_categories);
        builder.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.category_layout,null,false);
        final EditText editTextName = view.findViewById(R.id.editTextName);
        final RadioButton radioButtonExpense = view.findViewById(R.id.radioButtonExpense);
        final RadioButton radioButtonIncome = view.findViewById(R.id.radioButtonIncome);
        
        editTextName.setText(selectedCategory.getName());
        radioButtonExpense.setChecked(selectedCategory.isExpense());
        radioButtonIncome.setChecked(selectedCategory.isIncome());
        builder.setView(view);
        
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);
        
        final Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = AlertDialog.class.cast(dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int coef = 0;
                        if (radioButtonExpense.isChecked()) {
                            coef = -1;
                        } else if (radioButtonIncome.isChecked()){
                            coef = 1;
                        }
                        Category category = dbLocal.getCategoryByNameAndTypeCoef(editTextName.getText().toString(), coef);
                        if (category.getId() == -1) {
                            selectedCategory.setName(editTextName.getText().toString());
                            selectedCategory.setTypeCoef(coef);
                            dbLocal.updateCategory(selectedCategory);
                            
                            listCategoryAdapter.notifyDataSetChanged();
        
                            dialog.dismiss();
                            
                            setResult(RESULT_OK);
                        } else {
                            Toast.makeText(context, "Такая категория уже существует!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        
        return dialog;
    }
    
    
    class ListCategoryAdapter extends BaseAdapter {
    
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.spinner_category_dropdown_item, viewGroup,false);
            }
            ImageView imageView = view.findViewById(R.id.imageView);
            TextView textView = view.findViewById(R.id.textView);
    
            Category category = categoryList.get(i);
            imageView.setImageResource(category.getImageRes());
            textView.setText(category.getName());
            
            if (category.isDefault()) {
                textView.setTextColor(context.getResources().getColor(R.color.colorLightGrey));
            }
            
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, listFontSize);
    
            return view;
        }
    }
    
}