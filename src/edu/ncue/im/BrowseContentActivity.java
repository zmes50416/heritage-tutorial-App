package edu.ncue.im;


import android.app.ListActivity;
import android.os.Bundle;
import android.content.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.view.*;
import edu.ncue.test.jls.*;

import android.widget.SimpleAdapter;
public class BrowseContentActivity extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.contentlist_item,COUNTRIES));
		ListView lv = getListView();
		
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?>parent, View view, int position, long id){
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), ContentDetailActivity.class);
				startActivity(intent);
				Toast.makeText(getApplicationContext(), ((TextView)view).getText(), Toast.LENGTH_SHORT).show();
			}
		});
		
		
		
	}
	static final String[] COUNTRIES = new String[]{
		"SampleA","SampleB","SampleC","SampleD","SampleE","SampleF","SampleG"
	  };
}
