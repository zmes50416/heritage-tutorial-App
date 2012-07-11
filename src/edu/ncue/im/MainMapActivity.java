package edu.ncue.im;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
	protected static float DISTANCE_TO_SEARCH = 1000f;				//meter
	
	final static String POI_TAPPED_ACTION = "MainMapActivity.POI_TAPPED_ACTION";
	
	protected LocationManager locator;	
	protected Button gpsButton;
	protected ImageButton searchButton;
	protected ImageButton displayListButton;
	protected MapView mv;
	protected MapController mapController;
	protected static MyLocationListener myLocationListener;
	
	protected SlidingDrawer poiDrawer;
	protected POILoadTask poiLoadTask;
	private ArrayList<Map<String, String>>soilist;
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
        soilist = new ArrayList<Map<String, String>>();
        mv = (MapView) findViewById(R.id.mapview);
        mv.setBuiltInZoomControls(false);	//disable the zoom button
        mapController = mv.getController();
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

						Boolean findFlag = false;
						//Editable value = input.getText();
						Location location = locator.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if(location != null){
							//new foundList to save the result
							ArrayList<Map<String, String>> foundList = new ArrayList<Map<String, String>>();
							for(Map<String, String> m:soilist){
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
            		new POILoadTask().execute(location.getLatitude(),location.getLongitude());
        			bundle.putSerializable("POI", soilist);
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
    		
    		String message = String.format("CurrentLocation \n Longitude: %1$f \n Latitude: %2$f ", location.getLongitude(),location.getLatitude());
    		Toast.makeText(MainMapActivity.this, message, Toast.LENGTH_LONG).show();
    		
    		
    		GeoPoint currentPoint = new GeoPoint((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
    		//retrieve GPS data and zoom to that position
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
    		
    		if (this.myLocationListener.poiLoadTask.getStatus() != AsyncTask.Status.FINISHED)			
    			Toast.makeText(this, "Haven't get data from server yet", Toast.LENGTH_SHORT).show();
    		else if(!soilist.isEmpty()){
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
    
	public ArrayList<Map<String, String>> getList(){
		/*if(soilist == null)
			soilist = poiLoadTask.get();*/
		return soilist;
		
			 
	}
    //inner Class
    private class MyLocationListener implements LocationListener{
    	POILoadTask poiLoadTask;
    	public MyLocationListener(){
    		super();
    		poiLoadTask = new POILoadTask();
    	}
    	public void onLocationChanged(Location location) {
    		String message = String.format("NEW LOCATION Dectected! \n %1$f \n %2$f", location.getLongitude(),location.getLatitude());
    		Toast.makeText(MainMapActivity.this, message, Toast.LENGTH_LONG).show();
    		poiLoadTask.execute(location.getLatitude(),location.getLongitude());
    		try {
				soilist = poiLoadTask.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		//showCurrentLocation();
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
	//Network must need AsyncTask to run on another thread
	class POILoadTask extends AsyncTask<Double, Void, ArrayList<Map<String, String>>>{
		
		
		@Override
		protected ArrayList<Map<String, String>> doInBackground(Double... params) {
			DEHAPIReceiver receiver = new DEHAPIReceiver(params[0],params[1],MainMapActivity.DISTANCE_TO_SEARCH);
			return receiver.getsoilist();
		}
		

		
	}