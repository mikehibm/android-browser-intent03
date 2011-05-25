package com.example.intent03;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
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
		
		TextView url = (TextView)view.findViewById(R.id.txtListUrl);
		url.setText(item.url);

		FrameLayout framelayout = (FrameLayout)view.findViewById(R.id.frmLayout);
		
		TextView title = (TextView)framelayout.findViewById(R.id.txtListTitle);
		title.setText(item.title);

		ProgressBar prgBar = (ProgressBar)framelayout.findViewById(R.id.prgBar);
		if ("".equals(item.title)){
//			url.setBackgroundColor(Color.CYAN);
			prgBar.setVisibility(View.VISIBLE);
		} else {
//			url.setBackgroundColor(Color.TRANSPARENT);
			prgBar.setVisibility(View.INVISIBLE);
		}

		return view;
	}
}
