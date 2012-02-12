package org.g_okuyama.log;

public class LogListData {
	private int mDBID;
	private int mImageID;
	private String mName = null;
	private float mRate = 0;
	
	public void setDBID(int id){
		mDBID = id;
	}
	
	public int getDBID(){
		return mDBID;
	}
	
	public void setImageID(int id){
		mImageID = id;
	}
	
	public int getImageID(){
		return mImageID;
	}
	
	public void setTextData(String name){
		mName = name;
	}
	
	public String getTextData(){
		return mName;
	}
	
	public void setRatingData(float rate){
		mRate = rate;
	}
	
	public float getRatingData(){
		return mRate;
	}
}
