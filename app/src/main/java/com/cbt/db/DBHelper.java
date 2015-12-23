package com.cbt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by caobotao on 15/12/21.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "download.db";
    private static final int VERSION = 1;
    private static final String SQL_CREATR_TABLE =
                                "create table thread_info(" +
                                "_id integer primary key autoincrement," +
                                "thread_id integer," +
                                "url text," +
                                "start integer," +
                                "end integer," +
                                "finished integer)";
    private static final String SQL_DROP_TABLE =
                                "drop table if exists thread_info";

    public DBHelper(Context context) {
        super(context, DB_NAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        db.execSQL(SQL_CREATR_TABLE);
    }
}
