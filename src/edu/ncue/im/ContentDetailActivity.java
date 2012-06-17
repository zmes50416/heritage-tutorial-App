package edu.ncue.im;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import edu.ncue.test.jls.*;
public class ContentDetailActivity extends Activity {
	protected ImageView im;
	protected TextView poiTitle;
	protected ScrollView sV;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.content_detail);
		
		HashMap<String, String> poiMap = (HashMap<String, String>)this.getIntent().getSerializableExtra("SinglePOI");
		im = (ImageView)findViewById(R.id.titleImageView);
		im.setImageResource(R.drawable.place_holder1);
		
		poiTitle = (TextView)findViewById(R.id.descripe_of_POI);
		poiTitle.setText(poiMap.get("POI_title"));
		
		sV = (ScrollView)findViewById(R.id.contentScrollView);
		TextView poiContent = new TextView(this);
		poiContent.setText(poiMap.get("POI_description"));
		sV.addView(poiContent);
		
		
		
	}
}
