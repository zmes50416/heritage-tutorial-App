package edu.ncue.im;

import java.io.IOException;

import edu.ncue.test.jls.R;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NfcPassportActivity extends Activity {
	DataBaseHelper mDatabaseHelper;
	LinearLayout container;
	ImageView mainImageView;
	TextView titleTextView;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nfcpassport_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case R.id.pushIn:
			this.enterPushIn();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfcpassport);
		this.mainImageView = (ImageView) findViewById(R.id.mainPassportImageView);
		this.titleTextView = (TextView) findViewById(R.id.nfcPassportTitle);
		this.container = (LinearLayout)findViewById(R.id.passportPicContainer);
		
		mDatabaseHelper = new DataBaseHelper(this);
		try{
			mDatabaseHelper.createDataBase(); 
		}catch(IOException ioe){
			throw new Error("Unable to create database");
		}
		try{
			mDatabaseHelper.openDataBase();
		}catch(SQLException sqle){
			throw sqle;
		}
		
		Cursor c = mDatabaseHelper.getAll();
		c.moveToFirst();
		do{
				int itemID = c.getInt(c.getColumnIndexOrThrow("_id"));
				String picURL = c.getString(c.getColumnIndexOrThrow("picURL"));
				final String title = c.getString(c.getColumnIndexOrThrow("title"));
				final PassPortImageView mView = new PassPortImageView(this, itemID, picURL);
				mView.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						mainImageView.setImageResource(mView.pic);
						titleTextView.setText(title);
						
					}
					
				});
				this.container.addView(mView);
		}while(c.moveToNext());
	}
	
	public void enterPushIn(){
		startActivity(new Intent().setClass(getApplicationContext(), NfcPushInActivity.class));
	}

	@Override
	public void onPause(){
		super.onPause();
		this.mDatabaseHelper.close();
	}
	
	
	
}
