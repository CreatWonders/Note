package com.example.notepads;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    NotesDBHelper notesDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //为list注册上下文菜单
        ListView list = (ListView)findViewById(R.id.list);
        registerForContextMenu(list);
        //创建SQLiteOpenHelper对象
        notesDBHelper = new NotesDBHelper(this);
        //在列表显示全部便签
        ArrayList<Map<String,String>> items = getAll();
        setNotesListView(items);
    }

    protected void onDestroy(){
        //关闭数据库
        super.onDestroy();
        notesDBHelper.close();
    }

    //选项菜单，即有查找和新建的上栏菜单
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.action_search:
                SearchDialog();
                return true;
            case R.id.action_new:
                InsertDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //上下文菜单，即长按某个记录跳出是否修改和删除
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo){
        getMenuInflater().inflate(R.menu.contextmenu_wordslistview,menu);
    }
    public boolean onContextItemSelected(MenuItem item){
        TextView textId = null;
        TextView textTitle = null;
        TextView textContext = null;
        TextView textTime = null;
        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;
        switch (item.getItemId()){
            case R.id.action_delete:
                //删除单词
                info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView = info.targetView;
                textId = (TextView)itemView.findViewById(R.id.textViewId);
                if(textId!=null){
                    String strId = textId.getText().toString();
                    DeleteDialog(strId);
                }
                break;
            case R.id.action_update:
                //修改单词
                info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView =info.targetView;
                textId = (TextView)itemView.findViewById(R.id.textViewId);
                textTitle = (TextView)itemView.findViewById(R.id.textViewTitle);
                textContext = (TextView)itemView.findViewById(R.id.textViewContext);
                textTime = (TextView)itemView.findViewById(R.id.textViewTime);
                if(textContext!=null && textId!=null && textTime!=null && textTitle!=null){
                    String strId = textId.getText().toString();
                    String strTitle = textTitle.getText().toString();
                    String strContext = textContext.getText().toString();
                    String strTime = textTime.getText().toString();
                    UpdateDialog(strId,strTitle,strContext,strTime);
                }
                break;
        }
        return true;
    }

    //新建=增，的过程代码
    private void InsertUserSql(String strTitle,String strContext,String strTime){
        String sql = "insert into notes(title,context,time) values(?,?,?)";
        SQLiteDatabase db = notesDBHelper.getWritableDatabase();
        db.execSQL(sql,new String[]{strTitle,strContext,strTime});
    }
    private void Insert(String strTitle,String strContext,String strTime){
        SQLiteDatabase db = notesDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Notes.Note.COLUMN_NAME_TITLE,strTitle);
        values.put(Notes.Note.COLUMN_NAME_CONTEXT,strContext);
        values.put(Notes.Note.COLUMN_NAME_TIME,strTime);
        long newRowId;
        newRowId = db.insert(Notes.Note.TABLE_NAME,null,values);
    }
    private void InsertDialog(){
        final TableLayout tableLayout = (TableLayout)getLayoutInflater().inflate(R.layout.insert,null);
        new AlertDialog.Builder(this).setTitle("新建便签").setView(tableLayout)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strTitle = ((EditText)tableLayout.findViewById(R.id.textTitle)).getText().toString();
                        String strContext = ((EditText)tableLayout.findViewById(R.id.textContext)).getText().toString();
                        String strTime = ((EditText)tableLayout.findViewById(R.id.textTime)).getText().toString();
                        Insert(strTitle,strContext,strTime);
                        ArrayList<Map<String,String>>items = getAll();
                        setNotesListView(items);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    //删除
    private void Delete(String strId){
        SQLiteDatabase db = notesDBHelper.getReadableDatabase();
        String selection = Notes.Note._ID + "=?";
        String[] selectionArgs = {strId};
        db.delete(Notes.Note.TABLE_NAME,selection,selectionArgs);
    }
    private void DeleteUseSql(String strId){
        String sql = "delete from notes where _id='" + strId + "'";
        SQLiteDatabase db = notesDBHelper.getReadableDatabase();
        db.execSQL(sql);
    }

    private void DeleteDialog(final String strId){
        new AlertDialog.Builder(this).setTitle("删除便签").setMessage("是否真的删除？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteUseSql(strId);
                        setNotesListView(getAll());
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    //修改=更新
    private void UpdateUseSql(String strId,String strTitle,String strContext,String strTime){
        SQLiteDatabase db = notesDBHelper.getReadableDatabase();
        String sql = "update notes set title=?,context=?,time=? where _id=?";
        db.execSQL(sql,new String[]{strTitle,strContext,strTime,strId});
    }
    private void Update(String strId,String strTitle,String strContext,String strTime){
        SQLiteDatabase db = notesDBHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Notes.Note.COLUMN_NAME_TITLE,strTitle);
        values.put(Notes.Note.COLUMN_NAME_CONTEXT,strContext);
        values.put(Notes.Note.COLUMN_NAME_TIME,strTime);
        String selection = Notes.Note._ID + "=?";
        String[] selectionArgs = {strId};
        int count = db.update(Notes.Note.TABLE_NAME,values,selection,selectionArgs);
    }
    private void UpdateDialog(final String strId,final String strTitle,final String strContext,final String strTime){
        final TableLayout tableLayout = (TableLayout)getLayoutInflater().inflate(R.layout.insert,null);
        ((EditText)tableLayout.findViewById(R.id.textTitle)).setText(strTitle);
        ((EditText)tableLayout.findViewById(R.id.textContext)).setText(strContext);
        ((EditText)tableLayout.findViewById(R.id.textTime)).setText(strTime);
        new AlertDialog.Builder(this).setTitle("修改便签").setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strNewTitle = ((EditText)tableLayout.findViewById(R.id.textTitle)).getText().toString();
                        String strNewContext = ((EditText)tableLayout.findViewById(R.id.textContext)).getText().toString();
                        String strNewTime = ((EditText)tableLayout.findViewById(R.id.textTime)).getText().toString();
                        Update(strId, strNewTitle, strNewContext, strNewTime);
                        setNotesListView(getAll());
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    //查找
    private ArrayList<Map<String,String>>SearchUseSql(String strNoteSearch){
        SQLiteDatabase db = notesDBHelper.getReadableDatabase();
        String sql = "select * from notes where title like ? order by title desc";
        Cursor c = db.rawQuery(sql,new String[]{"%" + strNoteSearch + "%"});
        return ConvertCursor2List(c);
    }
    private void SearchDialog(){
        final TableLayout tableLayout = (TableLayout)getLayoutInflater().inflate(R.layout.searchterm,null);
        new AlertDialog.Builder(this).setTitle("查找便签").setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String textSearchNote = ((EditText)tableLayout.findViewById(R.id.textSearchNote))
                                .getText().toString();
                        ArrayList<Map<String,String>> items = null;
                        items = SearchUseSql(textSearchNote);
                        if(items.size()>0){
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("result",items);
                            Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }else
                            Toast.makeText(MainActivity.this,"没有找到",Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    private ArrayList<Map<String,String>> getAll(){
        SQLiteDatabase db = notesDBHelper.getReadableDatabase();
        String[] projection = {
                Notes.Note._ID,
                Notes.Note.COLUMN_NAME_TITLE,
                Notes.Note.COLUMN_NAME_CONTEXT,
                Notes.Note.COLUMN_NAME_TIME
        };
        String sortOrder = Notes.Note.COLUMN_NAME_TITLE + " DESC";

        Cursor c = db.query(
                Notes.Note.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        return ConvertCursor2List(c);
    }

    private ArrayList<Map<String,String>> ConvertCursor2List(Cursor cursor){
        ArrayList<Map<String,String>> result = new ArrayList<>();
        if(cursor.getCount()!=0){
        while(cursor.moveToNext()){
            Map<String,String> map = new HashMap<>();
            map.put(Notes.Note._ID,String.valueOf(cursor.getInt(0)));
            map.put(Notes.Note.COLUMN_NAME_TITLE,cursor.getString(1));
            map.put(Notes.Note.COLUMN_NAME_CONTEXT,cursor.getString(2));
            map.put(Notes.Note.COLUMN_NAME_TIME,cursor.getString(3));
            result.add(map);
        }}
        return result;
    }

    //设置适配器，在列表中显示便签
    private void setNotesListView(ArrayList<Map<String,String>> items){
        SimpleAdapter adapter = new SimpleAdapter(this,items,R.layout.item,
                new String[]{Notes.Note._ID, Notes.Note.COLUMN_NAME_TITLE,
                        Notes.Note.COLUMN_NAME_CONTEXT, Notes.Note.COLUMN_NAME_TIME},
                new int[]{R.id.textViewId,R.id.textViewTitle,
                        R.id.textViewContext,R.id.textViewTime});
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
    }
}
