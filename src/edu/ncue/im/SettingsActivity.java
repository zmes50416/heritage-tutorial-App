package edu.ncue.im;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.facebook.android.*;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.*;
import edu.ncue.test.jls.R;

public class SettingsActivity extends Activity {

	public static final String APP_ID = "273315202770124";
	String[] permissions = { "publish_stream", "offline_access"};
	private String name;
	private String imageURL;
	TextView userName;
	ImageView userPicView;
	public static Facebook facebook = new Facebook(APP_ID);
	private SharedPreferences mPrefs;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			Intent intent = new Intent(this, MainMapActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.settings);
		 
		 ActionBar actionBar = getActionBar();
		 actionBar.setDisplayHomeAsUpEnabled(true);
		 
		 Spinner spinner = (Spinner) findViewById(R.id.distance_spinner);
		 //建立一個ArrayAdapter物件，並放置下拉選單的內容
		 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new String[]{"近","中","遠"});
		 //設定下拉選單的樣式
		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 spinner.setAdapter(adapter);
		 //設定項目被選取之後的動作
		 spinner.setSelection(1);
		 spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
		 public void onItemSelected(AdapterView adapterView, View view, int position, long id){
			 				switch(adapterView.getSelectedItemPosition()){
			 				case 0:
			 					MainMapActivity.DISTANCE_TO_SEARCH = 300;
			 					break;
			 				case 1:
			 					MainMapActivity.DISTANCE_TO_SEARCH = 1000;
			 					break;
			 				case 2:
			 					MainMapActivity.DISTANCE_TO_SEARCH = 3000;
			 					break;
			 				}
		 }

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		 });
		 
		 userName = (TextView)findViewById(R.id.userNameTextView);
		 Button logoutButton = (Button)findViewById(R.id.facebookLogOutButton);
		 userPicView = (ImageView)findViewById(R.id.userPictureImageView);
		 
		 mPrefs = this.getPreferences(MODE_PRIVATE);
		 String access_token = mPrefs.getString("access_token", null);
		 long expires = mPrefs.getLong("access_expires", 0);
		 // still have bug when u log out fb app and we still holding the old token.
		 if(access_token != null){
			 facebook.setAccessToken(access_token);
		 }
		 if(expires != 0){
			 facebook.setAccessExpires(expires);
		 }
		 
		if (!facebook.isSessionValid()) {
			facebook.authorize(this,
					permissions,
					new DialogListener() {

						@Override
						public void onComplete(Bundle values) {
							SharedPreferences.Editor editor = mPrefs.edit();
		                    editor.putString("access_token", facebook.getAccessToken());
		                    editor.putLong("access_expires", facebook.getAccessExpires());
		                    editor.commit();
						}

						@Override
						public void onFacebookError(FacebookError e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onError(DialogError e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onCancel() {
							// TODO Auto-generated method stub

						}

					});
		}
		else
			Toast.makeText(getApplication(),"FB profile:"+ userName +" 已登入",Toast.LENGTH_LONG).show();
		
		AsyncFacebookRunner as = new AsyncFacebookRunner(facebook);
		Bundle params = new Bundle();
		params.putString("fields","picture.type(large),name");
        as.request("me", params, new UserReuestListener());
		logoutButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				
			}
			
		});
		
		 
	}
	 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	Bitmap bitmap;
	class UserReuestListener implements RequestListener{

		@Override
		public void onComplete(String response,
				Object state) {
			JSONObject jsonObject;
			try{
				jsonObject = new JSONObject(response);
				Log.d("json", "FBJSON:"+response);
				name = jsonObject.getString("name");
				imageURL = new JSONObject(jsonObject.getString("picture")).getJSONObject("data").getString("url");
				
				try {
					bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
					
                } catch (Exception e) {
					e.printStackTrace();
				}
				
				SettingsActivity.this.runOnUiThread(new Runnable() {
	                public void run(){
	                    userName.setText(name);
	                    userPicView.setImageBitmap(bitmap);
	                }
	            });

				
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			
		}

		@Override
		public void onIOException(IOException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFileNotFoundException(
				FileNotFoundException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMalformedURLException(
				MalformedURLException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e,
				Object state) {
			// TODO Auto-generated method stub
			
		}
 	
 }
	
}