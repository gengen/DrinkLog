package org.g_okuyama.log;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class LogDetailActivity extends Activity {
	public static final String TAG = "DrinkLog";
	
	int mCategory;
	String mName;
	String mImageURL;
	float mRate;
	String mComment;
	String mYear;
	String mType;
	String mArea;
	String mDate;
	String mPlace;
	String mPrice;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        getDetailData(extras.getInt("dbid"));
        
        setContentView(R.layout.reference);
        setLayout();
    }
    
    private void getDetailData(int dbid){
    	//DBを取得
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	String query = "select * from logtable where rowid = ?;";
    	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid)});

    	c.moveToFirst();
    	String cat = c.getString(1);
    	mCategory = Integer.valueOf(cat);
		mName = c.getString(2);
		mImageURL = c.getString(3);
		String rate = c.getString(4);
		mRate = Float.valueOf(rate);
		mComment = c.getString(5);
		mYear = c.getString(6);
		mType = c.getString(7);
		mArea = c.getString(8);
		mDate = c.getString(9);
		mPlace = c.getString(10);
		mPrice = c.getString(11);
		
		c.close();
    }
    
    private void setLayout(){
    	setName();
    	setImage();
    	setRate();
    	setComment();
    	setOtherLayout();
    }
    
    private void setName(){
    	TextView name = (TextView)findViewById(R.id.ref_name);
    	name.setText(mName);
    }
    
    private void setImage(){
    	ImageView image = (ImageView)findViewById(R.id.ref_image);
    	if(mImageURL.equals("none")){
    		switch(mCategory){
    		case DrinkLogActivity.CATEGORY_WHISKEY:
    			image.setImageResource(R.drawable.whiskey);
                break;
                
            case DrinkLogActivity.CATEGORY_COCKTAIL:
    			image.setImageResource(R.drawable.cocktail);
                break;
                
            case DrinkLogActivity.CATEGORY_WINE:
    			image.setImageResource(R.drawable.wine);
                break;

            case DrinkLogActivity.CATEGORY_SHOCHU:
    			image.setImageResource(R.drawable.shochu);
                break;

            case DrinkLogActivity.CATEGORY_SAKE:
    			image.setImageResource(R.drawable.sake);
                break;

            case DrinkLogActivity.CATEGORY_BRANDY:
    			image.setImageResource(R.drawable.brandy);
                break;

            case DrinkLogActivity.CATEGORY_BEER:
    			image.setImageResource(R.drawable.beer);
                break;

            case DrinkLogActivity.CATEGORY_OTHER:
    			image.setImageResource(R.drawable.other);
                break;

            default:
                Log.e(TAG, "Unknown Category");
                break;
    		}
    	}
    	else{
    		Log.d(TAG, "URL = " + mImageURL);
    		Uri uri = Uri.parse(mImageURL);
            image.setImageBitmap(RegisterActivity.uri2bmp(this, uri, 160, 120));
    	}
    }
    
    private void setRate(){
    	RatingBar rate = (RatingBar)findViewById(R.id.ref_rating);
    	rate.setRating(mRate);
    }
    
    private void setComment(){
    	TextView comment = (TextView)findViewById(R.id.ref_impression);
    	if(mComment.equals("none")){
    		comment.setText(getString(R.string.ref_no_comment));
    	}
    	else{
    		comment.setText(mComment);
    	}
    }
    
    private void setOtherLayout(){
        final LinearLayout otherLayout = (LinearLayout)findViewById(R.id.ref_other_display_layout);
        Button other = (Button)findViewById(R.id.ref_other);
        other.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                //「その他」ボタンのレイアウトを非表示に
                otherLayout.setVisibility(View.GONE);

                LinearLayout linear = (LinearLayout)findViewById(R.id.ref_linearLayout1);
                final LayoutInflater mInflater;
                mInflater = (LayoutInflater)LogDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mInflater.inflate(R.layout.reference_add, linear);
                
                setOtherLog();
            }
        });
    }
    
    private void setOtherLog(){
    	/*
        android:id="@+id/ref_year"
       	android:id="@+id/ref_area"
        android:id="@+id/ref_date"
        android:id="@+id/ref_place"
        android:id="@+id/ref_price"
        */
    	
    	
    }
}
