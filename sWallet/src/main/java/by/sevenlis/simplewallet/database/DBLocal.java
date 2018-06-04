package by.sevenlis.simplewallet.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import by.sevenlis.simplewallet.classes.Category;
import by.sevenlis.simplewallet.classes.ExpListGroup;
import by.sevenlis.simplewallet.classes.Operation;

public class DBLocal {
    private DBHelper dbHelper;
    
    public DBLocal(Context context) {
        this.dbHelper = new DBHelper(context);
    }
    
    private void close() {
        if (dbHelper != null) dbHelper.close();
    }
    
    public Category getCategoryByNameAndTypeCoef(String name, int typeCoef) {
        Category category = new Category(-1,typeCoef,name);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM Category WHERE LOWER(name) = ? AND typeCoef = ? ORDER BY name ASC LIMIT 1";
        Cursor cursor = db.rawQuery(sql,new String[]{name.toLowerCase(),String.valueOf(typeCoef)});
        if (cursor.moveToFirst()) {
            category = new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("typeCoef")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name"))
            );
        }
        cursor.close();
        close();
        
        return category;
    }
    
    public Category getCategory(int id) {
        Category category = new Category();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(true,"Category",null,"_id = ?",new String[]{String.valueOf(id)},null,null,null,"1");
        
        if (cursor.moveToFirst()) {
            category = new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("typeCoef")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name"))
            );
        }
        cursor.close();
        close();
    
        return category;
    }
    
    public void addOrUpdateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("typeCoef",category.getTypeCoef());
        cv.put("name",category.getName());
        if (category.getId() == -1) {
            db.insert("Category","typeCoef, name",cv);
        } else {
            db.update("Category",cv,"_id = ?",new String[]{String.valueOf(category.getId())});
        }
        close();
    }
    
    public List<Category> getCategories() {
        List<Category> categoryList = new ArrayList<>();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Category",null);
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("typeCoef")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name"))
            );
            categoryList.add(category);
        }
        cursor.close();
        close();
        
        return categoryList;
    }
    
    public void addOperation(Operation operation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (operation.getId() == -1) {
            ContentValues cv = new ContentValues();
            cv.put("date", operation.getDate().getTime());
            cv.put("category_id", operation.getCategory().getId());
            cv.put("comment", operation.getComment());
            cv.put("sum", operation.getSum());
            cv.put("typeCoef", operation.getCategory().getTypeCoef());
            db.insert("Operation",null, cv);
        }
        close();
    }
    
    public Calendar roundCalendarToStart(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal;
    }
    
    public Calendar roundCalendarToEnd(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY,23);
        cal.set(Calendar.MINUTE,59);
        cal.set(Calendar.SECOND,59);
        cal.set(Calendar.MILLISECOND,999);
        return cal;
    }
    
    public List<Date> getOperationDatesDesc() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<Date> dates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(true,"Operation AS O",new String[]{"strftime('%Y-%m-%d', O.date/1000, 'unixepoch', 'localtime') AS s_date"},null,null,null,null,"date DESC",null);
        while (cursor.moveToNext()) {
            String sDate = cursor.getString(cursor.getColumnIndexOrThrow("s_date"));
            try {
                Date date = sdf.parse(sDate);
                dates.add(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        close();
        
        return dates;
    }
    
    private List<Category> getCategoriesForDate(Date dateStart, Date dateEnd) {
        List<Category> categories = new ArrayList<>();
        
        Calendar start = Calendar.getInstance();
        start.setTime(dateStart);
        roundCalendarToStart(start);
        
        Calendar end = Calendar.getInstance();
        end.setTime(dateEnd);
        roundCalendarToEnd(end);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT DISTINCT " +
                "C._id AS c_id, " +
                "C.typeCoef AS c_typeCoef, " +
                "C.name AS c_name " +
                "from Operation O " +
                "LEFT JOIN Category C " +
                "ON C._id = O.category_id " +
                "WHERE O.date BETWEEN ? AND ?;";
        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(start.getTimeInMillis()), String.valueOf(end.getTimeInMillis())});
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow("c_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("c_typeCoef")),
                    cursor.getString(cursor.getColumnIndexOrThrow("c_name"))
            );
            categories.add(category);
        }
        cursor.close();
        close();
        
        return categories;
    }
    
    private List<Operation> getCategoryOperationsForDate(Category category, Date dateStart, Date dateEnd) {
        List<Operation> operations = new ArrayList<>();
    
        Calendar start = Calendar.getInstance();
        start.setTime(dateStart);
        roundCalendarToStart(start);
    
        Calendar end = Calendar.getInstance();
        end.setTime(dateEnd);
        roundCalendarToEnd(end);
    
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " +
                "O._id AS o_id, " +
                "O.date AS o_date, " +
                "O.comment AS o_comment, " +
                "O.sum AS o_sum, " +
                "O.typeCoef AS o_typeCoef " +
                "FROM Operation O " +
                "WHERE O.category_id = ? AND O.date BETWEEN ? AND ?;";
        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(category.getId()), String.valueOf(start.getTimeInMillis()), String.valueOf(end.getTimeInMillis())});
        while (cursor.moveToNext()) {
            Operation operation = new Operation(
                    cursor.getInt(cursor.getColumnIndexOrThrow("o_id")),
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow("o_date"))),
                    category,
                    cursor.getString(cursor.getColumnIndexOrThrow("o_comment")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("o_sum")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("o_typeCoef"))
            );
            operations.add(operation);
        }
        cursor.close();
        close();
        
        return operations;
    }
    
    public double getInBalance(Date date) {
        double balance = 0D;
        
        Calendar start = Calendar.getInstance();
        start.setTime(date);
        roundCalendarToStart(start);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " +
                "SUM(O.sum * C.typeCoef) AS balance " +
                "FROM Operation AS O " +
                "LEFT JOIN Category AS C " +
                "ON C._id = O.category_id " +
                "WHERE O.date < ?;";
        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(start.getTimeInMillis())});
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
        }
        cursor.close();
        close();
        
        return balance;
    }
    
    public double getIncomeTurnover(Date dateStart, Date dateEnd) {
        double turnover = 0D;
        
        Calendar start = Calendar.getInstance();
        start.setTime(dateStart);
        roundCalendarToStart(start);
    
        Calendar end = Calendar.getInstance();
        end.setTime(dateEnd);
        roundCalendarToEnd(end);
    
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " +
                "SUM(O.sum) AS turnover " +
                "FROM Operation AS O " +
                "LEFT JOIN Category AS C " +
                "ON C._id = O.category_id " +
                "WHERE O.date BETWEEN ? AND ? " +
                "AND C.typeCoef > 0;";
        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(start.getTimeInMillis()), String.valueOf(end.getTimeInMillis())});
        if (cursor.moveToFirst()) {
            turnover = cursor.getDouble(cursor.getColumnIndexOrThrow("turnover"));
        }
        cursor.close();
        close();
        
        return turnover;
    }
    
    public double getExpenseTurnover(Date dateStart, Date dateEnd) {
        double turnover = 0D;
        
        Calendar start = Calendar.getInstance();
        start.setTime(dateStart);
        roundCalendarToStart(start);
        
        Calendar end = Calendar.getInstance();
        end.setTime(dateEnd);
        roundCalendarToEnd(end);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " +
                "SUM(O.sum) AS turnover " +
                "FROM Operation AS O " +
                "LEFT JOIN Category AS C " +
                "ON C._id = O.category_id " +
                "WHERE O.date BETWEEN ? AND ? " +
                "AND C.typeCoef < 0;";
        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(start.getTimeInMillis()), String.valueOf(end.getTimeInMillis())});
        if (cursor.moveToFirst()) {
            turnover = cursor.getDouble(cursor.getColumnIndexOrThrow("turnover"));
        }
        cursor.close();
        close();
        
        return turnover;
    }
    
    public double getOutBalance(Date date) {
        double balance = 0D;
        
        Calendar end = Calendar.getInstance();
        end.setTime(date);
        roundCalendarToEnd(end);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " +
                "SUM(O.sum * C.typeCoef) AS balance " +
                "FROM Operation AS O " +
                "LEFT JOIN Category AS C " +
                "ON C._id = O.category_id " +
                "WHERE O.date <= ?;";
        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(end.getTimeInMillis())});
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
        }
        cursor.close();
        close();
        
        return balance;
    }
    
    public void updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("typeCoef",category.getTypeCoef());
        cv.put("name",category.getName());
        db.update("Category",cv,"_id = ?",new String[]{String.valueOf(category.getId())});
        close();
    }
    
    public List<ExpListGroup> getExpListGroups(Date mDateStart, Date mDateEnd) {
        List<ExpListGroup> expListGroups = new ArrayList<>();
        
        List<Category> categories = getCategoriesForDate(mDateStart, mDateEnd);
        for (Category category : categories) {
            List<Operation> operations = getCategoryOperationsForDate(category, mDateStart, mDateEnd);
            ExpListGroup group = new ExpListGroup(category,operations);
            expListGroups.add(group);
        }
        
        return expListGroups;
    }
    
    public void deleteOperation(Operation operation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Operation","_id = ?",new String[]{String.valueOf(operation.getId())});
        close();
    }
    
    public void updateOperation(Operation operation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", operation.getDate().getTime());
        cv.put("category_id", operation.getCategory().getId());
        cv.put("comment", operation.getComment());
        cv.put("sum", operation.getSum());
        cv.put("typeCoef", operation.getCategory().getTypeCoef());
        db.update("Operation",cv,"_id = ?",new String[]{String.valueOf(operation.getId())});
        close();
    }
    
    public Operation getOperation(int id) {
        Operation operation = new Operation();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(true,"Operation",null,"_id = ?",new String[]{String.valueOf(id)},null,null,null,"1");
        if (cursor.moveToFirst()) {
            Category category = getCategory(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
            operation = new Operation(
                    cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow("date"))),
                    category,
                    cursor.getString(cursor.getColumnIndexOrThrow("comment")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("sum")),
                    category.getTypeCoef()
            );
        }
        cursor.close();
        close();
        
        return operation;
    }
}
