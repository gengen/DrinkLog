package org.g_okuyama.log;

import android.graphics.Bitmap;

public class LogListData {
	private Bitmap mBitmap = null;
	private String mName = null;
	private float mRate = 0;
	
	public void setImageData(Bitmap bitmap){
		mBitmap = bitmap;
	}
	
	public Bitmap getImageData(){
		return mBitmap;
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
