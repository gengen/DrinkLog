package org.g_okuyama.log;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class DrinkLogPreference extends PreferenceActivity implements OnPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preference);
    }

    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        return false;
    }
    
    public static boolean isAttached(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean("image_attached", true);
    }
}
