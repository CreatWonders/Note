package com.example.notepads;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesDBHelper extends SQLiteOpenHelper{
    //数据库操作使用了SQLiteOpenHelper，此类是由SQLiteOpenHelper派生的类
    private final static String DATABASE_NAME = "notesDB";//数据库名字
    private final static int DATABASE_VERSION = 1;//数据库版本
    //建表SQL语句编写（在此就能看出合约类的作用防止耦合性的重要）
    private final static String SQL_CREATE_DATABASE = "CREATE TABLE " + Notes.Note.TABLE_NAME + " (" +
            Notes.Note._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Notes.Note.COLUMN_NAME_TITLE + " TEXT" + "," +
            Notes.Note.COLUMN_NAME_CONTEXT + " TEXT" + ","
            + Notes.Note.COLUMN_NAME_TIME + " TEXT" + " )";
    //删除表的SQL语句
    private final static String SQL_DELETE_DATABASE = "DROP TABLE IF EXISTS " +
            Notes.Note.TABLE_NAME;

    public NotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DATABASE);
    }//建立数据库

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //当数据库升级时被调用，首先删除旧表再建立一个新的表
        db.execSQL(SQL_DELETE_DATABASE);
        onCreate(db);
    }
}
