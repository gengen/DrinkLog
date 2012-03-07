package org.g_okuyama.log;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

public class ImageViewActivity extends Activity {
    public static final String TAG = "DrinkLog";
    
    String mUrl = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageview);
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        Bundle extra = getIntent().getExtras();
        mUrl = extra.getString("imageurl");
        setTitle(extra.getString("name"));
        
        setLayout();
    }
    
    private void setLayout(){
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        int width = disp.getWidth();
        int height = disp.getHeight();
        
        Uri uri = Uri.parse(mUrl);
        ImageView image = (ImageView)findViewById(R.id.imageview);
        image.setImageBitmap(RegisterActivity.uri2bmp(this, uri, width, height));
        
        image.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                //TODO:ÉäÉ\Å[ÉXâï˙
                finish();
            }
        });
    }
}
