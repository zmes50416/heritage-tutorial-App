package edu.ncue.im;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.List;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.drawable.Drawable;
import com.google.android.maps.*;

import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.location.LocationListener;
import edu.ncue.test.jls.*;

public class MainMapActivity extends MapActivity{//Ä~©ÓmapActivity
    /** Called when the activity is first created. */
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATE = 1;	//meter
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; 	//milesecond
	
	protected LocationManager locator;	
	protected Button gpsButton;
	protected ImageButton searchButton;
	protected ImageButton displayListButton;
	protected MapView mv;
	protected MapController mapController;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);	//³]©wlayout
        
        gpsButton = (Button) findViewById(R.id.retrieve_Location_Button);	//create button&View
        searchButton = (ImageButton) findViewById(R.id.pop_keyboard_Button);
        displayListButton = (ImageButton)findViewById(R.id.display_list_Button);
        
        
        locator = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mv = (MapView) findViewById(R.id.mapview);
        mapController = mv.getController();
        mv.setBuiltInZoomControls(true);
        
        locator.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER, 
        		MINIMUM_DISTANCE_CHANGE_FOR_UPDATE,
        		MINIMUM_TIME_BETWEEN_UPDATE, 
        		new MyLocationListener());
        
        gpsButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
	        		showCurrentLocation();
	        		//Toast.makeText(MainMapActivity.this, "CLICKED", Toast.LENGTH_SHORT).show();
			}
        
        });
        searchButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		
        		final EditText input = new EditText(v.getContext());
                final AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
        		alert.setView(input);
        		
        		alert.setPositiveButton("Search", new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int which) {
						//use value to search database
						//Editable value = input.getText();
					}
        		});
        		alert.setNegativeButton("Cancle",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
        		
        		alert.show();
        		//keyboard.toggleSoftInput(softInputAnchor, 0)
        	}
        });
        displayListButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent intent = new Intent();
        		intent.setClass(getApplicationContext(), BrowseContentActivity.class);
        		startActivity(intent);
        	}
        });
        //List<Overlay> mapOverlays = mv.getOverlays();
        //Drawable drawable = this.getResources().getDrawable(R.drawable.map_arrow);
        //HelloItemizedOverlay itemizedOverlay = new HelloItemizedOverlay(drawable,this);
        //GeoPoint point = new GeoPoint(30443769,-91158458);
        //OverlayItem overlayitem = new OverlayItem(point, "Laisses Rouler!","I'm in Louisiana");
        
        //GeoPoint p2 = new GeoPoint(17385812, 78480667);
        //OverlayItem overlayitem2 = new OverlayItem(p2, "Namashkaar!","Im in Hyderabad");
        
        //itemizedOverlay.addOverlay(overlayitem);
        //itemizedOverlay.addOverlay(overlayitem2);
        
        //mapOverlays.add(itemizedOverlay);
    }
    
    @Override
    protected boolean isRouteDisplayed()
    {
    	return false;
    }
    
    protected void showCurrentLocation(){
    	Location location = locator.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	
    	if (location != null)
    	{
    		
    		String message = String.format("CurrentLocation \n Longitude: %1$s \n Latitude: %2$s ", location.getLongitude(),location.getLatitude());
    		Toast.makeText(MainMapActivity.this, message, Toast.LENGTH_LONG).show();
    		
    		//retrieve where device is and zoom to that position
    		GeoPoint currentPoint = new GeoPoint((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
    		mapController.animateTo(currentPoint);
    		mapController.setZoom(16);
    		if(!mv.getOverlays().isEmpty())
    			mv.getOverlays().clear();
    		OverlayItem overlayItem = new OverlayItem(currentPoint, "Current Position","");
    		HelloItemizedOverlay itemizedOverlay = new HelloItemizedOverlay(this.getResources().getDrawable(R.drawable.map_arrow), this);
    		itemizedOverlay.addOverlay(overlayItem);
    		mv.getOverlays().add(itemizedOverlay);
    		
    		mv.invalidate();
    	}
    	else
    	{
    		Toast.makeText(getApplication(), "Currently cant get the Device's Location.",Toast.LENGTH_LONG).show();
    	}
    }

    //inner Class
    private class MyLocationListener implements LocationListener{

    	public void onLocationChanged(Location location) {
    		String message = String.format("NEW LOCATION Dectected! \n %1$s \n %2$s", location.getLongitude(),location.getLatitude());
    		Toast.makeText(MainMapActivity.this, message, Toast.LENGTH_LONG).show();

    	}

    	public void onProviderDisabled(String provider) {
    		Toast.makeText(MainMapActivity.this,"Provider disabled by the user. GPS turned off",Toast.LENGTH_LONG).show();
		
    	}

    	public void onProviderEnabled(String provider) {
    		Toast.makeText(MainMapActivity.this,"Provider enabled by the user. GPS turned ON",Toast.LENGTH_LONG).show();
		
    	}

    	public void onStatusChanged(String provider, int i, Bundle extras) {
    		Toast.makeText(MainMapActivity.this, "Provider StatusChanged", Toast.LENGTH_LONG).show();
		
    	}
	
    }
}