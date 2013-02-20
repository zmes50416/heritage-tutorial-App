package edu.ncue.im;

import java.io.IOException;
import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class PassPortImageView extends ImageView {
	
	int ID;
	int pic;
	String picURL;
	SharedPreferences sharedList;
	public PassPortImageView(Context context, int ID,String picURL) {
		super(context);
		Log.d("nfc", Integer.toString(ID));
		this.ID = ID;
		this.picURL = "edu.ncue.test.jls:drawable/"+picURL;
		sharedList = context.getSharedPreferences("edu.ncue.im.NFCPassport", Context.MODE_PRIVATE);
		this.setLayoutParams(new FrameLayout.LayoutParams(200, 200));
		checkID();
		
	}
	
	//check ID have been recorded or not
	public void checkID(){
		
		pic = this.getResources().getIdentifier("edu.ncue.test.jls:drawable/uncover", null, null);
		
		HashSet<String> a = (HashSet<String>) sharedList.getStringSet("passIDSet", null);
		if(a != null){
			if(a.contains(Integer.toString(this.ID))){
				pic = this.getResources().getIdentifier(picURL, null, null);
				Log.d("nfc", "PicURL:"+picURL+",identifier:"+pic);
			} 
		}
		
			
		this.setImageResource(pic);
	}

}
