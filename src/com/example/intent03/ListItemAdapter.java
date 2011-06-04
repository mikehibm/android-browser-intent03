package com.example.intent03;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.TextUtils;


public class ListItemAdapter extends ArrayAdapter<HistoryDb.HistoryItem> {
	
	private LayoutInflater mInflater;
	
	public ListItemAdapter(Context context, int rid, List<HistoryDb.HistoryItem> list){
		super(context, rid, list);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		HistoryDb.HistoryItem item = (HistoryDb.HistoryItem)getItem(position);
		
		View view = mInflater.inflate(R.layout.list_item, null);
		
		TextView txtListUrl = (TextView)view.findViewById(R.id.txtListUrl);
		txtListUrl.setText(item.url);

		FrameLayout framelayout = (FrameLayout)view.findViewById(R.id.frmLayout);
		TextView txtListTitle = (TextView)framelayout.findViewById(R.id.txtListTitle);
		ProgressBar prgBar = (ProgressBar)framelayout.findViewById(R.id.prgBar);
		
		if (TextUtils.isEmpty(item.title)){
			txtListTitle.setText(getContext().getString(R.string.msg_initial_title));
			txtListTitle.setPadding(30, 0, 0, 0);
			prgBar.setVisibility(View.VISIBLE);
		} else {
			txtListTitle.setText(item.title);
			prgBar.setVisibility(View.INVISIBLE);
		}

		return view;
	}
}
