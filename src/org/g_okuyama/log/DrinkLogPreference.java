package org.g_okuyama.log;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

public class DrinkLogPreference extends PreferenceActivity implements OnPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preference);
        
        Preference pref = findPreference("auth_cancel");
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0) {
                new AlertDialog.Builder(DrinkLogPreference.this)
                .setTitle(R.string.pref_dialog_auth_title)
                .setMessage(R.string.pref_dialog_auth_confirm)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //îFèÿâèú
                        SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token", null);
                        editor.putString("tokenSecret", null);
                        editor.commit();
                    }
                })
                .setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //âΩÇ‡ÇµÇ»Ç¢
                        return;
                    }
                })
                .show();
                return false;
            }
        });
    }

    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        return false;
    }
    
    public static boolean isAttached(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean("image_attached", true);
    }

    public static boolean isAutoTweet(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean("auto_tweet", false);
    }
}
