package edu.ncue.im;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;
import android.view.*;
import edu.ncue.test.jls.*;

public class BrowseContentActivity extends ListActivity {
	
	protected String[] POI_NAME;
	protected ArrayList<Map<String, String>> dataList;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		dataList = (ArrayList<Map<String, String>>) this.getIntent().getSerializableExtra("POI");
		if(dataList != null){
			int i =0;
			POI_NAME = new String[dataList.size()];
			for(Map<String, String> map:dataList){
				POI_NAME[i] = map.get("POI_title");
				i++;
			}
		}else
			POI_NAME = new String[]{"NONE"};
		
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.contentlist_item,POI_NAME));
		ListView lv = getListView();
		
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?>parent, View view, int position, long id){
				Intent intent = new Intent();
				HashMap<String, String> map = (HashMap<String, String>)dataList.get(position);
				Log.d("DetailedContent", map.toString());
				Bundle bundle = new Bundle();
				bundle.putSerializable("SinglePOI", map);
				intent.putExtras(bundle);
				intent.setClass(getApplicationContext(), ContentDetailActivity.class);
				startActivity(intent);
			}
		});
		
	}
}
