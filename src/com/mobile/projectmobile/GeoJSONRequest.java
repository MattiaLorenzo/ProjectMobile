package com.mobile.projectmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/*
 * HTTP Request to Google Maps url
 * to retrieve GeoJSON object relative to a certain path
 */ 
public class GeoJSONRequest {
	
	private String jsonMex;
	
	// convert InputStream to String //TODO: TO USE
	private String getStringFromInputStream(InputStream is) {
 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
		return sb.toString();
 
	}
	
    private class DoHTTPRequest extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... urls) {
        	
        	InputStream is = null;			    
		    jsonMex = "";
		    
		    
	        // Doing HTTP request
	        try {
	        	
	            HttpClient httpClient = new DefaultHttpClient();

	            HttpPost httpPost = new HttpPost(urls[0]);          
	            HttpResponse httpResponse = httpClient.execute(httpPost);	            
	            HttpEntity httpEntity = httpResponse.getEntity();
	            		            
	            is = httpEntity.getContent();  
	
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    is, "iso-8859-1"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            
	            
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }

	            jsonMex = sb.toString();
	            
	            is.close();
	            
	        } catch (Exception e) {
	            Log.e("BUFFER ERROR", "Error: " + e.toString());
	        }   
	        return null;
        }

        protected void onPostExecute(Void result) {

        }
        
    }
    
    // Build Google Maps url to request GeoJSON
    private String makeURL(LatLng srcPoint, LatLng dstPoint) {
    	
    	double srcLat = srcPoint.latitude;
    	double srcLng = srcPoint.longitude;
    	double dstLat = dstPoint.latitude;
    	double dstLng = dstPoint.latitude;
    	    
        String url = "http://maps.googleapis.com/maps/api/directions/json?";

        //Parameters after "json?" : origin, destination, sensor
        List<NameValuePair> urlParams = new LinkedList<NameValuePair>();
        urlParams.add(new BasicNameValuePair("origin", srcLat + "," + srcLng));
        urlParams.add(new BasicNameValuePair("destination", dstLat + "," + dstLng));
        urlParams.add(new BasicNameValuePair("sensor", "false"));

        String urlParamString = URLEncodedUtils.format(urlParams, "utf-8");
        url += urlParamString;        
        
        return url;
    }
    
    public String requestGeoJSONObject(LatLng srcPoint, LatLng dstPoint) {
    	
		String url = makeURL(srcPoint, dstPoint);
		
		// Request GeoJSON
		DoHTTPRequest myTask = new DoHTTPRequest();
		myTask.execute(url);
		
		try {
			// Wait for AsyncTask to finish
			myTask.get();
		}
		catch (ExecutionException e) {}
		catch (InterruptedException e) {}
					
		return jsonMex;   
	}

	
}

