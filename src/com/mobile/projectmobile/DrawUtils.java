package com.mobile.projectmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


/**
 * constants used for drawing on map
 */
public class DrawUtils {
	
	/* 
	 * hex colors: RGBA format (Alpha is transparency)
	 */
	
	// circle color for my position
	final static private int TRASPARENT_BLUE = 0x330077FF; 	//fill color	
	final static private int DARK_BLUE = 0xFF2222FF;			//border color
	
	// circle color for other positions
	final static private int TRASPARENT_GREEN = 0x33FF0000; 	//fill color	
	final static private int DARK_GREEN = 0xFFFF0000;			//border color
	
	// circle radius (meter)
	final static private double RADIUS = 1000;
	
	// border width (pixel)
	final static private float BORDER_WIDTH = 1;
	
	public static CircleOptions myPositionCircleOptions;
	public static CircleOptions otherPositionsCircleOptions;

	public static List<LatLng> pointList;
	
	public static void setCircleOptions() {
		
		LatLng defaultCenter = new LatLng(40.722543,-73.998585);
		
		myPositionCircleOptions = new CircleOptions()
		.center(defaultCenter)
	    .radius(RADIUS)				// radius in meters
	    .fillColor(TRASPARENT_BLUE)	// color inside the circle
	    .strokeColor(DARK_BLUE)		// color of border
	    .strokeWidth(BORDER_WIDTH);	// width of border (1px)	
		
		otherPositionsCircleOptions = new CircleOptions()
		.center(defaultCenter)
	    .radius(RADIUS)					// radius in meters
	    .fillColor(TRASPARENT_GREEN)	// color inside the circle
	    .strokeColor(DARK_GREEN)		// color of border
	    .strokeWidth(BORDER_WIDTH);		// width of border (1px)
	}
	
	// from GeoJSON object to List of LatLng points
	public static void decodePoly(String encoded) {

		pointList = new ArrayList<LatLng>();
		
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
    }
	
} 
    