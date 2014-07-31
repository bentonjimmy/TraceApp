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

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
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
	   //This will need Start, Stop, Reset
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
	   		case R.id.resetTrack:
	   			resetTracking();
	   			return true;
	   		default:
	   			return super.onOptionsItemSelected(item);
	   }
   }
   
   /*
   This method initializes the Google Map object by retreiving it from the Support Fragment Manager.
   The map is then set to allow it to show our location.
   */
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
   
   /*
   Initializes the Location Client.  The location client will provide all location data needed
   to add the user's location to the map.
   */
   protected void getLocationClient()
   {
	   if(myLocationClient == null)
	   {
		   myLocationClient = new LocationClient(getApplicationContext(), this, this);
		   if(myLocationClient != null)
		   {
			   myLocationClient.connect(); //start the connection
		   }
		   
	   }
	   else if(myLocationClient.isConnected() == false)
	   {
		   myLocationClient.connect();
	   }
   }

	
	/*
	Method that is called whenever the user's location changes.
	*/
	@Override
	public void onLocationChanged(Location location) {
		updateLocation(location.getLatitude(), location.getLongitude());
		
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		myLocationClient.requestLocationUpdates(REQUEST, this);
		
	}
	
	/*
	This method controls the actions of what needs to be done with the user's location changes.
	The camera position is updated and if the user is tracing a route, a point will be added
	to the line on the map.
	*/
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
	
	/*
	This method adds points to the polyline on the map based off of the user's latitude and
	longitude.  A PolylineOption first needs to be created in order to retreive the Polyline
	that will be drawn on the map.
	*/
	private void addPoint(double latitude, double longitude)
	{
		if(lineOptions == null)
		{
			lineOptions = new PolylineOptions().add(new LatLng(latitude, longitude))
					.color(lineColor).width(35).geodesic(false);
			line = theMap.addPolyline(lineOptions); //add line to map
		}
		if(line != null)
		{
			List<LatLng> points = line.getPoints();
			points.add(new LatLng(latitude, longitude)); //add additional points to the line
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
	
	/*
	This method responds to the Walk button being pressed.  The line color is changed
	and lineOptions is set to null in order to create a new line on the map.
	*/
	public void setWalking(View view)
	{	
		lineColor = WALKING;
		lineOptions = null;
		modeToast("walking");
	}
	
	/*
	This method responds to the Driving button being pressed.  The line color is changed
	and lineOptions is set to null in order to create a new line on the map.
	*/
	public void setDriving(View view)
	{
		lineColor = DRIVING;
		lineOptions = null;
		modeToast("driving");
	}
	
	/*
	This method responds to the PubTrans button being pressed.  The line color is changed
	and lineOptions is set to null in order to create a new line on the map.
	*/
	public void setPubTrans(View view)
	{
		lineColor = PUBTRANS;
		lineOptions = null;
		modeToast("on public transportation");
	}
	
	/*
	This method handles the toasts that are displayed when the user changes it's mode
	of transportation.
	*/
	private void modeToast(CharSequence action)
	{
		Context context = getApplicationContext();
		CharSequence text = "You are now "+ action;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}


	/*
	The two methods below are needed in order to implement Google Play services interfaces.
	*/
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
}

