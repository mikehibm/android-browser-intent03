package com.example.intent03;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class IntentReceiveActivity extends Activity {

	private static ArrayAdapter<String> adapter;			
	private String selected_url = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        if (adapter == null){
        	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        }
        
    	ListView list = (ListView)findViewById(R.id.list);
    	list.setAdapter(adapter);

		//リストの項目がタップされた時に開くダイアログを準備。
    	String[] str_items = { getString(R.string.mnu_browser) , 
    							  getString(R.string.mnu_send), 
    							  getString(R.string.mnu_delete)};
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
	   		.setIcon(R.drawable.icon)
	   		.setTitle(getString(R.string.mnu_select))
	   		.setItems(str_items, 
	   			new DialogInterface.OnClickListener(){
	   				//ダイアログの項目が選択された時の処理。
	   				public void onClick(DialogInterface dialog, int which) {
	   					switch (which){
	   					case 0:
		   					openBrowser(selected_url);
		   					break;
	   					case 1:
	   						startEmailActivity(selected_url);
	   						break;
	   					case 2:
	   						deleteUrl(selected_url);
	   						break;
	   					default:
	   						break;
	   					}
	   				}
	   			}
	   		);
    	
		//リストの項目がタップされた時の処理
    	list.setOnItemClickListener(
    		new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ListView listview = (ListView)parent;
					selected_url = (String)listview.getItemAtPosition(position);
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
			e.printStackTrace();
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
    		//URLを取得。
			String url = intent.getDataString();
			
			//標準ブラウザで開く
			intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
			startActivity(intent);

			//ページのタイトルを取得
			String title = HttpUtil.getTitle(url);
			Log.d("URLHistory", "title=" + title);

			try {
				//データベースに保存。
				HistoryDb.save(url, title);
				
			} catch (Exception e) {
				e.printStackTrace();
		    	showErrorDialog(e);
			}
    	}
	}
	
	//データベースから全件取得してListViewに表示する。
	private void showList() {
		
		try {
			//DBから全件取得。
 			ArrayList<HistoryDb> array = HistoryDb.selectAll();

			//ListViewに表示。
			adapter.clear();
			for (HistoryDb hist : array) {
				adapter.add(hist.title + " (" + hist.url + ")");
			}

			//TextViewに件数（または初期メッセージ）を表示
			TextView txt = (TextView)findViewById(R.id.txtCount);
	    	if (adapter.getCount() > 0){
	    		txt.setText("Count: " + adapter.getCount());
	    	} else {
	    		txt.setText(R.string.initial_msg);
	    	}
		
		} catch (Exception e) {
			e.printStackTrace();
	    	showErrorDialog(e);
		}
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
		for (int i = 0; i < adapter.getCount() ; i++) {
			msg += adapter.getItem(i) + "\n\n";
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