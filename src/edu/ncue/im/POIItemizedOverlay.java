package edu.ncue.im;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.*;

import edu.ncue.test.jls.R;
public class POIItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	
	private ArrayList<OverlayItem> mOverlay = new ArrayList<OverlayItem>();
	private MainMapActivity mContext;
	private Drawable marker;
	public POIItemizedOverlay( Drawable defaultMarker, Context context)
	{
		super(boundCenterBottom(defaultMarker));
		mContext = (MainMapActivity) context;
		marker = defaultMarker;
	}

	public void addOverlay(OverlayItem overlay)
	{
		mOverlay.add(overlay);
		populate();
	}
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlay.get(i);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		Projection projection = mapView.getProjection(); 
		for (int index = size() - 1; index >= 0; index--) {
			OverlayItem overLayItem = getItem(index); 

			String title = overLayItem.getTitle();
			Point point = projection.toPixels(overLayItem.getPoint(), null); 

			Paint paintCircle = new Paint();
			paintCircle.setColor(Color.RED);
			canvas.drawCircle(point.x, point.y, 5, paintCircle);

			Paint paintText = new Paint();
			paintText.setColor(Color.BLACK);
			paintText.setTextSize(15);
			canvas.drawText(title, point.x, point.y - 25, paintText); 

		}

		super.draw(canvas, mapView, shadow);
		boundCenterBottom(marker);
	}
	
	@Override
	public int size() {
		return mOverlay.size();
	}
	
	@Override
	protected boolean onTap(int index)
	{
		OverlayItem item = mOverlay.get(index);
		
	    MapView.LayoutParams geoLP = (MapView.LayoutParams) mContext.popView.getLayoutParams();  
        geoLP.point = item.getPoint();  
        mContext.mv.updateViewLayout(mContext.popView, geoLP);  
        mContext.popView.setVisibility(View.VISIBLE);  
        TextView title = (TextView) mContext.findViewById(R.id.map_bubbleTitle);  
        TextView text = (TextView) mContext.findViewById(R.id.map_bubbleText);  
        title.setText(item.getTitle());
        if(item.getSnippet().length()>=25)
        	text.setText(item.getSnippet().substring(0, 23)+"...");  
        else 
        	text.setText(item.getSnippet());
        ImageView imageView = (ImageView) mContext.findViewById(R.id.map_bubbleImage);  
        imageView.setOnClickListener(new View.OnClickListener(){  
            @Override  
            public void onClick(View v) {  
                  
                mContext.popView.setVisibility(View.GONE);  
            }  
              
              
        });  
        
		/*
		Bundle bundle = new Bundle();
		bundle.putSerializable("POITitle", item.getTitle());
		bundle.putSerializable("POISnippet", item.getSnippet());
		Intent intent = new Intent();
		intent.setAction(MainMapActivity.POI_TAPPED_ACTION);
		mContext.sendBroadcast(intent.putExtras(bundle));
		*/
		return true;
	}
	
	public void clear(){
		mOverlay.clear();
		this.populate();
	}
	
}
