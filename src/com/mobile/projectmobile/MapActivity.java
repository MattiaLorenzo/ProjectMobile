package com.mobile.projectmobile;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends ActionBarActivity
						implements LocationListener, 
								ConnectionCallbacks, OnConnectionFailedListener {

	
    // Debugging tag for the application
    static final String APPTAG = "App Debug";
    static final String MAPNAME = MapActivity.class.getName();
    
	// Reference to add_friend item of the ActionBar
    private MenuItem[] items;
    private static final int NUM_ITEMS = 5;
	private static final int VIEW_PATH = 0;
	private static final int MY_POSITION = 1;
	private static final int START_STOP_UPDATES = 2;
	private static final int CHAT = 3;
	private static final int ADD_FRIEND = 4;
    
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
    
    // Latitude and longitude of the user
    private LatLng myCoord;  
    
    // TODO
    // Latitude and longitude of friends
    private LatLng[] otherCoords;    
    
    // circles for my / other position
    CircleOptions myPositionCircleOptions, otherPositionsCircleOptions;
    private Circle myCircle;
    private Circle[] otherCircles;
    
    // departure-destination point of the trip
    private LatLng srcPoint, dstPoint;
    
    /*
     * It is "true" if periodic location updates have been turned on. 
     * It is "false" by default; it's set to "true" in the
     * method handleRequestSuccess of LocationUpdateReceiver.
     */
    private boolean updatesRequested = false;
    
    /*
     * It is "true" if we want GoogleMaps camera to follow the user's car;
     * "true" by default
     */
    private boolean centered = true;
    
    
    // Handle to SharedPreferences for this app
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    
    
    // Fragment attributes
    private Fragment chatFragment = new Fragment();
    
	private GoogleMap googleMap;
	

    
    /**
     * function to draw my position on the map: marker + circle
     * */
    private void drawPositionOnMap(LatLng coord) {
    	
    	// first time the circle is draw
    	if(myCircle == null)
	    	myCircle = googleMap.addCircle(myPositionCircleOptions);
    	
    	// set the center for the first time or move the circle
    	myCircle.setCenter(coord); //myCircle is not NULL
    	
    }

    
    // from GeoJSON object to List of LatLng points
 	public static List<LatLng> decodePoly(String encoded) {

 		List<LatLng> pointList = new ArrayList<LatLng>();
 		
         int index = 0, len = encoded.length();
         int lat = 0, lng = 0;

         while (index < len) {
             int b, shift = 0, result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lat += dlat;

             shift = 0;
             result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lng += dlng;

             LatLng p = new LatLng( (((double) lat / 1E5)),
                      (((double) lng / 1E5) ));
             pointList.add(p);
         }
         
         return pointList;
     }
    /**
     * function to draw path between srcPoint and dstPoint
     * */
    private void drawPathOnMap(LatLng srcCoord, LatLng dstCoord) {
    	
    	GeoJSONRequest req = new GeoJSONRequest();
    
    	String jsonMex = req.requestGeoJSONObject(srcCoord, dstCoord);
    	
    	
    	try {
		   //Tranform the string into a json object
		   JSONObject json = new JSONObject(jsonMex);
		   JSONArray routeArray = json.getJSONArray("routes");
		   JSONObject routes = routeArray.getJSONObject(0);
		   JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
		   String encodedString = overviewPolylines.getString("points");
		   
		   DrawUtils.decodePoly(encodedString);
		   List<LatLng> pointList = DrawUtils.pointList;
		   
		   //List<LatLng> pointList = decodePoly(encodedString);
	   
		   //from list of LatLng to PolyLine on Google Maps
		   for(int z = 0; z<pointList.size()-1;z++){
			   LatLng src= pointList.get(z);
	           LatLng dest= pointList.get(z+1);
	           googleMap.addPolyline(new PolylineOptions()
	           	.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
	            .width(3)
	            .color(Color.BLUE).geodesic(true));
		   }
		} 
    	catch (JSONException e) {
    		Log.e(APPTAG, "JSONException");
    	}

    }
    
    /**
     * function to move camera position towards the specified position
     * */
    private void moveCameraPositionTo(LatLng coord) {

    	// TODO spostare camera nella direzione in cui va utente
    	CameraPosition cameraPosition = new CameraPosition.Builder().target(coord).zoom(13).tilt(45).build();
    	googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }    
    
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
                Log.e(APPTAG, getString(R.string.error_gmaps));
            }
        }
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
        try {
            // Loading map
            initMap();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Remove App icon and title from Action Bar
        this.getSupportActionBar().setDisplayShowHomeEnabled(false);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        
		FragmentManager fm = getSupportFragmentManager();
		
		//Fragments are already defined in home.xml
        chatFragment = fm.findFragmentById(R.id.chat);

        FragmentTransaction transaction = fm.beginTransaction();

        transaction.hide(chatFragment);
        
        transaction.commit(); 
        
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set location parameters
         */
        
        // Set the update interval
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
        
        //TODO: TWO MOCK LOCATION JUST FOR TESTING
        srcPoint = new LatLng(51,0);
        dstPoint = new LatLng(51.5,-0.1);           
        
        // Note that location updates are off until the user turns them on
        updatesRequested = false;
        
        // Set parameters of circles draw on the map
        DrawUtils.setCircleOptions();
        myPositionCircleOptions = DrawUtils.myPositionCircleOptions;
        otherPositionsCircleOptions = DrawUtils.otherPositionsCircleOptions;
       
        // User's car in the center of the map by default
        centered = true;
       	
        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();
        
        //TODO: JUST TESTING
        drawPathOnMap(srcPoint, dstPoint);
	}
 
    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();

    }
    
    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        
        initMap();
        
        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
        	

            /* TODO RIPRISTINARE ICONA START O PAUSA OPPORTUNA
            updatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

            if(updatesRequested)
            	startStopItem.setIcon(R.drawable.pause);
            else 
            	startStopItem.setIcon(R.drawable.start);
            */
            
        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

    }

    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {

        // Save the current setting: location updates requested or not
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, updatesRequested);
        mEditor.commit();

        super.onPause();
    }
    
    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }
    
    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(APPTAG, getString(R.string.resolved));

                    break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(APPTAG, getString(R.string.no_resolution));

                    break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(APPTAG,
                       getString(R.string.unknown_activity_request_code, requestCode));

               break;
        }
    }
    
    private void manageChatFragment(boolean showChat){
    	
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
    	
    	if(showChat){
    	    items[VIEW_PATH].setVisible(false);
    	    items[MY_POSITION].setVisible(false);
    	    items[START_STOP_UPDATES].setVisible(false);
    	    items[CHAT].setVisible(false);
    	    items[ADD_FRIEND].setVisible(true);
        	
            transaction.show(chatFragment);

    	}
    	else{
    	    items[VIEW_PATH].setVisible(true);
    	    items[MY_POSITION].setVisible(true);
    	    items[START_STOP_UPDATES].setVisible(true);
    	    items[CHAT].setVisible(true);
    	    items[ADD_FRIEND].setVisible(false);
    		
            transaction.hide(chatFragment);
    	}
    	
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.action_bar, menu);
	    
	    items = new MenuItem[NUM_ITEMS];
	    
	    items[VIEW_PATH] = menu.findItem(R.id.view_path);
	    items[MY_POSITION] = menu.findItem(R.id.my_position);
	    items[START_STOP_UPDATES] = menu.findItem(R.id.start_stop_updates);
	    items[CHAT] = menu.findItem(R.id.chat_item);
	    items[ADD_FRIEND] = menu.findItem(R.id.add_friend_item);
	    
	    items[ADD_FRIEND].setVisible(false);
	    
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
	    // Handle presses on the action bar items
	    switch (id) {
        	case android.R.id.home: 
        		onBackPressed();
        		return true;
	    
	    	case R.id.view_path:
	    		/*
	    		double minLat, maxLat, minLng, maxLng;
	    		double srcLat, dstLat, srcLng, dstLng;
	    		double latSpan, lngSpan;
	    		
	    		srcLat = srcPoint.latitude;
	    		dstLat = dstPoint.latitude;
	    		srcLng = srcPoint.longitude;
	    		dstLng = dstPoint.longitude;
	    		
	    		latSpan = Math.abs(dstLat - srcLat);
	    		lngSpan = Math.abs(dstLng - srcLng);
	    		
	    		MapController controller = new MapController();
	    		controller.zoomToSpan((int)latSpan, (int)lngSpan);
	    		
	    		
	    		
	    		LatLng averagePoint = new LatLng( (minLat + maxLat)/2, (minLng + maxLng)/2 );    		
	    		moveCameraPositionTo(averagePoint);
	    		*/
	    		
	    		double minLat, maxLat, minLng, maxLng;
	    		double srcLat, dstLat, srcLng, dstLng;
	    		double latSpan, lngSpan;
	    		int width, height;
	    		
	    		srcLat = srcPoint.latitude;
	    		dstLat = dstPoint.latitude;
	    		srcLng = srcPoint.longitude;
	    		dstLng = dstPoint.longitude;
	    		
	    		/*
	    		srcLat = -44;
	    		dstLat = -10;
	    		srcLng = 113;
	    		dstLng = 154;
	    		*/
	    		minLat = Math.min(srcLat, dstLat);
	    		maxLat = Math.max(srcLat, dstLat);
	    		minLng = Math.min(srcLng, dstLng);
	    		maxLng = Math.max(srcLng, dstLng);
	    		
	    		
	    		// corners of bounding rectangle
	    		LatLng SWPoint = new LatLng(minLat, minLng); //south-west
	    		LatLng NEPoint = new LatLng(maxLat, maxLng); //north-east
	    		
	    		// Create a LatLngBounds that includes the entire route.
	    		LatLngBounds route = new LatLngBounds(SWPoint, NEPoint);

	    		latSpan = (int)Math.abs(dstLat - srcLat);
	    		lngSpan = (int)Math.abs(dstLng - srcLng);
	    		
	    		//TODO CONVERT latSpan e lngSpan to PIXEL 
	    		//width = fromDeegresToPixel(lngSpan);
	    		//height = fromDeegresToPixel(latSpan);
	    		
	    		// Set the camera to the greatest possible zoom level 
	    		// that includes the bounds
	 	    	//googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(route, width, height, 0));
	    		//googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.getCenter(), 10));
	    		
	    		return true;
	        case R.id.start_stop_updates:
	        	
	            updatesRequested = !updatesRequested;
	            
	            if(updatesRequested) {
	            	// Stop Location Updates has been clicked
	            	items[START_STOP_UPDATES].setIcon(R.drawable.pause); //start -> pause
	                if (servicesConnected()) {
	                    startPeriodicUpdates();
	                }
	            }
	            else {
	            	// Start Location Updates has been clicked
	            	items[START_STOP_UPDATES].setIcon(R.drawable.start); //pause -> start
	            	
	            	if (servicesConnected()) {
	                    stopPeriodicUpdates();
	                }
	            }
	            
	            return true;
	        case R.id.my_position:
	        	centered = true;
	        	
	        	// if "updatesRequested" is "false" my position could be out-of-date
	        	if(!updatesRequested) {
	        		Location myCurrentLocation = mLocationClient.getLastLocation();
	        		myCoord = new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude());
	        	}
	        	
	            if(myCoord != null) {
	            	moveCameraPositionTo(myCoord);
	            	drawPositionOnMap(myCoord); 	
	            }
	            return true;
	        case R.id.chat_item:
	    	    
	        	//we show the ChatFragment
	        	manageChatFragment(true);
	            
	        	/*
	        	Intent intent = new Intent(this, ChatActivity.class);
	            this.startActivity(intent);*/
	            return true;
	            
	        case R.id.add_friend_item:
	            	
            	((ChatFragment)chatFragment).addTripRequest();
            	
                Toast.makeText(this, 
                        "Button add friend", 
                        Toast.LENGTH_SHORT).show();
                return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onBackPressed(){
		
		if(chatFragment.isVisible()){
        	//we hide the ChatFragment
        	manageChatFragment(false);
		}
		else{
			NavUtils.navigateUpFromSameTask(this);
		}
			
	    return;
	}
	
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), APPTAG);
            }
            return false;
        }
    }
    

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
    	
    	// TODO DUBBIO: startPeriodicUpdates viene invocato due volte???
        if (updatesRequested) {
            startPeriodicUpdates();
        }
    }
    
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
    	Log.d(APPTAG, getString(R.string.disconnected));
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }
    
    /**
     * Google Map camera follows the user location
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
    	
        myCoord = new LatLng(location.getLatitude(), location.getLongitude());
        
        //When chatFragment is visible, we don't draw anything on the Map
        if(chatFragment.isHidden()){
	        drawPositionOnMap(myCoord);
	        
	        if(centered) {
	        	moveCameraPositionTo(myCoord);      	
	        }
        }
        
    	//TODO <SEND coord TO PHP SERVER>
        	
    }
    
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
    	
    	//"this" is MapActivity but it implements LocationListener
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
    	
    	//"this" is MapActivity but it implements LocationListener
    	mLocationClient.removeLocationUpdates(this);
    }
    
    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), APPTAG);
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
