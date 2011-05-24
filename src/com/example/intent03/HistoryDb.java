package com.example.intent03;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HistoryDb   {

	private final static String DBNAME = "mydata.db";
	private final static String TBL_HISTORY = "history";
	
	public static String DbPath = "";
	
	public static class HistoryItem{
		//DBのフィールド
		public int id = 0;
		public String url = "";
		public String title = "";
	}
	
	public static void init(String pkgName) throws Exception{
		DbPath = "/data/data/" + pkgName + "/" + DBNAME;

		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		try {
			//テーブルが無ければ作成する。
			createTable(db);
			
			Log.d(TBL_HISTORY, TBL_HISTORY + " is ready.");
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
	}
	
	//テーブルが無ければ作成する。
	private static void createTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + TBL_HISTORY
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, title TEXT);";
		db.execSQL(sql);

		//urlにインデックスを追加。
		sql = "CREATE INDEX IF NOT EXISTS 'main'.'ix_history_url' ON 'history' ('url' ASC)";
		db.execSQL(sql);
	}

	public static HistoryItem select(String url) throws Exception{
		HistoryItem hist = null;
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		try {
			//urlで検索して合致する1件を取得する。
			String[] columns = {"_id", "url", "title"};
			String where = "url = ?";
			String[] args = { url };
			String having = null;
			String group_by = null;
			String order_by = "_id";
			
			Cursor cursor = db.query(TBL_HISTORY, columns, where, args, group_by, having, order_by);
			while (cursor.moveToNext()){
				hist = new HistoryItem();
				hist.id = cursor.getInt(cursor.getColumnIndex("_id"));
				hist.url = cursor.getString(cursor.getColumnIndex("url"));
				hist.title = cursor.getString(cursor.getColumnIndex("title"));
				
				Log.d(TBL_HISTORY, "_id=" + hist.id + ", url=" + hist.url + ", title=" + hist.title);
			}
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
		
		return hist;
	}
	
	public static ArrayList<HistoryItem> selectAll() throws Exception{
		ArrayList<HistoryItem> array = new ArrayList<HistoryItem>();
		
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		try {
			//全件を取得する。
			String[] columns = {"_id", "url", "title"};
			String where = null;
			String having = null;
			String group_by = null;
			String order_by = "_id DESC";
			
			Cursor cursor = db.query(TBL_HISTORY, columns, where, null, group_by, having, order_by);
			while (cursor.moveToNext()){
				HistoryItem hist = new HistoryItem();
				hist.id = cursor.getInt(cursor.getColumnIndex("_id"));
				hist.url = cursor.getString(cursor.getColumnIndex("url"));
				hist.title = cursor.getString(cursor.getColumnIndex("title"));
				if (hist.title == null) hist.title = "";
				
				array.add(hist);
				
				Log.d(TBL_HISTORY, "_id=" + hist.id + ", url=" + hist.url + ", title=" + hist.title);
			}
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
		
		return array;
	}
	
	public static void save(String url, String title) throws Exception {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		try {
			//同じURLが既にあれば更新する。無ければ挿入する。
			insertOrUpdateHistory(db, url, title);
			
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
	}
	
	private static void insertOrUpdateHistory(SQLiteDatabase db, String url, String title) {
		ContentValues values = new ContentValues();
		values.put("url", url);
		values.put("title", title);

		String[] args = { url };

		//同じURLが既に保存されていれば更新する。
		int n = db.update(TBL_HISTORY, values, "url = ?", args );
		
		if (n == 0){
			//無ければ挿入する。
			db.insert(TBL_HISTORY, null, values);
		}
	}

	public static void delete(String url) throws Exception {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		try {
			String[] args = { url };
			db.delete(TBL_HISTORY, "url = ?", args);
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
	}

	public static void deleteAll() throws Exception {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		try {
			String sql = "DELETE FROM " + TBL_HISTORY;
			db.execSQL(sql);
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
	}

}
