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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    
    DatabaseHelper mHelper = null;
    File mSaveFile = null;
    String mLinkStr = null;
    String mCurDate = "unknown";
    int mCategory;
    
    int mYear;
    int mMonth;
    int mDay;

    boolean mPicFlag = false;
    //「その他」ボタンが押されたか？
    boolean mOtherFlag = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extra = getIntent().getExtras();
        mCategory = extra.getInt("category");
        
        //自動でキーボードを出さないように設定
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);         
        setContentView(R.layout.register);

        mHelper = new DatabaseHelper(this);
        
        Calendar cal1 = Calendar.getInstance();
        mYear = cal1.get(Calendar.YEAR);
        mMonth = cal1.get(Calendar.MONTH);
        mDay = cal1.get(Calendar.DATE);
        mCurDate = getDate(mYear, mMonth, mDay);
        setLayout();
    }
    
    private void setLayout(){
        setName();
        setImage();
        setEvaluate();
        setOther();
        setRegister();
    }
        
    private void setName(){
        AutoCompleteTextView view = (AutoCompleteTextView)findViewById(R.id.name);

        int id = getCategoryID(mCategory);
        if(id != -1){
            ArrayAdapter name_adapter = ArrayAdapter.createFromResource(
                    this, id, android.R.layout.simple_dropdown_item_1line);
            view.setAdapter(name_adapter);
        }
        //TODO:登録されていない名前で登録された場合は、name_adapterに追加する
        
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
                
                //TODO:多種類追加
            default:
                return -1;
        }
    }
    
    private void setImage(){
        ImageView curSet = (ImageView)findViewById(R.id.cur_image);
        //TODO:デフォルト画像表示
        //curSet.setImageURI(uri);
        
        final String[] setting_list = getResources().getStringArray(R.array.image_setting_array);        
        Button imageBtn = (Button)findViewById(R.id.picture);
        imageBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                new AlertDialog.Builder(RegisterActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.dialog_image)
                .setItems(setting_list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            //gallery
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY);
                        }
                        else{
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
                    }
                })
               .show();
            }
        });            
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
                mLinkStr = uri.toString();
                image.setImageBitmap(uri2bmp(this, uri, 160, 120));
                Button button = (Button)findViewById(R.id.picture);
                button.setText(R.string.modify);
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
                image.setImageBitmap(uri2bmp(this, uri, 160, 120));
                //保存用文字列
                mLinkStr = uri.toString();
                Button button = (Button)findViewById(R.id.picture);
                button.setText(R.string.modify);
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, R.string.user_canceled, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, R.string.get_image_error, Toast.LENGTH_SHORT).show();
            }            
        }
    }
    
    public static Bitmap uri2bmp(Context context,Uri uri,int maxW,int maxH) {
        BitmapFactory.Options options;
        InputStream in=null;
        try {
            //画像サイズの取得
            options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            in=context.getContentResolver().openInputStream(uri);  
            BitmapFactory.decodeStream(in,null,options);
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
            return bmp;
        } catch (Exception e) {
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
        //bar.setStepSize(RATING_STEP);
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
                int id = getLayoutID(mCategory);
                if(id != -1){
                    mInflater.inflate(id, linear);
                    setArea();
                    setDate();
                }
            }
        });
    }
    
    private int getLayoutID(int idx){
        switch(idx){
            case DrinkLogActivity.CATEGORY_WHISKEY:
                return R.layout.add_wh;
                
                //TODO:多種類追加
                
            default:
                return -1;
        }
    }

    private void setArea(){
        int id = getAreaID(mCategory);
        if(id != -1){
            ArrayAdapter adapter = ArrayAdapter.createFromResource(
                    this, id, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
            Spinner spinner = (Spinner)findViewById(R.id.spinner);
            spinner.setAdapter(adapter);        
        }
        else{
            //TODO:定義されていないとき
        }
    }
    
    private int getAreaID(int idx){
        switch(idx){
            case DrinkLogActivity.CATEGORY_WHISKEY:
                return R.array.area_array_wh;
                
                //TODO:多種類追加
                
            default:
                return -1;
        }
    }
    
    private void setDate(){
        TextView text = (TextView)findViewById(R.id.date);
        text.setText(mCurDate);
        
        Button selectBtn = (Button)findViewById(R.id.date_select);
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
    
    private void setRegister(){
        final Handler handler = new Handler();
        //final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
        
        Button regBtn = (Button)findViewById(R.id.register_commit);
        regBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Thread thread = new Thread() {
                    public void run() {
                        checkValue();
                        
                        handler.post(new Runnable(){
                            public void run(){
                                new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage(R.string.dialog_notify)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                            }
                        });
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
    
    private void checkValue(){
        //0文字の名前は受け付けない
        TextView name_view = (TextView)findViewById(R.id.name);
        String name = name_view.getText().toString();
        if(name.length() == 0){
            final Handler handler = new Handler();
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
            register(name);
        }        
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);
        db.close();
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);        
        db.close();
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);
        db.close();
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);   
        db.close();
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);   
        db.close();
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);
        db.close();        
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);        
        db.close();
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

        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);
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
        if(mLinkStr != null){
            values.put("image", mLinkStr);
        }        
    }
    
    private void putEvaluate(ContentValues values){
        RatingBar bar = (RatingBar)findViewById(R.id.rating);
        float rate = bar.getRating();
        String rate_str = String.valueOf(rate);
        values.put("evaluate", rate_str);        
    }
    
    private void putComment(ContentValues values){
        EditText comment_text = (EditText)findViewById(R.id.impression);
        String comment = comment_text.getText().toString();
        if(comment.length() != 0){
            values.put("comment", comment);
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
    }
    
    private void putType(ContentValues values){
        //TODO:他種類のケース追加
        values.put("type", "none");
    }
    
    private void putArea(ContentValues values){
        //地域
        if(mOtherFlag){
            Spinner area_spinner = (Spinner)findViewById(R.id.spinner);
            String area = area_spinner.getSelectedItem().toString();
            if(!area.equals("")){
                values.put("area", area);
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
    }

    private void putPrice(ContentValues values){
        if(mOtherFlag){
            EditText price_text = (EditText)findViewById(R.id.price);
            String price = price_text.getText().toString();
            if(price.length() != 0){
                values.put("price", price);
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
