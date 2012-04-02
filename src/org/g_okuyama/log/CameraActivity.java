package org.g_okuyama.log;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class CameraActivity extends Activity {
    private static final String TAG = "DrinkLog";
    
    static final int MENU_DISP_SETTING = 1;

    static final int REQUEST_CODE = 1;
    static final int RESPONSE_COLOR_EFFECT = 1;
    static final int RESPONSE_PICTURE_SIZE = 2;

    SurfaceHolder mHolder;
    private CameraPreview mPreview = null;
    
    private ContentResolver mResolver;
        
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera);
        
        mResolver = getContentResolver();
        
        //設定値の取得
        String effect = CameraPreference.getCurrentEffect(this);
        String size = CameraPreference.getCurrentPictureSize(this);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
        mHolder = sv.getHolder();

        mPreview = new CameraPreview(this);
        mPreview.setField(effect, size);
        mHolder.addCallback(mPreview);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //register UI Listener
    	setListener();        
    }
    
    private void setListener(){
        ImageButton shot = (ImageButton)findViewById(R.id.imgbtn);
        shot.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mPreview != null){
						mPreview.resumePreview();
				}
			}
        });
        
        ImageButton focus = (ImageButton)findViewById(R.id.focusbtn);
        focus.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mPreview != null){
						mPreview.doAutoFocus();
				}
			}
        });
    }
    
    public void onStart(){
        super.onStart();
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            new AlertDialog.Builder(this)
            .setTitle(R.string.sc_alert_title)
            .setMessage(getString(R.string.sc_alert_sd))
            .setPositiveButton(R.string.sc_alert_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(RESULT_OK);
                }
            })
            .show();
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //オプションメニュー(設定)
        MenuItem prefSetting = menu.add(0, MENU_DISP_SETTING, 0, R.string.menu_settings);
        prefSetting.setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }
    
    //オプションメニュー選択時のリスナ
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            
        case MENU_DISP_SETTING:
            displaySettings();
        	break;
            
        default:
            //何もしない
        }

        return true;
    }
    
    private void displaySettings(){
        Intent pref_intent = new Intent(this, CameraPreference.class);

        //色合い設定のリストを作成する
        List<String> effectList = null;
        if(mPreview != null){
            effectList = mPreview.getEffectList();
        }
        if(effectList != null){
            pref_intent.putExtra("effect", (String[])effectList.toArray(new String[0]));
        }
        
        //画像サイズ
        List<String> sizeList = null;
        if(mPreview != null){
            sizeList = mPreview.getSizeList();
        }
        if(sizeList != null){
            pref_intent.putExtra("size", (String[])sizeList.toArray(new String[0]));
        }
        
        startActivityForResult(pref_intent, REQUEST_CODE);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(data == null){
            return;
        }
        
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESPONSE_COLOR_EFFECT){
                if(mPreview != null){
                    mPreview.setColorValue(data.getStringExtra("effect"));
                }            	
            }

            if(resultCode == RESPONSE_PICTURE_SIZE){
                if(mPreview != null){
                    mPreview.setSizeValue(data.getIntExtra("size", 0));
                }
            }
        }
    }
    
    public static boolean deleteCache(File dir) {
        if(dir==null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteCache(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}