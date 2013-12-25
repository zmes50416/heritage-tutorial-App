package edu.ncue.im;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import edu.ncue.test.jls.*;

public class ContentDetailActivity extends Activity {
	protected ImageView im;
	protected TextView poiTitle;
	protected ScrollView sV;
	protected Bitmap bitmap;
	String test;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		this.setContentView(R.layout.content_detail);
		
		final HashMap<String, String> poiMap = (HashMap<String, String>)this.getIntent().getSerializableExtra("SinglePOI");
		im = (ImageView)findViewById(R.id.titleImageView);
		im.setImageResource(R.drawable.placeholder_history);

		
		if (poiMap.get("PICsURL") != null) {
			new Thread(new Runnable(){

				@Override
				public void run() {
					
					String[] imageURLs = poiMap.get("PICsURL").split(";");
					
					Log.d("dev", imageURLs.toString());
					try {
						bitmap = BitmapFactory.decodeStream((InputStream) new URL(
								imageURLs[0]).getContent());
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					ContentDetailActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							
							if (bitmap != null)
								im.setImageBitmap(bitmap);
						}
					});
				}
				
			}).start();
			
		}
		
		poiTitle = (TextView)findViewById(R.id.descripe_of_POI);
		poiTitle.setText(poiMap.get("POI_title"));
				
		sV = (ScrollView)findViewById(R.id.contentScrollView);
		TextView poiContent = new TextView(this);
		poiContent.setText(poiMap.get("POI_description"));
		sV.addView(poiContent);
		
		
		
	}
}
