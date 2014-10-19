package com.mobile.projectmobile;

import java.util.Arrays;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.widget.LoginButton;

public class FBLoggedOutHomeFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
	    super.onCreateView(inflater, container, savedInstanceState);
	    
	    View view = inflater.inflate(R.layout.fragment_home_fb_logged_out, container, false);

	    LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
	    
	    Log.e("log in", "login button");
	    
	    loginButton.setReadPermissions(Arrays.asList("user_friends"));
	    
	    return view;
		
	}

}

