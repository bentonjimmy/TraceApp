package edu.njit.CS656.traceapp;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity 
implements GooglePlayServicesClient.ConnectionCallbacks,
			com.google.android.gms.location.LocationListener,
			GooglePlayServicesClient.OnConnectionFailedListener{
	
	private GoogleMap theMap;
	private LocationClient myLocationClient;
	private PolylineOptions lineOptions;
	private Polyline line;
	private int lineColor = Color.GREEN;
	private boolean tracking = false;
	
	private final static LocationRequest REQUEST = LocationRequest.create()
			.setInterval(2000).setFastestInterval(500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	private final static int WALKING = Color.GREEN;
	private final static int DRIVING = Color.RED;
	private final static int PUBTRANS = Color.BLUE;

	/*
	 * This is called when the app is first started.  The initial view is set and the Map is retrieved.
	 */
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.activity_main);
	   getMap();
   }
   
   /*
    * This method runs when the app is active.  The map is retrieved along with the location client.
    */
   @Override
   protected void onResume()
   {
	   super.onResume();
	   getMap();
	   getLocationClient();
	   //initializeCamera();
   }
   
   /*
    * This needs to continue to receive new location updates but they don't need to be refreshed on
    * the map.
    */
   @Override
   protected void onPause()
   {
	   super.onPause();
   }
   
   @Override
   protected void onStop()
   {
	   //This should close all connections 
	   super.onStop();
	   if(myLocationClient != null)
	   {
		   myLocationClient.disconnect();
	   }
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
	   //Inflate the menu; this adds items to the action bar
	   //This will need Start, Stop, Pause, Reset
	   MenuInflater inflater = getMenuInflater();
	   inflater.inflate(R.menu.main, menu);
	   return super.onCreateOptionsMenu(menu);
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
	   //Handles actions in the action bar
	   switch(item.getItemId())
	   {
	   		case R.id.action_settings:
			   //do something
			   return true;
	   		case R.id.startTracking:
	   			startTracking();
	   			return true;
	   		case R.id.stopTracking:
	   			stopTracking();
	   			return true;
	   		default:
	   			return super.onOptionsItemSelected(item);
	   }
   }
   
   protected void getMap()
   {
	   if(theMap == null)
	   {
		   theMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		   if(theMap != null)
		   {
			   theMap.setMyLocationEnabled(true); //allow my location to be found
			   
		   }
	   }
   }
   
   protected void getLocationClient()
   {
	   if(myLocationClient == null)
	   {
		   myLocationClient = new LocationClient(getApplicationContext(), this, this);
		   if(myLocationClient != null)
		   {
			   myLocationClient.connect();
		   }
		   
	   }
	   else if(myLocationClient.isConnected() == false)
	   {
		   myLocationClient.connect();
	   }
   }

	
	@Override
	public void onLocationChanged(Location location) {
		updateLocation(location.getLatitude(), location.getLongitude());
		
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		myLocationClient.requestLocationUpdates(REQUEST, this);
		
	}
	
	
	private void updateLocation(double latitude, double longitude)
	{
		theMap.moveCamera(CameraUpdateFactory.newCameraPosition(
				new CameraPosition.Builder().target(new LatLng(latitude, longitude))
				.zoom(15.5f).bearing(0).tilt(25).build()));
		
		//Only add the points if we are tracking the user
		if(tracking)
		{
			addPoint(latitude, longitude);
		}
	}
	
	private void initializeCamera()
	{
		Location lastLoc = myLocationClient.getLastLocation();
		theMap.moveCamera(CameraUpdateFactory.newCameraPosition(
				new CameraPosition.Builder().target(new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()))
				.zoom(15.5f).bearing(0).tilt(25).build()));
	}
	
	private void addPoint(double latitude, double longitude)
	{
		if(lineOptions == null)
		{
			lineOptions = new PolylineOptions().add(new LatLng(latitude, longitude))
					.color(lineColor).width(35).geodesic(false);
			line = theMap.addPolyline(lineOptions);
		}
		if(line != null)
		{
			List<LatLng> points = line.getPoints();
			points.add(new LatLng(latitude, longitude));
			line.setPoints(points);
		}
	}
   
	/*
	 * This method is used to start tracking the user's movements
	 * and painting them to the map
	 */
	public void startTracking()
	{
		tracking = true;
		//Start new line here
	}
	
	/*
	 * This method is used to stop tracking the user and to stop painting the line
	 * on the map
	 */
	public void stopTracking()
	{
		tracking = false;
	}
	
	public void resetTracking()
	{
		//Not working properly
		//tracking = false;
		theMap.clear(); //Clear everything on the map
		lineOptions = null;
		//line.remove(); //Removes the last line
		line = null;
	}
	
	public void setWalking(View view)
	{	
		lineColor = WALKING;
		lineOptions = null;
		modeToast("walking");
	}
	
	public void setDriving(View view)
	{
		lineColor = DRIVING;
		lineOptions = null;
		modeToast("driving");
	}
	
	public void setPubTrans(View view)
	{
		lineColor = PUBTRANS;
		lineOptions = null;
		modeToast("on public transportation");
	}
	
	private void modeToast(CharSequence action)
	{
		Context context = getApplicationContext();
		CharSequence text = "You are now "+ action;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
}

