package org.g_okuyama.log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final int DB_VERSION = 1;
    //type: wine=red or white, cocktail=base, whiskey=malt corn etc.
    private static final String CREATE_TABLE_SQL = 
        "create table logtable"
        + "(rowid integer primary key autoincrement,"
        + "category text not null,"
        + "name text not null,"
        + "year text not null,"
        + "type text not null,"
        + "area text not null,"
        + "image text not null,"
        + "date text not null,"
        + "place text not null,"
        + "price text not null,"
        + "evaluate text not null,"
        + "comment text not null)";

    public DatabaseHelper(Context context) {
        super(context, "logdb", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
    }

}
