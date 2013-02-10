package edu.ncue.im;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import edu.ncue.test.jls.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FBSettingActivity extends FragmentActivity {
	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	private static final int FRAGMENT_COUNT = SELECTION +1;
	
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	private boolean isResumed = false;
	
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = 
	    new Session.StatusCallback() {
	    @Override
	    public void call(Session session, 
	            SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
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
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.fbsetting);
		 
		 FragmentManager fm = getSupportFragmentManager();
		 fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
		 fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
		 
		 FragmentTransaction transaction = fm.beginTransaction();
		    for(int i = 0; i < fragments.length; i++) {
		        transaction.hide(fragments[i]);
		    }
		    transaction.commit();
		    
		    uiHelper = new UiLifecycleHelper(this, callback);
		    uiHelper.onCreate(savedInstanceState);
	
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
	            showFragment(SELECTION, false);
	        } else if (state.isClosed()) {
	            // If the session state is closed:
	            // Show the login fragment
	            showFragment(SPLASH, false);
	        }
	    }
	}

	@Override
	protected void onResumeFragments() {
	    super.onResumeFragments();
	    Session session = Session.getActiveSession();

	    if (session != null && session.isOpened()) {
	        // if the session is already open,
	        // try to show the selection fragment
	        showFragment(SELECTION, false);
	    } else {
	        // otherwise present the splash screen
	        // and ask the user to login.
	        showFragment(SPLASH, false);
	    }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		this.uiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
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
		
}

	
