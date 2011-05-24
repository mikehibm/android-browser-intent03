package com.example.intent03;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ListItemAdapter extends ArrayAdapter<HistoryDb.HistoryItem> {
	
	private LayoutInflater mInflater;
	
	public ListItemAdapter(Context context, int rid, List<HistoryDb.HistoryItem> list){
		super(context, rid, list);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		HistoryDb.HistoryItem item = (HistoryDb.HistoryItem)getItem(position);
		
		View view = mInflater.inflate(R.layout.list_item, null);
		
		TextView title = (TextView)view.findViewById(R.id.txtListTitle);
		title.setText(item.title);
		
		TextView url = (TextView)view.findViewById(R.id.txtListUrl);
		url.setText(item.url);
		
		return view;
	}
}
