package by.sevenlis.simplewallet.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "sWallet.s3db";
    private static final int VERSION = 1;
    
    DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql;
        
        sql = "CREATE TABLE Operation (_id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, category_id INTEGER, comment TEXT, sum REAL, typeCoef INTEGER);";
        sqLiteDatabase.execSQL(sql);
    
        sql = "CREATE TABLE Category (_id INTEGER PRIMARY KEY AUTOINCREMENT, typeCoef INTEGER, name TEXT);";
        sqLiteDatabase.execSQL(sql);
        
        sql = "INSERT INTO Category (typeCoef, name) VALUES (-1, 'Прочие расходы');";
        sqLiteDatabase.execSQL(sql);
    
        sql = "INSERT INTO Category (typeCoef, name) VALUES (1, 'Прочие доходы');";
        sqLiteDatabase.execSQL(sql);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    
    }
}
