package edu.ncue.im;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.*;

import com.google.android.maps.*;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.location.LocationListener;
import edu.ncue.test.jls.*;

public class MainMapActivity extends MapActivity{//Ä~©ÓmapActivity
    /** Called when the activity is first created. */
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATE = 1;	//meter
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; 	//milesecond
	private static float DISTANCE_TO_SEARCH = 2f;
	
	final static String POI_TAPPED_ACTION = "MainMapActivity.POI_TAPPED_ACTION";
	
	protected LocationManager locator;	
	protected Button gpsButton;
	protected ImageButton searchButton;
	protected ImageButton displayListButton;
	protected MapView mv;
	protected MapController mapController;
	protected static MyLocationListener myLocationListener;
	
	protected SlidingDrawer poiDrawer;
	protected DEHAPIReceiver receiver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);	//³]©wlayout
        
        
        gpsButton = (Button) findViewById(R.id.retrieve_Location_Button);	//create button&View
        searchButton = (ImageButton) findViewById(R.id.pop_keyboard_Button);
        displayListButton = (ImageButton)findViewById(R.id.display_list_Button);
        poiDrawer = (SlidingDrawer)findViewById(R.id.poiDrawer);
        poiDrawer.setVisibility(View.INVISIBLE);
        locator = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mv = (MapView) findViewById(R.id.mapview);
        mv.setBuiltInZoomControls(false);
        mapController = mv.getController();
        //mv.setBuiltInZoomControls(true);
        myLocationListener = new MyLocationListener();
        
        gpsButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
	        		showCurrentLocation();
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
						Boolean findFlag = false;
						Editable value = input.getText();
						Location location = locator.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if(location != null){
							ArrayList<Map<String, String>> foundList = new ArrayList<Map<String, String>>();
							for(Map<String, String> m:new DEHAPIReceiver(location.getLatitude(),location.getLongitude(),DISTANCE_TO_SEARCH).getsoilist()){
								String name = m.get("POI_title");
								if(name.contains(input.getText())){
									foundList.add(m);
									findFlag = true;
								}		
							}
							if(findFlag == true){
								Intent intent = new Intent();
								Bundle searchBundle = new Bundle();
								searchBundle.putSerializable("POI", foundList);
								intent.putExtras(searchBundle);
								intent.setClass(getApplicationContext(), BrowseContentActivity.class);
								startActivity(intent);
							}
							if(findFlag == false){
								Toast.makeText(getApplicationContext(), "Cant Find Any Point contains "+input.getText(), Toast.LENGTH_SHORT).show();
							}
						}
							
						
						
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
        		Location location = locator.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        		if(location != null){//passing currentGeoData to ListView
        			
            		Bundle bundle = new Bundle();
            		
        			bundle.putSerializable("POI", new DEHAPIReceiver(location.getLatitude(),location.getLongitude(),DISTANCE_TO_SEARCH).getsoilist());
        			intent.putExtras(bundle);
        			//bundle.putDouble("CurrentLongitude", location.getLongitude());
        			//bundle.putDouble("CurrentLatitude", location.getLatitude());
        		}
        		intent.setClass(getApplicationContext(), BrowseContentActivity.class);
        		startActivity(intent);
        		
        	}
        });
        
        
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	Log.d("gps", "paused");
    	locator.removeUpdates(myLocationListener);
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	Log.d("gps", "Resumed");
    	locator.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER, 
        		MINIMUM_DISTANCE_CHANGE_FOR_UPDATE,
        		MINIMUM_TIME_BETWEEN_UPDATE, 
        		myLocationListener);
        		
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
    		if(!mv.getOverlays().isEmpty())//clear the old place first
    			mv.getOverlays().clear();
    		OverlayItem overlayItem = new OverlayItem(currentPoint, "Current Position","");
    		
    		IntentFilter intentFilter = new IntentFilter();
    		intentFilter.addAction(POI_TAPPED_ACTION);
    		this.registerReceiver(new BroadcastReceiver(){
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					String title = (String) arg1.getSerializableExtra("POITitle");
					String snippets = (String) arg1.getSerializableExtra("POISnippet");
					if(poiDrawer.getVisibility()==View.INVISIBLE){
						poiDrawer.setVisibility(View.VISIBLE);
					}
					TextView drawerTitle = (TextView)poiDrawer.findViewById(R.id.drawerTitle);
					TextView poiContent = (TextView) poiDrawer.findViewById(R.id.POIcontent);
					drawerTitle.setText(title);
					poiContent.setText(snippets);
					Log.d("BroadCast", title);
					Log.d("BroadCast", snippets);
				}
    			
    		}, intentFilter);
    		
    		HelloItemizedOverlay itemizedOverlay = new HelloItemizedOverlay(this.getResources().getDrawable(R.drawable.map_arrow), this);
    		itemizedOverlay.addOverlay(overlayItem);
    		mv.getOverlays().add(itemizedOverlay);
    		
    		
    		
    		//receiver test
    		
    		ArrayList<Map<String, String>> soilist = null;
			try {
				soilist = new POILoadTask().execute(location.getLatitude(),location.getLongitude()).get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
    		if(!soilist.isEmpty()){
    			HelloItemizedOverlay poiOverlay = new HelloItemizedOverlay(this.getResources().getDrawable(R.drawable.poi), this);
    			for(Map<String, String> map : soilist){
    				GeoPoint gp;
    				gp = new GeoPoint((int)(Double.parseDouble(map.get("latitude"))*1E6),(int)(Double.parseDouble(map.get("longitude"))*1E6));
    				OverlayItem poi = new OverlayItem(gp, map.get("POI_title"),map.get("POI_description"));
    				poiOverlay.addOverlay(poi);
    			}
    			mv.getOverlays().add(poiOverlay);
    		}else{
    			Toast.makeText(getApplication(),"Can't Find Any POI in distance",Toast.LENGTH_LONG).show();
    		}
    		
    		//receiver test
    		
    		mv.invalidate();
    		
    	}
    	else
    	{
    		Toast.makeText(getApplication(), "Currently cant get the Device's Location.",Toast.LENGTH_LONG).show();
    	}
    	
    }
    public ArrayList<Map<String, String>> getData(double latitude, double longtitude,float distance){
    	receiver = new DEHAPIReceiver(latitude,longtitude,distance);
		ArrayList<Map<String,String>> soilist = receiver.getsoilist();
		return soilist;
    	
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
	class POILoadTask extends AsyncTask<Double, Void, ArrayList<Map<String, String>>>{
		DEHAPIReceiver receiver;
		
		@Override
		protected ArrayList<Map<String, String>> doInBackground(Double... params) {
			// TODO Auto-generated method stub
			receiver = new DEHAPIReceiver(params[0],params[1],0.5f);
			return receiver.getsoilist();
		}
		

		
	}