package org.g_okuyama.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import org.g_okuyama.log.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.Toast;

public class RegisterActivity extends Activity {
    public static final String TAG = "DrinkLog";
    public static final int RATING_STAR_NUM = 5;
    public static final float RATING_STEP = (float)0.5;
    public static final int REQUEST_CODE_GALLERY = 9999; 
    public static final int REQUEST_CODE_CAMERA = 9998;
    public static final int REQUEST_CODE_SILENT = 9997;
	public static final int RESPONSE_EDIT = 6667;	
    
    DatabaseHelper mHelper = null;
    File mSaveFile = null;
    String mCurDate = "unknown";
    int mCategory;
    boolean mEditFlag = false;
        
    int mYear;
    int mMonth;
    int mDay;

    boolean mPicFlag = false;
    //「その他」ボタンが押されたか？
    boolean mOtherFlag = false;
    
    //登録時の同時tweet用に名前、コメント、評価、パスはクラス変数にする
    String mName;
    String mComment;    
    float mRate;
    String mImageURL = null;
    
    //編集用にその他のログもクラス変数にする
    private int mDBID = -9876;
	String mVintage;
	String mType;
	String mArea;
	String mDate;
	String mPlace;
	String mPrice;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extra = getIntent().getExtras();
        mCategory = extra.getInt("category");
        String flag = extra.getString("from");
        if(flag.equals("LogDetailActivity")){
        	mEditFlag = true;
        	mDBID = extra.getInt("dbid");
        	getDBData(mDBID);
        }
        
        //自動でキーボードを出さないように設定
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);         
        setContentView(R.layout.register);

        mHelper = new DatabaseHelper(this);

        setLayout();
    }
    
    private void getDBData(int dbid){
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	String query = "select * from logtable where rowid = ?;";
    	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid)});

    	c.moveToFirst();
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
    	initDate();
        setName();
        setImage();
        setEvaluate();
        setComment();
        setOther();
        setRegister();
    }
    
    private void initDate(){
    	Calendar cal1 = Calendar.getInstance();
    	mYear = cal1.get(Calendar.YEAR);
    	mMonth = cal1.get(Calendar.MONTH);
    	mDay = cal1.get(Calendar.DATE);
    	mCurDate = getDate(mYear, mMonth, mDay);
    }
        
    private void setName(){
        AutoCompleteTextView view = (AutoCompleteTextView)findViewById(R.id.name);

        int id = getCategoryID(mCategory);
        if((id != -1) && !mEditFlag){
            /*
             * TODO:有料版で対応。少なくともワインは追加が必要
             */
            /*
            ArrayAdapter name_adapter = ArrayAdapter.createFromResource(
                    this, id, android.R.layout.simple_dropdown_item_1line);
            view.setAdapter(name_adapter);
            */
        }
        else if(mEditFlag){
        	//編集時は入力補完は必要ない
        	view.setText(mName);
        }

        //TODO:(option)登録されていない名前で登録された場合は、name_adapterに追加する
        
        view.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable edit) {
                String name = edit.toString();
                //TODO:名前から地域を特定<-余裕あったらでいい
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }
    
    private int getCategoryID(int idx){
        switch(idx){
            case DrinkLogActivity.CATEGORY_WHISKEY:
                return R.array.name_array_wh;
                
            case DrinkLogActivity.CATEGORY_COCKTAIL:
            	return R.array.name_array_co;
            	
                //TODO:多種類追加
            default:
                return -1;
        }
    }
    
    private void setImage(){
        final ImageView image = (ImageView)findViewById(R.id.cur_image);
        
        final String[] setting_list = getResources().getStringArray(R.array.image_setting_array);        
        ImageButton imageBtn = (ImageButton)findViewById(R.id.picture);
        imageBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                new AlertDialog.Builder(RegisterActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.dialog_image)
                .setItems(setting_list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            //camera
                            File file = new File(Environment.getExternalStorageDirectory(), "/DrinkLog");
                            if(file.exists() == false){
                                file.mkdir();
                            }
                            
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            mSaveFile = new File(file, String.valueOf(System.currentTimeMillis() + ".jpg"));
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mSaveFile));
                            startActivityForResult(intent, REQUEST_CODE_CAMERA);
                        }
                        else if(which == 1){
                        	//silent camera
                        	Intent intent = new Intent(RegisterActivity.this, CameraActivity.class);
                        	startActivityForResult(intent, REQUEST_CODE_SILENT);
                        }
                        else if(which == 2){
                            //gallery
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY);                        	
                        }
                        else{
                            //delete
                            //リンクのみ空にする
                            if(mEditFlag){
                                mImageURL = "none";                                
                            }
                            else{
                                mImageURL = null;
                            }
                            
                            //デフォルト画像表示
                            image.setImageResource(R.drawable.default_image);
                        }
                    }
                })
               .show();
            }
        });
        
        //デフォルト画像表示
        image.setImageResource(R.drawable.default_image);

        if(mEditFlag){
        	if(!mImageURL.equals("none")){
        		Uri uri = Uri.parse(mImageURL);
        		Bitmap bmp = RegisterActivity.uri2bmp(this, uri, 160, 120);
        		if(bmp != null){
        		    image.setImageBitmap(bmp);
        		}
        		else{
        	        image.setImageResource(R.drawable.default_image);        		    
        		}
        	}
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){        
        if(requestCode == REQUEST_CODE_GALLERY){
            if(data == null){
                return;
            }

            if(resultCode == RESULT_OK){
                ImageView image = (ImageView)findViewById(R.id.cur_image);
                Uri uri = data.getData();
                //保存用文字列
                mImageURL = uri.toString();
                
                Bitmap bmp = RegisterActivity.uri2bmp(this, uri, 160, 120);
                if(bmp != null){
                    image.setImageBitmap(bmp);
                }
                else{
                    image.setImageResource(R.drawable.default_image);
                }
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, R.string.user_canceled, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, R.string.get_image_error, Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQUEST_CODE_CAMERA){
            if(resultCode == RESULT_OK){
                ImageView image = (ImageView)findViewById(R.id.cur_image);
                Uri uri = Uri.fromFile(mSaveFile);
                Bitmap bmp = RegisterActivity.uri2bmp(this, uri, 160, 120);
                if(bmp != null){
                    image.setImageBitmap(bmp);
                }
                else{
                    image.setImageResource(R.drawable.default_image);
                }

                //保存用文字列
                mImageURL = uri.toString();
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, R.string.user_canceled, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, R.string.get_image_error, Toast.LENGTH_SHORT).show();
            }            
        }
        else if(requestCode == REQUEST_CODE_SILENT){
        	if(resultCode == 1){
        		ImageView image = (ImageView)findViewById(R.id.cur_image);
        		Uri uri = data.getData();
        		//保存用文字列
        		mImageURL = uri.toString();
        		Bitmap bmp = RegisterActivity.uri2bmp(this, uri, 160, 120);
        		if(bmp != null){
        			image.setImageBitmap(bmp);
        		}
        		else{
        			image.setImageResource(R.drawable.default_image);
        		}
        	}
        	else{
        		
        	}
        }
    }
    
    public static Bitmap uri2bmp(Context context,Uri uri,int maxW,int maxH) {
        BitmapFactory.Options options;
        InputStream in=null;
        try {
    		Log.d(TAG, "1");
            //画像サイズの取得
            options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
    		Log.d(TAG, "2");
            in=context.getContentResolver().openInputStream(uri);  
    		Log.d(TAG, "3");
            BitmapFactory.decodeStream(in,null,options);
    		Log.d(TAG, "4");
            in.close();
            int scaleW=options.outWidth /maxW+1;
            int scaleH=options.outHeight/maxH+1;
            int scale =Math.max(scaleW,scaleH);
         
            //画像の読み込み
            options=new BitmapFactory.Options();
            options.inJustDecodeBounds=false;
            options.inSampleSize=scale;
            options.inPurgeable=true;
            in=context.getContentResolver().openInputStream(uri);  
            Bitmap bmp = BitmapFactory.decodeStream(in, null, options);
            in.close();
            
            //回転
            int w = bmp.getWidth();
            int h = bmp.getHeight();
            if(w > h){
                Matrix matrix = new Matrix();
                // 回転させる角度を指定
                matrix.postRotate(90.0f);   
                Bitmap bmp2 = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
                bmp = null;
                bmp = bmp2;
            }
            
            return bmp;
        } catch (Exception e) {
    		Log.d(TAG, "exception");

            try {
                if (in!=null) in.close();
            } catch (Exception e2) {
            }
            return null;
        }
    }
    
    private void setEvaluate(){
        RatingBar bar = (RatingBar)findViewById(R.id.rating);
        bar.setNumStars(RATING_STAR_NUM);
        
        if(mEditFlag){
        	float rate = Float.valueOf(mRate);
        	bar.setRating(rate);
        }
    }
    
    private void setComment(){
    	if(mEditFlag){
    		if(!mComment.equals("none")){
    			EditText edit = (EditText)findViewById(R.id.impression);
    			edit.setText(mComment);
    		}
    	}
    }
    
    private void setOther(){
        final LinearLayout otherLayout = (LinearLayout)findViewById(R.id.other_display_layout);
        Button other = (Button)findViewById(R.id.log_other);
        other.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                mOtherFlag = true;
                //「その他」ボタンのレイアウトを非表示に
                otherLayout.setVisibility(View.GONE);

                //その他の項目を動的に追加(種類ごとに異なる)
                LinearLayout linear = (LinearLayout)findViewById(R.id.linearLayout1);
                final LayoutInflater mInflater;
                mInflater = (LayoutInflater)RegisterActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mInflater.inflate(R.layout.register_add, linear);
        
                setVintage();
                setType();
                setArea();
                setDate();
                setPlace();
                setPrice();
                //カテゴリごとにいらない項目をマスクする
                setMask();
            }
        });
    }
    
    private void setVintage(){
    	if(mEditFlag){
    		if(!mVintage.equals("none")){
    			TextView view = (TextView)findViewById(R.id.year);
    			view.setText(mVintage);
    		}
    	}
    }
    
    private void setType(){
    	ArrayAdapter adapter = null;
    	Spinner spinner = null;
        int id = getTypeID(mCategory);
        if(id != -1){
            adapter = ArrayAdapter.createFromResource(
                    this, id, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
            spinner = (Spinner)findViewById(R.id.type_spinner);
            spinner.setAdapter(adapter);
        }
        
    	if(mEditFlag){
    		if(!mType.equals("none")){
    			int i;
    			for(i=0; i<adapter.getCount(); i++){
    				String type = (String)adapter.getItem(i);
    				if(type.equals(mType)){
    					spinner.setSelection(i);
    	    			return;
    				}
    			}    			
    		}
    	}    	
    }
    
    private int getTypeID(int idx){
        switch(idx){
            case DrinkLogActivity.CATEGORY_WHISKEY:
                return R.array.type_array_wh;
                
            case DrinkLogActivity.CATEGORY_COCKTAIL:
            	return R.array.type_array_co;
                
            case DrinkLogActivity.CATEGORY_WINE:
            	return R.array.type_array_wi;

            case DrinkLogActivity.CATEGORY_SHOCHU:
                return R.array.type_array_sh;

            case DrinkLogActivity.CATEGORY_SAKE:
            	return R.array.type_array_ja;

            case DrinkLogActivity.CATEGORY_BRANDY:
            	return R.array.type_array_br;

            case DrinkLogActivity.CATEGORY_BEER:
                return R.array.type_array_be;

            default:
            	return -1;
        }
    }
    
    private void setArea(){
    	ArrayAdapter adapter = null;
    	Spinner spinner = null;
        int id = getAreaID(mCategory);
        if(id != -1){
            adapter = ArrayAdapter.createFromResource(
                    this, id, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
            spinner = (Spinner)findViewById(R.id.area_spinner);
            spinner.setAdapter(adapter);        
        }
        
    	if(mEditFlag){
    		if(!mArea.equals("none")){
    			int i;
    			for(i=0; i<adapter.getCount(); i++){
    				String type = (String)adapter.getItem(i);
    				if(type.equals(mArea)){
    					spinner.setSelection(i);
    	    			return;
    				}
    			}    			
    		}
    	}    	
    }
    
    private int getAreaID(int idx){
        switch(idx){
            case DrinkLogActivity.CATEGORY_WHISKEY:
                return R.array.area_array_wh;
                
            case DrinkLogActivity.CATEGORY_WINE:
            	return R.array.area_array_wi;

            case DrinkLogActivity.CATEGORY_SHOCHU:
                return R.array.area_array_sh;

            case DrinkLogActivity.CATEGORY_SAKE:
            	return R.array.area_array_ja;

            case DrinkLogActivity.CATEGORY_BRANDY:
            	return R.array.area_array_br;

            case DrinkLogActivity.CATEGORY_BEER:
                return R.array.area_array_be;

            default:
            	return -1;
        }
    }
    
    private void setDate(){
        TextView text = (TextView)findViewById(R.id.date);
        if(mEditFlag){
        	mCurDate = mDate;
        	text.setText(mDate);
        }
        else{
        	text.setText(mCurDate);
        }
        
        ImageButton selectBtn = (ImageButton)findViewById(R.id.date_select);
        selectBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                //指定された日付を設定
                                TextView text = (TextView)findViewById(R.id.date);
                                mCurDate = getDate(year, month, day);
                                text.setText(mCurDate);
                            }
                        },
                        mYear, mMonth, mDay);
                dialog.show();
            }
        });
    }
    
    private String getDate(int year, int mon, int d){        
        String month = Integer.toString(mon + 1);
        if(month.length() == 1){
            month = "0" + month;
        }
        String day = Integer.toString(d);
        if(day.length() == 1){
            day = "0" + day;
        }
        return Integer.toString(year) + "/" + month + "/" + day;
    }
    
    private void setPlace(){
    	if(mEditFlag){
    		if(!mPlace.equals("none")){
    			TextView view = (TextView)findViewById(R.id.place);
    			view.setText(mPlace);
    		}
    	}
	
    }
    
    private void setPrice(){
    	if(mEditFlag){
    		if(!mPrice.equals("none")){
    			TextView view = (TextView)findViewById(R.id.price);
    			view.setText(mPrice);
    		}
    	}
    }
    
    private void setMask(){
    	LinearLayout year = (LinearLayout)findViewById(R.id.reg_year_layout);
    	LinearLayout area = (LinearLayout)findViewById(R.id.reg_area_layout);
    	LinearLayout type = (LinearLayout)findViewById(R.id.reg_type_layout);    	

    	switch(mCategory){
        case DrinkLogActivity.CATEGORY_WHISKEY:
            break;
            
        case DrinkLogActivity.CATEGORY_COCKTAIL:
        	year.setVisibility(View.GONE);
        	area.setVisibility(View.GONE);
            break;
            
        case DrinkLogActivity.CATEGORY_WINE:
            break;

        case DrinkLogActivity.CATEGORY_SHOCHU:
        	year.setVisibility(View.GONE);
            break;

        case DrinkLogActivity.CATEGORY_SAKE:
        	year.setVisibility(View.GONE);
            break;

        case DrinkLogActivity.CATEGORY_BRANDY:
            break;

        case DrinkLogActivity.CATEGORY_BEER:
        	year.setVisibility(View.GONE);
            break;

        case DrinkLogActivity.CATEGORY_OTHER:
        	year.setVisibility(View.GONE);
        	type.setVisibility(View.GONE);
        	area.setVisibility(View.GONE);
            break;

        default:
            break;
        }
    }
        
    private void setRegister(){
        final Handler handler = new Handler();
        //final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
        
        Button regBtn = (Button)findViewById(R.id.register_commit);
        regBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Thread thread = new Thread() {
                    public void run() {
                        //0文字の名前は受け付けない
                        TextView name_view = (TextView)findViewById(R.id.name);
                        mName = name_view.getText().toString();
                        if(mName.length() == 0){
                            handler.post(new Runnable(){
                                public void run(){            
                                    new AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle(R.string.register_warning)
                                    .setMessage(R.string.input_request)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            return;
                                        }
                                    }).show();
                                }
                            });
                        }
                        else{
                            register(mName);
                            
                            //登録完了通知
                            handler.post(new Runnable(){
                                public void run(){
                                    new AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle(R.string.dialog_notify_title)
                                    .setMessage(R.string.dialog_notify)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //Twitter同時投稿
                                            if(DrinkLogPreference.isAutoTweet(RegisterActivity.this)){
                                                Intent intent = new Intent(RegisterActivity.this, TwitterActivity.class);
                                                intent.putExtra("name", mName);
                                                intent.putExtra("rate", String.valueOf(mRate));
                                                if(mComment.length() != 0){
                                                    intent.putExtra("comment", mComment);
                                                }
                                                else{
                                                    intent.putExtra("comment", "none");
                                                }
                                                if(mImageURL != null){
                                                    intent.putExtra("path", mImageURL);
                                                }
                                                else{
                                                    intent.putExtra("path", "none");
                                                }
                                                startActivity(intent);                                             
                                            }
                                            
                                            //タイトルに戻る
                            	            Intent back_intent = new Intent();
                            	            setResult(RESPONSE_EDIT, back_intent);
                                            finish();
                                        }
                                    })
                                    .show();
                                }
                            });
                        }
                    }
                };
                thread.start();
            }
        });
        
        Button canclBtn = (Button)findViewById(R.id.register_cancel);
        canclBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                new AlertDialog.Builder(RegisterActivity.this)
                .setTitle(R.string.cancel_confirm_title)
                .setMessage(R.string.cancel_confirm)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //何もしない
                        return;
                    }
                })
                .show();
            }
        });
    }
    
    private void register(String name){
        switch(mCategory){
            case DrinkLogActivity.CATEGORY_WHISKEY:
                putWhData(mCategory, name);
                break;
                
            case DrinkLogActivity.CATEGORY_COCKTAIL:
                putCoData(mCategory, name);
                break;
                
            case DrinkLogActivity.CATEGORY_WINE:
                putWiData(mCategory, name);
                break;

            case DrinkLogActivity.CATEGORY_SHOCHU:
                putShData(mCategory, name);
                break;

            case DrinkLogActivity.CATEGORY_SAKE:
                putJaData(mCategory, name);
                break;

            case DrinkLogActivity.CATEGORY_BRANDY:
                putBrData(mCategory, name);
                break;

            case DrinkLogActivity.CATEGORY_BEER:
                putBeData(mCategory, name);
                break;

            case DrinkLogActivity.CATEGORY_OTHER:
                putOtData(mCategory, name);
                break;

            default:
                Log.e(TAG, "Unknown Category");
                break;
        }
    }
    
    private void putWhData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putYear(values);
        putType(values);
        putArea(values);
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }

    private void putCoData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putType(values);
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }

    private void putWiData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putYear(values);
        putType(values);
        putArea(values);
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }

    private void putShData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putType(values);
        putArea(values);
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }
    
    private void putJaData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putType(values);
        putArea(values);
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }

    private void putBrData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putYear(values);
        putType(values);
        putArea(values);
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }

    private void putBeData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putType(values);
        putArea(values);
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }

    private void putOtData(int category, String name){
        ContentValues values = new ContentValues();
        initValues(values);

        values.put("category", String.valueOf(category));
        putName(values, name);
        putImage(values);
        putEvaluate(values);
        putComment(values);
        /*以下はその他の項目*/
        putDate(values);
        putPlace(values);
        putPrice(values);

        registerDB(values);
    }
    
    private void registerDB(ContentValues values){
        SQLiteDatabase db = mHelper.getWritableDatabase();

        if(mEditFlag){
        	db.update("logtable", values, "rowid = ?", new String[]{Integer.toString(mDBID)});
        }
        else{
        	db.insert("logtable", null, values);
        }
        
        db.close();            	
    }
    
    private void initValues(ContentValues values){
        values.put("image", "none");
        values.put("comment", "none");
        values.put("year", "none");
        values.put("type", "none");
        values.put("area", "none");
        values.put("place", "none");
        values.put("price", "none");    	
    }

    private void putName(ContentValues values, String name){
        values.put("name", name);
    }
    
    private void putImage(ContentValues values){
        if(mImageURL != null){
            values.put("image", mImageURL);
        }
    }
    
    private void putEvaluate(ContentValues values){
        RatingBar bar = (RatingBar)findViewById(R.id.rating);
        mRate = bar.getRating();
        String rate_str = String.valueOf(mRate);
        values.put("evaluate", rate_str);
    }
    
    private void putComment(ContentValues values){
        EditText comment_text = (EditText)findViewById(R.id.impression);
        mComment = comment_text.getText().toString();
        if(mComment.length() != 0){
            values.put("comment", mComment);
        }
    }
    
    private void putYear(ContentValues values){
        if(mOtherFlag){
            EditText year_text = (EditText)findViewById(R.id.year);
            String year = year_text.getText().toString();
            if(year.length() != 0){
                values.put("year", year);
            }
        }
        else{
            if(mEditFlag){
            	/*
            	 * 編集時、かつ「その他」ボタンが押されない場合は、
            	 * DBから直接取り出したデータを登録する。
            	 * (以下、putXXX系は同じ)
            	 */
            	values.put("year", mYear);
            }
        }        
    }
    
    private void putType(ContentValues values){
        if(mOtherFlag){
            Spinner type_spinner = (Spinner)findViewById(R.id.type_spinner);
            String type = type_spinner.getSelectedItem().toString();
            if(!type.equals("")){
                values.put("type", type);
            }
        }
        else{
            if(mEditFlag){
            	values.put("type", mType);
            }
        }        
    }
    
    private void putArea(ContentValues values){
        //地域
        if(mOtherFlag){
            Spinner area_spinner = (Spinner)findViewById(R.id.area_spinner);
            String area = area_spinner.getSelectedItem().toString();
            if(!area.equals("")){
                values.put("area", area);
            }
        }
        else{
            if(mEditFlag){
            	values.put("area", mArea);
            }
        }
    }
    
    private void putDate(ContentValues values){
    	values.put("date", mCurDate);
    }

    private void putPlace(ContentValues values){
        if(mOtherFlag){
            EditText place_text = (EditText)findViewById(R.id.place);
            String place = place_text.getText().toString();
            if(place.length() != 0){
                values.put("place", place);
            }
        }
        else{
            if(mEditFlag){
            	values.put("place", mPlace);
            }
        }
    }

    private void putPrice(ContentValues values){
        if(mOtherFlag){
            EditText price_text = (EditText)findViewById(R.id.price);
            String price = price_text.getText().toString();
            if(price.length() != 0){
                values.put("price", price);
            }
        }
        else{
            if(mEditFlag){
            	values.put("price", mPrice);
            }
        }
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(RegisterActivity.this)
            .setTitle(R.string.cancel_confirm_title)
            .setMessage(R.string.cancel_confirm)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //何もしない
                }
            })
            .show();
            return false;
        }
        else{
            return super.onKeyDown(keyCode, event);   
        }
    }
}
