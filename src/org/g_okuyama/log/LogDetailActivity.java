package org.g_okuyama.log;

import android.app.Activity;
import android.os.Bundle;

public class LogDetailActivity extends Activity {
	public static final String TAG = "DrinkLog";
	
	private int mDBID;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        mDBID = extras.getInt("dbid");
        
        setContentView(R.layout.reference);
        setLayout();
    }
    
    private void setLayout(){
    	
    }
}
