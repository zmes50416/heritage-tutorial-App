package edu.ncue.im;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.ncue.test.jls.R;

public class SplashFragment extends Fragment {
	
	private static final String TAG = "SelectionFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.splash, 
	            container, false);
	    return view;
	}
}
