package edu.ncue.im;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.facebook.android.*;
import com.facebook.android.Facebook.*;
import edu.ncue.test.jls.R;

public class SocialLoginActivity extends Activity {

	public static final String APP_ID = "273315202770124";
	String[] permissions = { "publish_stream", "offline_access"};
	
	Facebook facebook = new Facebook(APP_ID);
	private SharedPreferences mPrefs;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.social_login);
		 
		 mPrefs = this.getPreferences(MODE_PRIVATE);
		 String access_token = mPrefs.getString("access_token", null);
		 long expires = mPrefs.getLong("access_expires", 0);
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
		 
	}
	 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
		finish();
	}
	
	
	
}
