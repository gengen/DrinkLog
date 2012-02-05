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
        + "category text not null,"/*1*/
        + "name text not null,"/*2*/
        + "year text not null,"/*3*/
        + "type text not null,"/*4*/
        + "area text not null,"/*5*/
        + "image text not null,"/*6*/
        + "date text not null,"/*7*/
        + "place text not null,"/*8*/
        + "price text not null,"/*9*/
        + "evaluate text not null,"/*10*/
        + "comment text not null)";/*11*/

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
