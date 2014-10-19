package com.mobile.projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;


public class HomeActivity extends FragmentActivity {
    
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = 
        new Session.StatusCallback() {
        @Override
        public void call(Session session, 
                SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    // Fragment attributes
    private static final int FB_LOGGED_OUT_HOME = 0;
    private static final int HOME = 1;
    private static final int FRAGMENT_COUNT = HOME +1;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    
 	// Boolean recording whether the activity has been resumed so that
 	// the logic in onSessionStateChange is only executed if this is the case
 	private boolean isResumed = false;
 	
 	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
		
		FragmentManager fm = getSupportFragmentManager();
		
		//Fragments are already defined in home.xml
        fragments[FB_LOGGED_OUT_HOME] = fm.findFragmentById(R.id.fbLoggedOutHomeFragment);
        fragments[HOME] = fm.findFragmentById(R.id.homeFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for(int i = 0; i < fragments.length; i++) {
        	//Hides an existing fragment. This is only relevant for fragments 
        	//whose views have been added to a container, as this will cause the view to be hidden.
            transaction.hide(fragments[i]);
        }
        transaction.commit();        
		
    }
 	
 	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		uiHelper.onActivityResult(requestCode, resultCode, data);
    }
	
 	@Override
    protected void onResumeFragments() {
		super.onResumeFragments();

		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			showFragment(HOME, false);
		} else {
			showFragment(FB_LOGGED_OUT_HOME, false);
		}
		
    }
	
	@Override
    public void onResume() {
        super.onResume();
        
        uiHelper.onResume();
        
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        uiHelper.onPause();

        isResumed = false;
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        
		/* This saves the session in case the application is destroyed by the system. */
		uiHelper.onSaveInstanceState(outState);
		
	}
	
	@Override
    public void onDestroy() {
 		super.onDestroy();

 		/* This removes the broadcast receiver that was added in onCreate(). */
 		uiHelper.onDestroy();

    }
	
	
    private void showFragment(int fragmentIndex, boolean addToBackStack) {
    	
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	
        // Only make changes if the activity is visible
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            
            // Get the number of entries in the back stack
            int backStackSize = manager.getBackStackEntryCount();
            // Clear the back stack
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            
            if (state.isOpened()) {
                // If the session state is open:
                // Show the authenticated fragment
                showFragment(HOME, false);
                
            } else if (state.isClosed()) {
                // If the session state is closed:
                // Show the login fragment
                showFragment(FB_LOGGED_OUT_HOME, false);
            }
        }
    }
    
}
