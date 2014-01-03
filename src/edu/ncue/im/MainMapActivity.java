package edu.ncue.im;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import edu.ncue.test.jls.R;

public class MainMapActivity extends MapActivity{//繼承mapActivity
    /** Called when the activity is first created. */
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATE = 10;	//meter
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 100; 	//milesecond
	
	protected static float DISTANCE_TO_SEARCH = 1000f;				//meter
	
	final static String POI_TAPPED_ACTION = "MainMapActivity.POI_TAPPED_ACTION";
	
	protected LocationManager locationManager;	
	//protected Button gpsButton;
	//protected ImageButton searchButton;
	//protected ImageButton displayListButton;
	protected MyMapView mv;
	protected MapController mapController;
	protected SeekBar yearSeekBar;
	protected static MyLocationListener myLocationListener;
	protected MyLocationOverlay myLocationOverlay;
	protected SlidingDrawer poiDrawer;
	protected POILoadTask poiLoadTask;
	protected ArrayList<Map<String, String>>soilist;
	protected int yearToSearch; 
	protected TextView yearTextView;
	protected RelativeLayout yearLayout;
	protected View popView;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		int id = item.getItemId();
		if(id == R.id.get_location){
			this.showCurrentLocation();
			return true;
		}
		else if(id == R.id.listView){
			this.enterListView();
			return true;
		}
		else if(id == R.id.history_scoop){
			this.showHistoryScoop();
			return true;
		}
		else if(id == R.id.setting){
			this.enterSetting();
			return true;
		}
		else if(id == R.id.nfc_passport){
			this.enterNfc();
			return true;
		}
		else{
			return super.onOptionsItemSelected(item);
		}
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);	//設定layout
        SharedPreferences mPrefs = this.getSharedPreferences("edu.ncue.im.DistanceSetting", Context.MODE_PRIVATE);
        switch(mPrefs.getInt("distanceSetting", 1)){
        case 0:
        	DISTANCE_TO_SEARCH = 500;
        	break;
        case 1:
        	DISTANCE_TO_SEARCH = 1000;
        	break;
        case 2:
        	DISTANCE_TO_SEARCH = 1500;
        	break;
        default:
        	DISTANCE_TO_SEARCH = 1000;
        	break;
        }
        
        
        yearLayout = (RelativeLayout)findViewById(R.id.yearLayout);
        yearLayout.setVisibility(View.GONE);
        
        yearToSearch = 0;
        yearSeekBar = (SeekBar) findViewById(R.id.year_seekBar);
        yearSeekBar.setMax(500);
        //yearSeekBar.setVisibility(View.GONE);
        yearSeekBar.setEnabled(false);
        yearTextView = (TextView) findViewById(R.id.year_TextView);
        

        poiDrawer = (SlidingDrawer)findViewById(R.id.poiDrawer);
        poiDrawer.setVisibility(View.GONE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        soilist = new ArrayList<Map<String, String>>();
        mv = (MyMapView) findViewById(R.id.mapview);
        
        mv.setBuiltInZoomControls(false);	//disable the zoom button
        mapController = mv.getController();
        myLocationOverlay = new MyLocationOverlay(this, this.mv);
        myLocationListener = new MyLocationListener();
        poiLoadTask = new POILoadTask();
        final POIItemizedOverlay myOverlay = new POIItemizedOverlay(this.getResources().getDrawable(R.drawable.map_arrow), this);
        mv.setOnLongpressListener(new MyMapView.OnLongpressListener() {
			
			@Override
			public void onLongpress(MapView view, final GeoPoint longpressLocation) {
				runOnUiThread(new Runnable(){
					public void run(){
						myOverlay.clear();
						OverlayItem overlayItem = new OverlayItem(longpressLocation, "指定位址"," ");
						
						myOverlay.addOverlay(overlayItem);
						getList(longpressLocation.getLatitudeE6() / 1E6, longpressLocation.getLongitudeE6() / 1E6);
						if(drawOnMap()!= true){
							yearSeekBar.setEnabled(true);
							Toast.makeText(getApplication(),"搜尋條件下無景點",Toast.LENGTH_SHORT).show();
						}			    		
			    		mv.getOverlays().add(myOverlay);
			    		mv.invalidate();
					}
				});
				
			}
		});
        
        popView = super.getLayoutInflater().inflate(R.layout.overlay_pop, null);
        mv.addView(popView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
        popView.setVisibility(View.GONE);

        yearSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
        	
        	Boolean isDrawed;
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				yearToSearch = progress+1500;
				isDrawed = drawOnMap();
				mv.invalidate();
				yearTextView.setText(String.valueOf(yearToSearch));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(isDrawed == false)
					Toast.makeText(getApplication(),"已無符合年代以上之景點",Toast.LENGTH_SHORT).show();
			}
        	
        });
        if(!locationEnabled){
        	AlertDialog.Builder builder = new AlertDialog.Builder(MainMapActivity.this);
    		builder.setMessage("GPS功能關閉中，是否前往設定開啟?")
    			.setCancelable(true)
    			.setPositiveButton("設定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				})
				.setNegativeButton("稍後", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
					}
				});
    		AlertDialog alert = builder.create();
    		alert.show();
        }
        
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	if(this.myLocationOverlay !=        null){
    		this.myLocationOverlay.disableMyLocation();
    		this.myLocationOverlay.disableCompass();
    	}
    		Log.d("gps", "paused");
    	locationManager.removeUpdates(myLocationListener);
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	Log.d("gps", "Resumed");
    	
    	locationManager.requestLocationUpdates(
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

    protected void showHistoryScoop(){
    	yearLayout.setVisibility(View.VISIBLE);
		AnimationSet animateSet = new AnimationSet(true);
		Animation slideUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
		slideUp.setDuration(600);
		slideUp.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
			}
		});
		animateSet.addAnimation(slideUp);
		LayoutAnimationController controller = new LayoutAnimationController(animateSet, 0.25f);
		
		this.yearLayout.startAnimation(slideUp);
    }
	protected void showCurrentLocation(){
    	Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	if (location == null){
    		location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    		Log.d("dev", "Location provide from Network");
    	}
    	if (location != null)
    	{ 			
    		
    		GeoPoint currentPoint = new GeoPoint((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
    		//Toast.makeText(MainMapActivity.this,"Current Location \n Longitude: "+currentPoint.getLongitudeE6()+"\n Latitude: "+currentPoint.getLatitudeE6(), Toast.LENGTH_LONG).show();
    		//retrieve GPS data and zoom to that position
    		mapController.animateTo(currentPoint);
    		mapController.setZoom(16);
        	
    		if(!mv.getOverlays().isEmpty())//clear the old place first
    			mv.getOverlays().clear();
    		
    		//OverlayItem overlayItem = new OverlayItem(currentPoint, "Current Position","");
    		
    		myLocationOverlay.enableCompass();
        	myLocationOverlay.enableMyLocation();
        	myLocationOverlay.runOnFirstFix(new Runnable() {
  			  public void run() {
  			    mapController.animateTo(myLocationOverlay.getMyLocation());
  			     }
  			});
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
    		
    		
    		
    		getList(location.getLatitude(),location.getLongitude());
    		if(drawOnMap()!= true)
    			Toast.makeText(getApplication(),"搜尋條件下無景點",Toast.LENGTH_LONG).show();
    		yearSeekBar.setEnabled(true);    		
    		mv.invalidate();
    		
    	}
    	else
    	{
    		Toast.makeText(getApplication(), "無法取得您所在的位址，請稍後再試。",Toast.LENGTH_LONG).show();
    	}
    	
    }
	
	protected void enterListView(){
		Intent intent = new Intent();
		
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location == null)
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		if(location != null){//passing currentGeoData to ListView
			
    		Bundle bundle = new Bundle();
    		if(soilist == null){
    			getList(location.getLatitude(),location.getLongitude());
    			Log.d("data", soilist.toString());
    			}
    		bundle.putSerializable("POI", soilist);
			intent.putExtras(bundle);
			//bundle.putDouble("CurrentLongitude", location.getLongitude());
			//bundle.putDouble("CurrentLatitude", location.getLatitude());
		}
		intent.setClass(getApplicationContext(), BrowseContentActivity.class);
		startActivity(intent);
	}
	
	POIItemizedOverlay oldpoiOverlay;
	protected boolean drawOnMap(){ //if draw new poi is needed then return true otherwise just return false
		if(!soilist.isEmpty()){
			POIItemizedOverlay poiOverlay = new POIItemizedOverlay(this.getResources().getDrawable(R.drawable.poi), this);
			if(oldpoiOverlay != null){
				mv.getOverlays().remove(oldpoiOverlay);
			}
			for(Map<String, String> map : soilist){	//check every point are in the year
				GeoPoint gp;
				if(Integer.parseInt(map.get("POI_YEAR"))<=this.yearToSearch){
					gp = new GeoPoint((int)(Double.parseDouble(map.get("latitude"))*1E6),(int)(Double.parseDouble(map.get("longitude"))*1E6));
					OverlayItem poi = new OverlayItem(gp, map.get("POI_title"),map.get("POI_description"));
					poiOverlay.addOverlay(poi);
				}
			}
			if(poiOverlay.size() != 0){
				mv.getOverlays().add(poiOverlay);
				oldpoiOverlay = poiOverlay;
				return true;
			}
			else 
				return false;
		}
		else{
			return false;
		}
		
	}
	
	protected void enterSetting(){
		startActivity(new Intent().setClass(getApplicationContext(), SettingsActivity.class));
	}
	
	//NFCPassPort
	protected void enterNfc(){
		startActivity(new Intent().setClass(getApplicationContext(), NfcPassportActivity.class));
	}
	Location oldLocation;
	
	public ArrayList<Map<String, String>> getList(double latitude, double longitude){
		Location currentLocation = new Location("currentLocation");
		
		currentLocation.setLatitude(latitude);
		currentLocation.setLongitude(longitude);
		if(soilist.isEmpty() || oldLocation == null){
			oldLocation = new Location("oldLocation");
			poiLoadTask = new POILoadTask();
			poiLoadTask.execute(latitude, longitude);
			try {
				soilist = this.poiLoadTask.get();
			} catch (InterruptedException e) {
				Toast.makeText(MainMapActivity.this, "資料格式不符發生問題", Toast.LENGTH_SHORT);
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			oldLocation.setLatitude(latitude);
			oldLocation.setLongitude(longitude);
			return soilist;
		}
		
		float distance = currentLocation.distanceTo(oldLocation);
		if (distance > 100) {
				poiLoadTask = new POILoadTask();
				poiLoadTask.execute(latitude, longitude);
				try {
					soilist = this.poiLoadTask.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				oldLocation.setLatitude(latitude);
				oldLocation.setLongitude(longitude);
				Log.d("GPS", "Distance:"+distance);
			}
			else
				Log.d("GPS", "Too Near Distance:"+distance);
		
		
		
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
    		//
    		//getList(location.getLatitude(),location.getLongitude());
    	}

    	public void onProviderDisabled(String provider) {
    		Toast.makeText(MainMapActivity.this,"GPS funtion shot down, please turn on GPS to keep most funtion to work",Toast.LENGTH_SHORT).show();
		
    	}

    	public void onProviderEnabled(String provider) {
    		Toast.makeText(MainMapActivity.this,"Provider enabled by the user. GPS turned ON",Toast.LENGTH_SHORT).show();
		
    	}

    	public void onStatusChanged(String provider, int i, Bundle extras) {
    		Toast.makeText(MainMapActivity.this, "Provider StatusChanged", Toast.LENGTH_SHORT).show();
		
    	}
	
    }
    /*
     * 試存取DEP API POI Request
     * 傳出Request 並取得JSON回應
     * 最後處理成一份ArrayList
     */
	//Network must need AsyncTask to run on another thread
	private class POILoadTask extends AsyncTask<Double, Void, ArrayList<Map<String, String>>>{
		ProgressDialog p;
		String formatted_result;
		String request_URL;
		private ArrayList<Map<String, String>> soilist;
		
		protected JSONObject jsonObjcet;
		protected JSONArray jsonList;
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			Log.d("progress", "task onPreExecute");
			p = ProgressDialog.show(MainMapActivity.this, "Downloading", "自伺服器下載資料中...");
		}
		
		@Override
		protected ArrayList<Map<String, String>> doInBackground(Double... params) {
			
			request_URL ="http://deh.csie.ncku.edu.tw/dehencode/json/nearbyPOIs?lat="+params[0]+"&lng="+params[1]+"&dist="+MainMapActivity.DISTANCE_TO_SEARCH;
			soilist = new ArrayList<Map<String,String>>();
			try{
				formatted_result = this.sentHttpRequest(request_URL);
				soilist = parseJson(formatted_result);
			}catch(JSONException e){
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						Toast.makeText(MainMapActivity.this, "資料規格發生問題", Toast.LENGTH_SHORT).show();
						
					}
					
				});
				e.printStackTrace();
			}
			catch(Exception e){
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						Toast.makeText(MainMapActivity.this, "伺服器發生問題", Toast.LENGTH_SHORT).show();
						
					}
					
				});
				e.printStackTrace();
			}
			
			
			return this.getsoilist();
			
		}
		@Override
		protected void onPostExecute(ArrayList<Map<String, String>> result){
			super.onPostExecute(result);
			Log.d("progress", "task onPostExecute");
			p.dismiss();
		}
		
		private ArrayList<Map<String, String>> parseJson(String str)throws JSONException{
			ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
			if(formatted_result!=null){
				//formatted_result = purge(formatted_result);
				jsonObjcet = new JSONObject(str);
				JSONArray soilist = jsonObjcet.getJSONArray("results");
				
				
				for(int i=0;i<soilist.length();i++){
					JSONObject temp = soilist.getJSONObject(i);
					
					//add hashmap to ArrayList
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("POI_id", temp.getString("POI_id"));
					map.put("POI_title", temp.getString("POI_title"));
					map.put("distance", temp.getString("distance"));
					map.put("latitude", temp.getString("latitude"));
					map.put("longitude", temp.getString("longitude"));
					map.put("POI_description",temp.getString("POI_description"));
					map.put("POI_YEAR", temp.getString("year"));
					JSONObject picJson = temp.getJSONObject("PICs");
					map.put("pic_Count",picJson.getString("count"));
					if(picJson.getInt("count") != 0){
					
						JSONArray pics = picJson.getJSONArray("pic");
						String picsSumURL = new String();
						for (int j=0;j<picJson.getInt("count");j++){
							JSONObject jsonPic = pics.getJSONObject(j);
							String singleURL = jsonPic.getString("url");
							//jsonObject will retrieve one more h cause data error. ex hhttp://...
							picsSumURL += singleURL.substring(1,singleURL.length())+";";
							
							}
						map.put("PICsURL", picsSumURL);
						Log.d("jsonURL",map.get("PICsURL"));
					}					
					list.add(map);
					Log.d("json","Title:"+temp.getString("POI_title") );
			        Log.d("json","id:"+temp.getString("POI_id") );
			        Log.d("json","longitude:"+temp.getString("longitude"));
			        Log.d("json","latitude:"+temp.getString("latitude"));
			        Log.d("json","distance:"+temp.getString("distance") );
			        Log.d("json",map.get("pic_Count"));
			        Log.d("json","year:"+temp.getString("year"));
			        
				}
					
				}
			else
				Log.d("mine","Null List");
				return list;
			}
		
		private String sentHttpRequest(String url) throws Exception{
			BufferedReader in = null;
			try{
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url);
				Log.d("json", url);
				HttpResponse response = client.execute(request);
				
				in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				
				while((line = in.readLine())!= null){
					sb.append(line+NL);
				}
				in.close();
				
				String result = sb.toString();
				return result;
			}catch(Exception e){
				Toast.makeText(MainMapActivity.this, "伺服器發生問題", Toast.LENGTH_SHORT).show();
				e.getMessage();
				e.printStackTrace();
				
				return null;
			}
		}
		
		public ArrayList<Map<String, String>> getsoilist(){
			return soilist;
		}
		

		
	}
    
}
