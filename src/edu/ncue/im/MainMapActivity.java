package edu.ncue.im;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import com.google.android.maps.*;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import android.location.LocationListener;
import edu.ncue.test.jls.*;


public class MainMapActivity extends MapActivity{//繼承mapActivity
    /** Called when the activity is first created. */
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATE = 0;	//meter
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 100; 	//milesecond
	
	protected static float DISTANCE_TO_SEARCH = 1000f;				//meter
	
	final static String POI_TAPPED_ACTION = "MainMapActivity.POI_TAPPED_ACTION";
	
	protected LocationManager locator;	
	protected Button gpsButton;
	protected ImageButton searchButton;
	protected ImageButton displayListButton;
	protected MapView mv;
	protected MapController mapController;
	protected SeekBar yearSeekbar;
	protected static MyLocationListener myLocationListener;
	protected MyLocationOverlay myLocationOverlay;
	protected SlidingDrawer poiDrawer;
	protected POILoadTask poiLoadTask;
	private ArrayList<Map<String, String>>soilist;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);	//設定layout
        
        
        gpsButton = (Button) findViewById(R.id.retrieve_Location_Button);	//create button&View
        searchButton = (ImageButton) findViewById(R.id.pop_keyboard_Button);
        displayListButton = (ImageButton)findViewById(R.id.display_list_Button);
        poiDrawer = (SlidingDrawer)findViewById(R.id.poiDrawer);
        poiDrawer.setVisibility(View.GONE);
        locator = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        soilist = new ArrayList<Map<String, String>>();
        mv = (MapView) findViewById(R.id.mapview);
        mv.setBuiltInZoomControls(false);	//disable the zoom button
        mapController = mv.getController();
        myLocationListener = new MyLocationListener();
        poiLoadTask = new POILoadTask();
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
    	if(this.myLocationOverlay !=        null){
    		this.myLocationOverlay.disableMyLocation();
    		this.myLocationOverlay.disableCompass();
    	}
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
    		
    		GeoPoint currentPoint = new GeoPoint((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
    		Toast.makeText(MainMapActivity.this,"Current Location \n Longitude: "+currentPoint.getLongitudeE6()+"\n Latitude: "+currentPoint.getLatitudeE6(), Toast.LENGTH_LONG).show();
    		//retrieve GPS data and zoom to that position
    		mapController.animateTo(currentPoint);
    		mapController.setZoom(16);
        	
    		if(!mv.getOverlays().isEmpty())//clear the old place first
    			mv.getOverlays().clear();
    		
    		//OverlayItem overlayItem = new OverlayItem(currentPoint, "Current Position","");
    		myLocationOverlay = new MyLocationOverlay(this, this.mv);
    		myLocationOverlay.enableCompass();
        	myLocationOverlay.enableMyLocation();
            
        	//POI TAPPED ACTION
    		IntentFilter intentFilter = new IntentFilter();
    		intentFilter.addAction(POI_TAPPED_ACTION);
    		this.registerReceiver(new BroadcastReceiver(){
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					String title = (String) arg1.getSerializableExtra("POITitle");
					String snippets = (String) arg1.getSerializableExtra("POISnippet");
					if(poiDrawer.getVisibility()==View.GONE){
						//pop up animate still have bug
						/*AnimationSet animateSet = new AnimationSet(true);
						Animation slideUp = new TranslateAnimation(
				                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				                Animation.RELATIVE_TO_SELF, 1.0f);
						slideUp.setDuration(600);
						slideUp.setAnimationListener(new AnimationListener(){
						});
						animateSet.addAnimation(slideUp);
						LayoutAnimationController controller = new LayoutAnimationController(animateSet, 0.25f);
						
						poiDrawer.startAnimation(slideUp);
						*/
						
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
    		
    		//POIItemizedOverlay itemizedOverlay = new POIItemizedOverlay(this.getResources().getDrawable(R.drawable.map_arrow), this);
    		//itemizedOverlay.addOverlay(overlayItem);
    		//mv.getOverlays().add(itemizedOverlay);
    		mv.getOverlays().add(myLocationOverlay);
    		
    		
    		
    		//receiver test
    		this.getList(location.getLatitude(),location.getLongitude());
    		if(!soilist.isEmpty()){
    			POIItemizedOverlay poiOverlay = new POIItemizedOverlay(this.getResources().getDrawable(R.drawable.poi), this);
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
    
	public ArrayList<Map<String, String>> getList(double latitude, double longitude){
		poiLoadTask = new POILoadTask();
		poiLoadTask.execute(latitude,longitude);	
		try {
				soilist = this.poiLoadTask.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return soilist;
		
			 
	}
    
	//inner Class
    private class MyLocationListener implements LocationListener{
    	
    	public MyLocationListener(){
    		super();
    		
    	}
    	public void onLocationChanged(Location location) {
    		String message = String.format("NEW LOCATION Dectected! \n %1$f \n %2$f", location.getLongitude(),location.getLatitude());
    		Toast.makeText(MainMapActivity.this, message, Toast.LENGTH_LONG).show();
    		getList(location.getLatitude(),location.getLongitude());
    	}

    	public void onProviderDisabled(String provider) {
    		Toast.makeText(MainMapActivity.this,"GPS funtion shot down, please turn on GPS to keep most funtion to work",Toast.LENGTH_SHORT).show();
		
    	}

    	public void onProviderEnabled(String provider) {
    		Toast.makeText(MainMapActivity.this,"Provider enabled by the user. GPS turned ON",Toast.LENGTH_LONG).show();
		
    	}

    	public void onStatusChanged(String provider, int i, Bundle extras) {
    		Toast.makeText(MainMapActivity.this, "Provider StatusChanged", Toast.LENGTH_LONG).show();
		
    	}
	
    }
    
	//Network must need AsyncTask to run on another thread
	private class POILoadTask extends AsyncTask<Double, Void, ArrayList<Map<String, String>>>{
		ProgressDialog p;
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			Log.d("progress", "task onPreExecute");
			p = ProgressDialog.show(MainMapActivity.this, "Downloading", "自伺服器下載資料中...");
		}
		
		@Override
		protected ArrayList<Map<String, String>> doInBackground(Double... params) {
			DEHAPIReceiver receiver = new DEHAPIReceiver(params[0],params[1],MainMapActivity.DISTANCE_TO_SEARCH);
			return receiver.getsoilist();
			
		}
		@Override
		protected void onPostExecute(ArrayList<Map<String, String>> result){
			super.onPostExecute(result);
			Log.d("progress", "task onPostExecute");
			p.dismiss();
		}
		

		
	}
    
}
