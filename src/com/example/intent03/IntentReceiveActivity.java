package com.example.intent03;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class IntentReceiveActivity extends Activity implements Runnable {

	private static ListItemAdapter adapter;			
	private HistoryDb.HistoryItem selected_item = null;
	
	private Handler mHandler = new Handler();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        if (adapter == null){
        	ArrayList<HistoryDb.HistoryItem> arraylist = new ArrayList<HistoryDb.HistoryItem>();
        	adapter = new ListItemAdapter(this, 0, arraylist);
        }
        
    	ListView listview = (ListView)findViewById(R.id.list);
    	listview.setAdapter(adapter);

		//リストの項目がタップされた時に開くダイアログを準備。
    	String[] str_items = { getString(R.string.mnu_browser) , 
    							  getString(R.string.mnu_send), 
    							  getString(R.string.mnu_delete)};
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
	   		.setIcon(R.drawable.icon)
	   		.setTitle(getString(R.string.msg_select))
	   		.setItems(str_items, 
	   			new DialogInterface.OnClickListener(){
	   				//ダイアログの項目が選択された時の処理。
	   				public void onClick(DialogInterface dialog, int which) {
	   					switch (which){
	   					case 0:
		   					openBrowser(selected_item.url);
		   					break;
	   					case 1:
	   						String msg = selected_item.title + "\n" + selected_item.url;
	   						startEmailActivity(msg);
	   						break;
	   					case 2:
	   						deleteUrl(selected_item.url);
	   						break;
	   					default:
	   						break;
	   					}
	   				}
	   			}
	   		);
    	
		//リストの項目がタップされた時の処理
    	listview.setOnItemClickListener(
    		new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ListView listview = (ListView)parent;
					selected_item = (HistoryDb.HistoryItem)listview.getItemAtPosition(position);
					dialog.show();
				}
			}
    	);

    	try {
        	//データベースを準備
        	HistoryDb.init(getPackageName());
			
        	//インテントを処理
        	processIntent(getIntent());

    	} catch (Exception e) {
	    	showErrorDialog(e);
		}
    }
	
	@Override
	protected void onResume() {
		super.onResume();

		showList();
	}
	
	private void processIntent(Intent intent) {
    	
    	if (Intent.ACTION_VIEW.equals(intent.getAction()) ){
			try {
				if (isConnected()){
					Log.d("inten04", "describeContents=" + intent.describeContents());
//					Log.d("inten04", "Type=" + intent.getType());
//					if (intent.getCategories() != null){
//						Log.d("inten04", "Categories().size=" + intent.getCategories().size());
//					} else {
//						Log.d("inten04", "Categories()=null");
//					}
//					Log.d("inten04", "toUri(0)=" + intent.toUri(0));
//					Log.d("inten04", "Flags=" + intent.getFlags());

					if (Pref.getOpenBrowser(this)){
						//標準ブラウザで開く
						intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
						startActivity(intent);
					}

					//データベースに保存。
					String url = intent.getDataString();
					HistoryDb.save(url, null);

					//別スレッドでタイトルの取得処理を開始。
					Thread thread = new Thread(this);
					thread.start();
				}
				
			} catch (Exception e) {
		    	showErrorDialog(e);
			}
    	}
	}
	
	//別スレッドで処理する
	@Override
	public void run() {
		Intent intent = getIntent();
		final String url = intent.getDataString();

		//HTTP通信を実行してページのタイトルを取得
		final String title = HttpUtil.getTitle(url, getString(R.string.msg_no_title));

		//処理完了後、ハンドラにUIスレッド側で実行する処理を渡す。
		mHandler.post(new Runnable(){
			@Override
			public void run(){
				try {
					//ListViewを更新(該当のurlが既にListViewから削除されていた場合はfalseを返す)
					if (updateList(url, title)){
						//削除されていなければデータベースを更新。
						HistoryDb.save(url, title);
					}
				} catch (Exception e) {
			    	showErrorDialog(e);
				}
			}
		});
	}

	//ネットワークに接続されているかチェックする。
	private boolean isConnected(){
		ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null){
			return false;
		} else {
			if (info.isConnected()){
				return true;
			} else {
				return false;
			}
		}
	}
	
	//データベースから全件取得してListViewに表示する。
	private void showList() {
		
		try {
			//DBから全件取得。
 			ArrayList<HistoryDb.HistoryItem> array = HistoryDb.selectAll();

			//ListViewに表示。
			adapter.setNotifyOnChange(false);				//adapterと画面の連動を一旦中断(速度を上げる為)
			adapter.clear();
			for (HistoryDb.HistoryItem hist : array) {
				adapter.add(hist);
			}
			adapter.notifyDataSetChanged();					//adapterと画面の連動を再開

			//TextViewに件数（または初期メッセージ）を表示
			TextView txt = (TextView)findViewById(R.id.txtCount);
	    	if (adapter.getCount() > 0){
	    		txt.setText("Count: " + adapter.getCount());
	    	} else {
	    		txt.setText(R.string.initial_msg);
	    	}
		
		} catch (Exception e) {
	    	showErrorDialog(e);
		}
	}
	
	//ListViewのタイトルを更新する。
	private boolean updateList(String url, String title){
		for (int i = 0; i < adapter.getCount(); i++) {
			HistoryDb.HistoryItem item = adapter.getItem(i);
			if (url.equals(item.url)){
				item.title = title;
				adapter.notifyDataSetChanged();
				return true;
			}
		}
		return false;
	}

	private void deleteUrl(String selectedUrl) {
		try {
			HistoryDb.delete(selectedUrl);
		} catch (Exception e) {
			e.printStackTrace();
	    	showErrorDialog(e);
		}
		showList();
	}

	//データベースから全レコードを削除
	private void clearList(){
		try {
			HistoryDb.deleteAll();
		} catch (Exception e) {
			e.printStackTrace();
	    	showErrorDialog(e);
		}
		showList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean b = (adapter != null && adapter.getCount() >0);
		MenuItem item = menu.findItem(R.id.mnuSend);
		item.setEnabled(b);

		item = menu.findItem(R.id.mnuClear);
		item.setEnabled(b);

		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.mnuExit:
				finish();
				break;
			case R.id.mnuSend:
				sendEmailAll();
				break;
			case R.id.mnuClear:
				clearList();
				break;
			case R.id.mnuPref:
				openPref();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//設定画面を開く
	private void openPref() {
		Intent intent = new Intent(this, Pref.class); 
        startActivity(intent);
	}
	
	//ブラウザを開く。
	private void openBrowser(String url){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(url));
		startActivity(intent);
    }

	//Eメールを一括送信
	private void sendEmailAll(){
		String msg = "";
		HistoryDb.HistoryItem item;
		for (int i = 0; i < adapter.getCount() ; i++) {
			item = (HistoryDb.HistoryItem)adapter.getItem(i);
			msg += item.title + "\n" + item.url + "\n\n";
		}
		startEmailActivity(msg);
	}
	
	//SENDTOインテントを発行してEmail作成画面を呼び出す。
	private void startEmailActivity(String msg){
		try {
			String to_addr = Pref.getToAddr1(this); 
			String prefix = Pref.getPrefix(this); 
			String footer = Pref.getFooter(this); 
				
			ValidateBeforeSend(to_addr);

			String subject = prefix; 
			String message = msg + "\n\n" + footer;
				
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:" + to_addr));
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(Intent.EXTRA_TEXT,  message );
				
			startActivity(intent);
		} 
	    catch (Exception e) {
	    	e.printStackTrace();
	    	showErrorDialog(e);
	    }
	}
	
	//設定内容をチェックする。
	private void ValidateBeforeSend(String to_addr) throws Exception {
		if (to_addr == null || "".equals(to_addr)){
			throw new Exception(getString(R.string.msg_invalid_to_addr));
		}
	}

	//例外の内容をダイアログで表示
	private void showErrorDialog(Exception e){
    	new AlertDialog.Builder(this)
			.setMessage(e.getMessage())
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

}