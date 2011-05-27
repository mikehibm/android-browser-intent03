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
		public long id = 0;
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
				loadPropertiesFromCursor(hist, cursor);
				array.add(hist);
			}
			cursor.close();
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
		
		return array;
	}
	
	private static void loadPropertiesFromCursor(HistoryItem hist, Cursor cursor){
		hist.id = cursor.getInt(cursor.getColumnIndex("_id"));
		hist.url = cursor.getString(cursor.getColumnIndex("url"));
		hist.title = cursor.getString(cursor.getColumnIndex("title"));
		if (hist.title == null) hist.title = "";
	}
	
	public static HistoryItem save(String url, String title) throws Exception {
		
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		HistoryItem hist = null;
		try {
			//同じURLが既にあれば更新、無ければ挿入する。
			hist = insertOrUpdateHistory(db, url, title);
			
		} catch (Exception e) {
			throw e;
		} finally{
			db.close();
		}
		return hist;
	}
	
	//urlで検索してあれば更新、無ければ挿入する。
	private static HistoryItem insertOrUpdateHistory(SQLiteDatabase db, String url, String title) {
		String[] columns = {"_id", "url", "title"};
		String where = "url = ?";
		String[] args = { url };
		
		ContentValues values = new ContentValues();
		values.put("url", url);
		values.put("title", title);

		HistoryItem existing_item = null;
		
		Cursor cursor = db.query(TBL_HISTORY, columns, where, args, null, null, null);
		if (cursor.moveToNext()){
			existing_item = new HistoryItem();
			loadPropertiesFromCursor(existing_item, cursor);
		}
		cursor.close();

		if (existing_item != null){
			if (title != null && !"".equals(title)){
				//オフライン時はtitleとurlに同じ文字列がセットされて呼ばれるが、その場合は既存のレコードのtitleを上書きしない。
				if (!title.equals(url) || existing_item.title == null){
					//同じURLが既に保存されていれば更新する。
					db.update(TBL_HISTORY, values, "url = ?", args );
					existing_item.title = title;
				}
			}
		} else {
			//無ければ挿入する。
			long newid = db.insert(TBL_HISTORY, null, values);
			
			existing_item = new HistoryItem();
			existing_item.id = newid;
			existing_item.url = url;
			existing_item.title = title;
		}
		
		return existing_item;
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
