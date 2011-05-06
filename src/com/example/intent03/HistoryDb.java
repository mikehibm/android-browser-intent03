package com.example.intent03;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDb   {

	private final static String DBNAME = "mydata.db";
	private final static String TBL_HISTORY = "history";
	
	public static String DbPath = "";
	
	public static void init(String pkgName){
		DbPath = "/data/data/" + pkgName + "/" + DBNAME;
	}

	public static void select(){
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		
		try {
			//テーブルが無ければ作成する。
			createTable(db);
			
			//SELECTを実行。
			
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally{
			db.close();
		}
	}
	
	public static void save(String url, String title) {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		
		try {
			//テーブルが無ければ作成する。
			createTable(db);
			
			//同じURLが既に無ければ挿入する。既にあれば更新する。
			insertOrUpdateHistory(db, url, title);
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally{
			db.close();
		}
	}
	
	public static void delete(String url) {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		
		try {
			String[] args = { url };
			db.delete(TBL_HISTORY, "url = @1", args);
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally{
			db.close();
		}
	}

	public static void deleteAll() {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DbPath, null);
		
		try {
			String sql = "DELETE " + TBL_HISTORY;
			db.execSQL(sql);
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally{
			db.close();
		}
	}

	private static void insertOrUpdateHistory(SQLiteDatabase db, String url, String title) {
		
		//同じURLが既に保存されているかどうかチェック
		
		
		ContentValues values = new ContentValues();
		values.put("url", url);
		values.put("title", title);
		db.insert(TBL_HISTORY, null, values);
	}

	//テーブルが無ければ作成する。
	private static void createTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + TBL_HISTORY
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, title TEXT);";
		db.execSQL(sql);
	}



}
