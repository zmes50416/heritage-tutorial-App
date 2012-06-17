package edu.ncue.im;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.*;
public class HelloItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	
	private ArrayList<OverlayItem> mOverlay = new ArrayList<OverlayItem>();
	private Context mContext;
	
	public HelloItemizedOverlay( Drawable defaultMarker, Context context)
	{
		super(boundCenterBottom(defaultMarker));
		mContext = context;
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
	public int size() {
		return mOverlay.size();
	}
	
	@Override
	protected boolean onTap(int index)
	{
		OverlayItem item = mOverlay.get(index);
		Bundle bundle = new Bundle();
		bundle.putSerializable("POITitle", item.getTitle());
		bundle.putSerializable("POISnippet", item.getSnippet());
		Intent intent = new Intent();
		intent.setAction(MainMapActivity.POI_TAPPED_ACTION);
		mContext.sendBroadcast(intent.putExtras(bundle));
		
		return true;
	}

	
	
}
