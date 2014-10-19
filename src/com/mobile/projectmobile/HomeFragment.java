package com.mobile.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class HomeFragment extends Fragment{
	
	/* Next, define a private constant that you'll use later on whenever you make a new permissions request. 
	 * You'll use it to decide whether to update a session's info in the onActivityResult() method: */
	private static final int REAUTH_ACTIVITY_CODE = 100;
	
	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	
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
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //initialize uiHelper object
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
	    super.onCreateView(inflater, container, savedInstanceState);
	    
	    View view = inflater.inflate(R.layout.fragment_home_fb_logged_in, container, false);
	    
		// Find the user's profile picture custom view
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.home_profile_pic);
		profilePictureView.setCropped(true);
		
		// Find the user's name view
		userNameView = (TextView) view.findViewById(R.id.home_user_name);
	    
	    Button createTripButton= (Button) view.findViewById(R.id.create_trip);
	    createTripButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	createTrip(v);
	        }
	    });
	    
	    // Check for an open session
	    Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	        // Get the user's data
	        makeMeRequest(session);
	    }
	    
	    return view;
	}
	
	private void createTrip(View view){
		
	    Log.e("createTrip", "create trip");
		
		Intent intent = new Intent(HomeFragment.this.getActivity(), MapActivity.class);
		startActivity(intent);
	}
	
	//First, create a private method that requests the user's data
	private void makeMeRequest(final Session session) {
	    // Make an API call to get user data and define a 
	    // new callback to handle the response.
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                    // Set the id for the ProfilePictureView
	                    // view that in turn displays the profile picture.
	                    profilePictureView.setProfileId(user.getId());
	                    // Set the Textview's text to the user's name.
	                    userNameView.setText(user.getName());
	                }
	            }
	            if (response.getError() != null) {
	                // Handle errors, will do so later.
	        	    Log.e("makeMeRequest", response.getError().toString());
	            }
	        }
	    });
	    request.executeAsync();
	} 
	
	//Next, define a private method that will respond to session changes and 
	//call the makeMeRequest() method if the session's open
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
	    if (session != null && session.isOpened()) {
	        // Get the user's data.
	        makeMeRequest(session);
	    }
	}
	
	//call the corresponding UiLifecycleHelper method if the REAUTH_ACTIVITY_CODE request code is passed in
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REAUTH_ACTIVITY_CODE) {
	        uiHelper.onActivityResult(requestCode, resultCode, data);
	    }
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

