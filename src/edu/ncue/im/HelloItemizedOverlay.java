package edu.ncue.im;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

	
	
}
