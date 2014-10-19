package com.mobile.projectmobile;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class ChatFragment extends Fragment {

	private List<GraphUser> friendsWithApp;

	/* Define private variables for the UiLifecycleHelper object and the Session.StatusCallback listener implementation. 
	 * The listener overrides the call() method to invoke the onSessionStateChange() method you previously defined */
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
	    super.onCreateView(inflater, container, savedInstanceState);
	    
	    View view = inflater.inflate(R.layout.chat_layout, container, false);
	    
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	    
	    return view;
	}
	
	//Next, define a private method that will respond to session changes and 
	//call the makeMeRequest() method if the session's open
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {

	}
	
	
	public void sendRequestDialog(Bundle params){
		
	    WebDialog requestsDialog = (
	        new WebDialog.RequestsDialogBuilder(getActivity(),Session.getActiveSession(), params))
	        .setOnCompleteListener(new OnCompleteListener() {

	                @Override
	                public void onComplete(Bundle values,FacebookException error) {
	                	
	                	
	                    if (error != null) {
	                        if (error instanceof FacebookOperationCanceledException) {
	                            Toast.makeText(getActivity(), "Request cancelled", Toast.LENGTH_SHORT).show();
	                        } else {
	                            Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_SHORT).show();
	                        }
	                    } else {
	                        final String requestId = values.getString("request");
	                        
	                        Log.e("sendRequestDialog - requestID", requestId);
	                        
	                        if (requestId != null) {
	                            Toast.makeText(getActivity(), "Request sent", Toast.LENGTH_SHORT).show();
	                        } else {
	                            Toast.makeText(getActivity(), "Request cancelled", Toast.LENGTH_SHORT).show();
	                        }
	                    }   
	                }

	            }).build();
	    
        Window dialog_window = requestsDialog.getWindow();
        
        dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
	    Log.e("sendRequestDialog","showFriends");
	    
	    requestsDialog.show();
	}
	
	
	public void addTripRequest() {
		
		Log.e("add_friend", "addTripRequest");
		
		// Okay, we're going to filter our friends by their device, we're looking for friends with an Android device
		
		// Get a list of the user's friends' names and devices
		final Session session = Session.getActiveSession();
	    
	    // Check to see that the user granted the user_friends permission before loading friends.
	    // This only loads friends who've installed the game.
	    if (session.getPermissions().contains("user_friends")) {
	        // Get the user's list of friends
	        Request friendsRequest = Request.newMyFriendsRequest(session, 
	                new Request.GraphUserListCallback() {

	            @Override
	            public void onCompleted(List<GraphUser> users, Response response) {
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                    Log.e("add_friend", error.toString());

	                } else if (session == Session.getActiveSession()) {
	                	
	                    GraphMultiResult multiResult = response.getGraphObjectAs(GraphMultiResult.class);
	                    GraphObjectList<GraphObject> data = multiResult.getData();
	                    friendsWithApp = data.castToListOf(GraphUser.class);
	                	
	                	Log.e("SIZE_USERS", Integer.toString(friendsWithApp.size()));
	                    
	                    //Store the filtered friend ids in the following List
						ArrayList<String> friendIDs = new ArrayList<String>();
	                    
	                    for(GraphUser friend : friendsWithApp){
		                    Log.e("FRIENDS NAMEs", friend.getFirstName());
		                    
		                    friendIDs.add(friend.getId());
	                    }
	                    
	                    Bundle params = new Bundle();
	                    params.putString("to", TextUtils.join(",", friendIDs.toArray(new String[friendIDs.size()])));
	                    params.putString("message","Why don't you travel with me?");
	                    
	                    Log.e("FRIENDS IDs", params.getString("to"));

	                    sendRequestDialog(params);
	                }
	            }
	        });
	        
	        Bundle params = new Bundle();
	        params.putString("fields", "name,first_name,last_name");
	        friendsRequest.setParameters(params);
	        friendsRequest.executeAsync();
	    }
	}
	
	//call the corresponding UiLifecycleHelper method if the REAUTH_ACTIVITY_CODE request code is passed in
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    //if (requestCode == REAUTH_ACTIVITY_CODE) {
	        uiHelper.onActivityResult(requestCode, resultCode, data);
	    //}
	}
	
	//make sure all other the fragment lifecycle methods call the relevant methods in the UiLifecycleHelper class
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
	    super.onSaveInstanceState(bundle);
	    uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
}
