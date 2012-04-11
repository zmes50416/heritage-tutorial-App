package edu.ncue.im;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import edu.ncue.test.jls.*;
public class ContentDetailActivity extends Activity {
	protected ImageView im;
	protected TextView tv;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.content_detail);
		im = (ImageView)findViewById(R.id.titleImageView);
		im.setImageResource(R.drawable.sample_image);
		
		tv = (TextView)findViewById(R.id.descripe_of_POI);
		tv.setText("Sample");
	}
}
