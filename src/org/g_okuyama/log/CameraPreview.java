package org.g_okuyama.log;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

class CameraPreview implements SurfaceHolder.Callback {
    public static final String TAG = "ContShooting";
    Camera mCamera = null;
    Context mContext = null;

    AutoFocusCallback mFocus = null;

    private Size mSize = null;
    private List<Size> mSupportList = null;
    //サポートリストに対する端末の下限値のインデックス
    private int mOffset = 0;
    private File mFile = null;
    PreviewCallback mPreviewCallback = null;
    
    //for setting
	private boolean mSetColor = false;
	private boolean mSetSize = false;
	private String mSetValue = null;
	private int mSetInt = 0;
	private int mPicIdx = 0;
	
	//初期設定
	private String mEffect = null;
	private String mSizeStr = null;
	
    CameraPreview(Context context){
        mContext = context;
	}
	
	public void setField(String effect, String size){
        mEffect = effect;
        //mPicIdx = size;
        mSizeStr = size;
	}
    
    public void surfaceCreated(SurfaceHolder holder) {
    	if(mCamera == null){
    	    try{
                mCamera = Camera.open();
    	        
    	    }catch(RuntimeException e){
    	        new AlertDialog.Builder(mContext)
    	        .setTitle(R.string.sc_error_title)
    	        .setMessage(mContext.getString(R.string.sc_error_cam))
    	        .setPositiveButton(R.string.sc_error_cam_ok, new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	            	((CameraActivity)mContext).finish();
    	            }
    	        })
    	        .show();
    	            
    	        try {
    	            this.finalize();
    	        } catch (Throwable t) {
	            	((CameraActivity)mContext).finish();               
    	        }
    	        return;
    	    }
    	    
    	    mCamera.setDisplayOrientation(90); 
    	}
    	
    	if(mSupportList == null){
    	    createSupportList();
    	}

    	try {
            mCamera.setPreviewDisplay(holder);               
        } catch (IOException e) {
            Log.e(TAG, "IOException in surfaceCreated");
            mCamera.release();
            mCamera = null;
        }
    }
    
    private void createSupportList(){
        Camera.Parameters params = mCamera.getParameters();
        mSupportList = Reflect.getSupportedPreviewSizes(params);
           
        if (mSupportList != null && mSupportList.size() > 0) {
            //降順にソート
            Collections.sort(mSupportList, new PreviewComparator());
            
            /*
            for(int i = 0; i < mSupportList.size(); i++){
                if(mSupportList.get(i).width > mWidth){
                    continue;
                }
                
                if(mSupportList.get(i).height > mHeight){
                    continue;
                }
                
                mSize = mSupportList.get(i);
                mOffset = i;
                break;
            }
            */

            //if(mSize == null){
                mSize = mSupportList.get(0);
                mOffset = 0;
            //}
        }
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
    	release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        //Cameraがopen()できなかったとき用
        if(mCamera == null){
            return;
        }

        //止めないでsetParameters()するとエラーとなる場合があるため止める
        mCamera.stopPreview();
        
        //設定画面で設定したとき
        if(mSetValue != null){
        	if(mSetColor == true){
                mEffect = mSetValue;
        	}
        	else if(mSetSize == true){
                mPicIdx = mSetInt;
                mSizeStr = getSizeList().get(mPicIdx);
        	}
        	
            mSetValue = null;
            mSetColor = false;
            mSetSize = false;
        }
        //設定画面で設定しないとき
        else{
            List<String> list = getSizeList();
            for(int i = 0; i<list.size(); i++){
                if(list.get(i).equals(mSizeStr)){
                    mPicIdx = i;
                }
                //mSizeStrが"0"のときはmPicIdxに値が設定されずに抜ける(=0になる)
            }
        }
        
        setAllParameters();

        //mPreviewCallback = new PreviewCallback(this);
        mCamera.startPreview();
        //focus
        mFocus = new AutoFocusCallback(){
            public void onAutoFocus(boolean success, Camera camera) {
                mPreviewCallback = new PreviewCallback(CameraPreview.this);
            }
        };
        try{
            mCamera.autoFocus(mFocus);
        }catch(Exception e){
            mPreviewCallback = new PreviewCallback(CameraPreview.this);            
        }
    }
    
    private void setAllParameters(){
        Camera.Parameters param = mCamera.getParameters();

        //一度に複数のパラメータを設定すると落ちる端末があるため、1つずつ設定する
        try{
            param.setColorEffect(mEffect);            
            mCamera.setParameters(param);                
        }catch(Exception e){
            param = mCamera.getParameters();
        }

        try{
        	int i = mOffset + mPicIdx;
            mSize = mSupportList.get(mOffset + mPicIdx); 
            param.setPreviewSize(mSize.width, mSize.height);
            mCamera.setParameters(param);
        }catch(Exception e){
            //nothing to do
        }
    }
    
    public void resumePreview(){
    	if(mPreviewCallback != null){
    		if(mCamera != null){
    			mCamera.startPreview();
    			mCamera.setPreviewCallback(mPreviewCallback);
    		}
    	}
    }
    
    List<String> getEffectList(){
        Camera.Parameters param = mCamera.getParameters();
        return param.getSupportedColorEffects();
    }
    
    List<String> getSizeList(){
    	List<String> list = new ArrayList<String>();
    	for(int i = mOffset; i<mSupportList.size(); i++){
    		//String size = mSupportList.get(i).width + "x" + mSupportList.get(i).height;
    		String size = mSupportList.get(i).height + "x" + mSupportList.get(i).width;
    		list.add(size);
    	}
    	return list;
    }
    
    void setColorValue(String value){
    	mSetColor = true;
    	mSetValue = value;
    }
    
    void setSizeValue(int value){
    	mSetSize = true;
    	mSetInt = value;
    	//この後のsurfaceChangedで設定する用のマークを付ける
    	mSetValue = "hoge";
    }

    void doAutoFocus(){
        mCamera.setPreviewCallback(null);
    	if(mCamera != null && mFocus != null){
    		try{
    			mCamera.autoFocus(mFocus);
    		}catch(Exception e){
    			mPreviewCallback = new PreviewCallback(CameraPreview.this);            
    		}
    	}
    }

    void release(){
        if(mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    class PreviewComparator implements java.util.Comparator {
    	public int compare(Object s, Object t) {
    		//降順
    		return ((Size) t).width - ((Size) s).width;
    	}
    }
    
    public class PreviewCallback implements Camera.PreviewCallback {
        private CameraPreview mPreview = null;

        PreviewCallback(CameraPreview preview){
            mPreview = preview;
        }

        public void onPreviewFrame(byte[] data, Camera camera) {
            //一旦コールバックを止める
        	camera.setPreviewCallback(null);

            //convert to "real" preview size. not size setting before.
            Size size = convertPreviewSize(data);

            final int width = size.width;
            final int height = size.height;            
            int[] rgb = new int[(width * height)];

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            decodeYUV420SP(rgb, data, width, height);
            bmp.setPixels(rgb, 0, width, 0, 0, width, height);
            
            //回転
            Matrix matrix = new Matrix();
            // 回転させる角度を指定
            matrix.postRotate(90.0f);   
            Bitmap bmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp = null;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bmp2.compress(Bitmap.CompressFormat.JPEG, 100, out);
            
            String path = savedata(out.toByteArray());
            
            release();
            
            Intent intent = new Intent();
            if(path != null){
            	Uri uri = Uri.parse("file://" + path);
            	intent.setData(uri);
            }
            ((CameraActivity)mContext).setResult(1, intent);
            ((CameraActivity)mContext).finish();
       }
        
        private Size convertPreviewSize(byte[] data){
            double displaysize = data.length / 1.5;
            Size size;
            int x, y;
            
            for(int i=0; i<mSupportList.size(); i++){
                size = mSupportList.get(i);
                x = size.width;
                y = size.height;
                if((x*y) == displaysize){
                    return size;
                }
            }
            return null;
        }
        
        public String savedata(byte[] data){
        	if(mFile == null){
        		mFile = new File(Environment.getExternalStorageDirectory(), "/DrinkLog");
        	}

            FileOutputStream fos = null;
            File savefile = null;
            String datastr = getCurrentDate();
            try{
                if(mFile.exists() == false){
                    mFile.mkdir();
                }
                savefile = new File(mFile.getPath(), datastr + ".jpg");
                fos = new FileOutputStream(savefile);
                fos.write(data);
                fos.flush();
                fos.close();
            }catch(IOException e){
                Log.e(TAG, "IOException in savedata");
                if(fos != null){
                    try {
                        fos.close();
                    } catch (IOException e1) {
                        //do nothing
                    }
                }
                return null;
            }
            
            return savefile.getPath();
        } 
        
        // YUV420 to BMP 
        public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) { 
            final int frameSize = width * height; 

            for (int j = 0, yp = 0; j < height; j++) { 
                int uvp = frameSize + (j >> 1) * width, u = 0, v = 0; 
                for (int i = 0; i < width; i++, yp++) { 
                    int y = (0xff & ((int) yuv420sp[yp])) - 16; 
                    if (y < 0) y = 0; 
                    if ((i & 1) == 0) { 
                            v = (0xff & yuv420sp[uvp++]) - 128; 
                            u = (0xff & yuv420sp[uvp++]) - 128; 
                    } 

                    int y1192 = 1192 * y; 
                    int r = (y1192 + 1634 * v); 
                    int g = (y1192 - 833 * v - 400 * u); 
                    int b = (y1192 + 2066 * u); 

                    if (r < 0) r = 0; else if (r > 262143) r = 262143; 
                    if (g < 0) g = 0; else if (g > 262143) g = 262143; 
                    if (b < 0) b = 0; else if (b > 262143) b = 262143; 

                    rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff); 
                } 
            }
        }
        
        String getCurrentDate(){
            Calendar cal1 = Calendar.getInstance();
            int year = cal1.get(Calendar.YEAR);
            int mon = cal1.get(Calendar.MONTH) + 1;
            int d = cal1.get(Calendar.DATE);
            int h = cal1.get(Calendar.HOUR_OF_DAY);
            int min = cal1.get(Calendar.MINUTE);
            int sec = cal1.get(Calendar.SECOND);
            int msec = cal1.get(Calendar.MILLISECOND);
            
            String month = Integer.toString(mon);
            if(month.length() == 1){
                month = "0" + month;
            }
            String day = Integer.toString(d);
            if(day.length() == 1){
                day = "0" + day;
            }
            String hour = Integer.toString(h);
            if(hour.length() == 1){
                hour = "0" + hour;
            }
            String minute = Integer.toString(min); 
            if(minute.length() == 1){
                minute = "0" + minute;
            }
            String second = Integer.toString(sec);
            if(second.length() == 1){
                second = "0" + second;
            }
            String millisecond = Integer.toString(msec);
            if(millisecond.length() == 1){
                millisecond = "00" + millisecond;
            }
            else if(millisecond.length() == 2){
                millisecond = "0" + millisecond;
            }            

            return Integer.toString(year) + month + day + hour + minute + second + millisecond;
        }
    }
}