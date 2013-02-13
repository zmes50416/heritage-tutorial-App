package edu.ncue.im;

import edu.ncue.test.jls.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class NfcPushInActivity extends Activity {
	
	private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private TextView testTV;
    private String mText; 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfcpushin);
		testTV = (TextView) this.findViewById(R.id.testContentTextView);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(mNfcAdapter == null){	//check nfcfunction is there or not
			Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		else{	//check nfc function have turn on or not
			if(!mNfcAdapter.isEnabled()){
				Toast.makeText(this, "NFC is not turn on", Toast.LENGTH_SHORT).show();
			}
				
		}
		//ForgegroundDispatch 
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try{
			ndef.addDataType("*/*");
		}catch(MalformedMimeTypeException e){
			throw new RuntimeException("fail", e);
			
		}
		mFilters = new IntentFilter[] {
				ndef,
		};
		
		mTechLists = new String[][]{ new String[]{NfcF.class.getName()} };
	}
	//must disable ForgegroundDispatch when onPause
    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                mTechLists);
    }
    
    //NFC Tag data come in onNewIntent
    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        mText= "Tag not found";
        processIntent(intent);
        this.testTV.setText("content:"+ mText);
    }
    
    //process Tag
    public void processIntent(Intent intent){
    	Tag mTag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndefTag = Ndef.get(mTag);
        int size = ndefTag.getMaxSize();
        String type = ndefTag.getType();
        NdefMessage ndefMesg = ndefTag.getCachedNdefMessage();
        if(ndefMesg == null){
        	Toast.makeText(this, "Tag are not correctly read", Toast.LENGTH_SHORT).show();
        	return;
        }
        NdefRecord[] ndefRecords = ndefMesg.getRecords();
        
        
        int len = ndefRecords.length;
        Log.d("NFC","record Length:"+len);
        for(int i = 0;i<len;i++){
        	 NfcTextRecord nfcTextRecord = NfcTextRecord.parse(ndefRecords[i]);
        	Toast.makeText(this, nfcTextRecord.getText(), Toast.LENGTH_SHORT);
        	mText = nfcTextRecord.getText();
        }
    }	

}
