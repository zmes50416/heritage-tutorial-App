package edu.ncue.im;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
	private static String DB_PATH = "/data/data/edu.ncue.test.jls/databases/";
	private static String DB_NAME = "poidb";
	private SQLiteDatabase myDataBase;
	private final Context mContext;
	private DataBaseHelper mDataBaseHelper; 
	
	public DataBaseHelper(Context context){
		super(context, DB_NAME, null, 1);
		this.mContext = context;
	}
	
	public void createDataBase() throws IOException{
		boolean dbExist = checkDataBase();
		
		if(dbExist){
			//donothing
		}else{
			this.getReadableDatabase();
			try{
				copyDataBase();
			}catch(IOException e){
				e.printStackTrace();
				throw new Error("Error copying DB");
			}
		}
	}
	
	private boolean checkDataBase(){
		
		SQLiteDatabase checkDB = null;
		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		}catch(SQLiteException e){
			
		}
		
		if(checkDB != null){
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}
	
	private void copyDataBase() throws IOException{
		InputStream myInput = mContext.getAssets().open(DB_NAME);
		
		String outFileName = DB_PATH + DB_NAME;
		
		OutputStream myOutput = new FileOutputStream(outFileName);
		
		byte[] buffer = new byte[1024];
		int length;
		while((length = myInput.read(buffer)) > 0){
			myOutput.write(buffer, 0, length);
		}
		
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public void openDataBase() throws SQLiteException{
		String mPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
	}
	
	@Override
	public synchronized void close(){
		if(myDataBase != null)
			myDataBase.close();
		super.close();
	}
	
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	public Cursor getAll(){
		//String[] projection = { "_id","title","picURL"};
		
		Cursor c = myDataBase.rawQuery("SELECT * FROM poidb", null);
		return c;
	}
	
	public Cursor getOne(int id){
		Cursor c = myDataBase.rawQuery("SELECT * FROM poidb WHERE _id ="+id, null);
		return c;
	}

}
