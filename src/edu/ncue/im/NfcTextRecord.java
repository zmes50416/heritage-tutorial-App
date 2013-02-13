package edu.ncue.im;

import java.io.UnsupportedEncodingException;

import android.nfc.NdefRecord;

public class NfcTextRecord{
	private final String mLanguageCode;
	private final String mText;
	
	private NfcTextRecord(String languageCode, String text){
		this.mLanguageCode = languageCode;
		this.mText = text;
	}
	
	public String getText(){
		return mText;
	}
	
	public String getLanguageCode(){
		return mLanguageCode;
	}
	
	
	public static NfcTextRecord parse(NdefRecord record){
		try{
			byte[] payload = record.getPayload();
			
			String textEncoding = ((payload[0]& 0200) == 0) ? "UTF-8" : "UTF-16";
			int languageCodeLength = payload[0] & 0077;
			String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			String text = new String(payload, languageCodeLength +1,payload.length - languageCodeLength -1, textEncoding);
			return new NfcTextRecord(languageCode, text);
			
		}catch(UnsupportedEncodingException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public static boolean isText(NdefRecord record){
		try{
			parse(record);
			return true;
		} catch(IllegalArgumentException e){
			return false;
		}
	}
}
