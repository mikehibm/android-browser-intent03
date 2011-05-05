package com.example.intent03;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class IntentReceiveActivity extends Activity {

	//リストに表示する項目を保持する配列。（staticでないと毎回クリアされてしまう。）
	static ArrayAdapter<String> adapter;			
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        if (adapter == null){
        	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        }
        
    	ListView list = (ListView)findViewById(R.id.list);
    	list.setAdapter(adapter);

    	processIntent(getIntent());
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		showCount();
	}
	
	private void processIntent(Intent intent) {
    	
    	if (Intent.ACTION_VIEW.equals(intent.getAction()) ){
    		
			String url = intent.getDataString();
			
    		//追加済みでなければリスト表示用の配列に追加
			if (findUrl(url) < 0){
				adapter.insert(url, 0);				
			}
			
			//標準ブラウザで開く
			intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
			startActivity(intent);
    	}

    	showCount();
	}
	
	private void showCount(){
		//TextViewに件数を表示
		TextView txt = (TextView)findViewById(R.id.txtCount);
    	if (adapter.getCount() > 0){
    		txt.setText("Count: " + adapter.getCount());
    	} else {
    		txt.setText(R.string.initial_msg);
    	}
	}
	
	//URLが既にリストにあるかどうかチェックする。あればその位置、なければ-1を返す。
	private int findUrl(String url){
		for (int i = adapter.getCount()-1; i >=0 ; i--) {
			if (adapter.getItem(i).equals(url) ){
				return i;
			}
		}
		return -1;
	}
	
	private void clearList(){
		adapter.clear();
		showCount();
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
	
	private void sendEmailAll(){
		String url = "";
		for (int i = 0; i < adapter.getCount() ; i++) {
			url += adapter.getItem(i) + "\n\n";
		}
		sendEmail(url);
	}
	
	private void openPref() {
		Intent intent = new Intent(this, Pref.class); 
        startActivity(intent);
	}
	
	private void sendEmail(String msg){
		try {
			String to_addr = Pref.getToAddr1(this); 
			String prefix = Pref.getPrefix(this); 
			String footer = Pref.getFooter(this); 
				
			ValidateBeforeSend(to_addr, prefix, footer);

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
				
	    	new AlertDialog.Builder(this)
 				.setMessage(e.getMessage())
 				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	    }
	}
	
	// Preference設定をValidateする。
	private void ValidateBeforeSend(String to_addr, String prefix, String footer) throws Exception {
		if (to_addr == null || "".equals(to_addr)){
			throw new Exception(getString(R.string.msg_invalid_to_addr));
		}
	}
	
}