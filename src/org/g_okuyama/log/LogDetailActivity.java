package org.g_okuyama.log;

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class LogDetailActivity extends Activity {
	public static final String TAG = "DrinkLog";
	private static final int MENU_EDIT = 0;
	private static final int MENU_DELETE = 1;
	private static final int MENU_SEARCH = 2;
	private static final int MENU_SHARE = 3;
	private static final int REQUEST_EDIT = 6666;	
	private static final int RESPONSE_DELETE = 7777;	
	private static final int RESPONSE_EDIT = 6667;	
	
	private int mDBID;
	
	int mCategory;
	String mName;
	String mImageURL;
	float mRate;
	String mComment;
	String mVintage;
	String mType;
	String mArea;
	String mDate;
	String mPlace;
	String mPrice;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        mDBID = extras.getInt("dbid");
        
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
		mVintage = c.getString(6);
		mType = c.getString(7);
		mArea = c.getString(8);
		mDate = c.getString(9);
		mPlace = c.getString(10);
		mPrice = c.getString(11);
		
		c.close();
    }
    
    private void setLayout(){
       	getDetailData(mDBID);
    	setName();
    	setImage();
    	setRate();
    	setComment();
    	setTweet();
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
    
    private void setTweet(){
        /*
        Button tweetBtn = (Button)findViewById(R.id.ref_tweet);
        tweetBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                share();
            }
        });
        */
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
    	TextView year = (TextView)findViewById(R.id.ref_year);
    	if(mVintage.equals("none")){
    		year.setText(getString(R.string.ref_no_setting));
    	}
    	else{
    		year.setText(mVintage);
    	}
    	
    	TextView area = (TextView)findViewById(R.id.ref_area);
    	if(mArea.equals("none")){
    		area.setText(getString(R.string.ref_no_setting));	
    	}
    	else{
    		area.setText(mArea);
    	}
    	
    	TextView date = (TextView)findViewById(R.id.ref_date);
    	//TODO:英語表記入れる
    	date.setText(mDate);
    	
    	TextView place = (TextView)findViewById(R.id.ref_place);
    	if(mPlace.equals("none")){
    		place.setText(getString(R.string.ref_no_setting));
    	}
    	else{
    		place.setText(mPlace);
    	}
    	
    	TextView price = (TextView)findViewById(R.id.ref_price);
    	if(mPrice.equals("none")){
    		price.setText(getString(R.string.ref_no_setting));
    	}
    	else{
    		price.setText(mPrice);
    	}
    }
    
    /*
     * オプションメニューの作成
     * ・編集
     * ・削除
     * ・検索(Wikipedia)
     * ・投稿(twitter)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem edit = menu.add(0, MENU_EDIT, 0 ,R.string.menu_edit);
        edit.setIcon(android.R.drawable.ic_menu_edit);

        MenuItem delete = menu.add(0, MENU_DELETE, 0 ,R.string.menu_delete);
        delete.setIcon(android.R.drawable.ic_menu_delete);

        MenuItem search = menu.add(0, MENU_SEARCH, 0 ,R.string.menu_search);
        search.setIcon(android.R.drawable.ic_menu_search);
        
        MenuItem share = menu.add(0, MENU_SHARE, 0 ,R.string.menu_share);
        share.setIcon(android.R.drawable.ic_menu_share);

        return true;
    }
    
    //オプションメニュー選択時のリスナ
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_EDIT:
    		edit();
    		break;
    		
    	case MENU_DELETE:
    		delete();
    		break;
    		
    	case MENU_SEARCH:
    		search();
    		break;
    		
    	case MENU_SHARE:
    		share();
    		break;

    	default:
    		//何もしない
    	}

    	return true;
    }
    
    private void edit(){
    	Intent intent = new Intent(this, RegisterActivity.class);
    	intent.putExtra("category", mCategory);
    	intent.putExtra("from", "LogDetailActivity");
    	intent.putExtra("dbid", mDBID);
    	startActivityForResult(intent, REQUEST_EDIT);
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode == REQUEST_EDIT){
    		if(resultCode == RESPONSE_EDIT){
    			//編集後のログを表示
    			setLayout();
    		}
    	}
    }
    
    private void delete(){
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.cancel_confirm_title)
    	.setMessage(getString(R.string.list_alert_confirm))
    	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    	DatabaseHelper helper = new DatabaseHelper(LogDetailActivity.this);
		    	SQLiteDatabase db = helper.getWritableDatabase();
		    	db.delete("logtable", "rowid = ?", new String[]{Integer.toString(mDBID)});
		    	//前画面のActivityに戻る
	            Intent intent = new Intent();
	            setResult(RESPONSE_DELETE, intent);
		    	finish();
			}
		})
		.setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
			}
		})
		.show();
    }
    
    private void search(){
    	
    }
    
    private void share(){
    	Intent intent = new Intent(this, TwitterActivity.class);
    	intent.putExtra("name", mName);
    	intent.putExtra("rate", String.valueOf(mRate));
    	intent.putExtra("comment", mComment);
    	startActivity(intent);
    }
}
