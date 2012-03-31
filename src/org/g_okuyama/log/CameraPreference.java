package org.g_okuyama.log;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class CameraPreference extends PreferenceActivity implements OnPreferenceChangeListener{
    public static final String TAG = "ContShooting";

    public static final String DEFAULT_SHOOT_NUM = "0";
    public static final String DEFAULT_INTERVAL = "0";
    
    static final int COLOR_EFFECT = 1;
    static final int SCENE_MODE = 2;
    static final int WHITE_BALANCE = 3;
    static final int PICTURE_SIZE = 4;
    static final int SHOOT_NUM = 5;
    static final int INTERVAL = 6;
    static String[] sSizeList = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.camera_preference);
        
        Bundle extras = getIntent().getExtras();
        String[] effectList = null;
        String[] whiteList = null;
        String[] sceneList = null;
        if(extras != null){
            effectList = extras.getStringArray("effect");
            sSizeList = extras.getStringArray("size");
        }

        //色合い
        ListPreference colorPref = (ListPreference)this.findPreference("color_effect");
        if(effectList != null){
        	colorPref.setOnPreferenceChangeListener(this);
        	colorPref.setSummary(getCurrentEffect(this));
        	colorPref.setEntries(effectList);
        	colorPref.setEntryValues(effectList);
        }
        else{
        	colorPref.setEnabled(false);
        }
        
        //画像サイズ
        ListPreference sizePref = (ListPreference)this.findPreference("picture_size");
        String size = getCurrentPictureSize(this);
        if(sSizeList != null){
            sizePref.setOnPreferenceChangeListener(this);
            if(!size.equals("0")){
                sizePref.setSummary(size);
            }
            else{
                sizePref.setSummary(getString(R.string.picture_size_summary));                
            }
            sizePref.setEntries(sSizeList);

            /*
            String[] valueList = new String[sSizeList.length];
            for(int i=0; i<sSizeList.length; i++){
                valueList[i] = String.valueOf(i);
            }
            Log.d(TAG, "size = " + valueList);
            */
            sizePref.setEntryValues(sSizeList);
        }
        else{
            sizePref.setEnabled(false);
        }
}
    
    public static String getCurrentEffect(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString("color_effect", /*default*/Camera.Parameters.EFFECT_NONE);
    }

    public static String getCurrentPictureSize(Context c){
    	return /*String str = */PreferenceManager.getDefaultSharedPreferences(c)
    			.getString("picture_size", /*default*/"0");

    }

	public boolean onPreferenceChange(Preference pref, Object newValue) {
		final CharSequence value = (CharSequence)newValue;
		if(value == null){
			return false;
		}
		
		if(pref.getKey().equals("color_effect")){
		    //選択されたら、設定画面を終了し、即反映させる
            Intent intent = new Intent();
            intent.putExtra("effect", value);
            this.setResult(COLOR_EFFECT, intent);
            finish();
		}

		else if(pref.getKey().equals("picture_size")){
            Intent intent = new Intent();
            
            for(int i=0; i<sSizeList.length; i++){
            	if(sSizeList[i].equals(value)){
                    intent.putExtra("size", i);
                    
                    //Log.d(TAG, "result data = " + i);
                    
                    break;
            	}
            }
            this.setResult(PICTURE_SIZE, intent);
            finish();
		}
		
		return true;
	}
}
