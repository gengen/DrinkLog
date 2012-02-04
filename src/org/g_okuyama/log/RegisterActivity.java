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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    boolean mPicFlag = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mHelper = new DatabaseHelper(this);
        setLayout();
    }
    
    private void setLayout(){
        setName();
        setCategory();
        setImage();
        setDate();
        setEvaluate();
        setRegister();
    }
    
    private void setName(){
        ArrayAdapter name_adapter = ArrayAdapter.createFromResource(
                this, R.array.whiskey_array, android.R.layout.simple_dropdown_item_1line);
        AutoCompleteTextView view = (AutoCompleteTextView)findViewById(R.id.name);
        view.setAdapter(name_adapter);
        //TODO:登録されていない名前で登録された場合は、name_adapterに追加する
        
        view.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable edit) {
                String name = edit.toString();
                //TODO:名前から地域を特定
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
        });
    }
    
    private void setCategory(){
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.area_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setAdapter(adapter);        
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
                .setTitle(R.string.dialog_category)
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
    
    private void setDate(){
        Calendar cal1 = Calendar.getInstance();
        final int mYear = cal1.get(Calendar.YEAR);
        final int mMonth = cal1.get(Calendar.MONTH);
        final int mDay = cal1.get(Calendar.DATE);

        TextView text = (TextView)findViewById(R.id.date);
        mCurDate = getDate(mYear, mMonth, mDay);
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
    
    private void setEvaluate(){
        RatingBar bar = (RatingBar)findViewById(R.id.rating);
        bar.setNumStars(RATING_STAR_NUM);
        //bar.setStepSize(RATING_STEP);
    }
    
    private void setRegister(){
        final Handler handler = new Handler();
        //final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
        
        Button regBtn = (Button)findViewById(R.id.register_commit);
        regBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Thread thread = new Thread() {
                    public void run() {
                        /*
                        handler.post(new Runnable(){
                            public void run() {
                                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                dialog.setMessage(getString(R.string.dialog_progress));
                                dialog.show();                                
                            }
                        });
                        */

                        checkValue();

                        /*
                        handler.post(new Runnable(){
                            public void run(){
                                dialog.dismiss();                                
                            }
                        });
                        */
                        
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
            new AlertDialog.Builder(RegisterActivity.this)
                .setTitle(R.string.register_warning)
                .setMessage(R.string.input_request)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
        }
        else{
            register(name);
        }
    }
    
    private void register(String name){
        Log.d(TAG, "enter register()");
        
        ContentValues values = new ContentValues();
        
        //名前
        values.put("name", name);

        //種類
        //TODO:他種類のケース追加
        values.put("category", getString(R.string.whiskey));
        
        //年数
        EditText year_text = (EditText)findViewById(R.id.year);
        String year = year_text.getText().toString();
        if(year.length() == 0){
            values.put("year", "none");
        }
        else{
            values.put("year", year);
        }
        
        //タイプ
        //TODO:他種類のケース追加
        values.put("type", "none");
        
        //地域
        Spinner area_spinner = (Spinner)findViewById(R.id.spinner);
        String area = area_spinner.getSelectedItem().toString();
        if(area.equals("")){
            values.put("area", "none");
        }
        else{
            values.put("area", area);
        }
        
        //画像(image)
        if(mLinkStr == null){
            values.put("image", "none");
        }
        else{
            values.put("image", mLinkStr);
        }
        
        //日時(date)
        values.put("date", mCurDate);
        
        //場所(place)
        EditText place_text = (EditText)findViewById(R.id.place);
        String place = place_text.getText().toString();
        if(place.length() == 0){
            values.put("place", "none");
        }
        else{
            values.put("place", place);
        }
        
        //価格(price)
        EditText price_text = (EditText)findViewById(R.id.price);
        String price = price_text.getText().toString();
        if(place.length() == 0){
            values.put("price", "none");
        }
        else{
            values.put("price", price);
        }

        //評価(evaluate)
        RatingBar bar = (RatingBar)findViewById(R.id.rating);
        float rate = bar.getRating();
        String rate_str = String.valueOf(rate);
        values.put("evaluate", rate_str);
        
        //コメント(comment)
        EditText comment_text = (EditText)findViewById(R.id.impression);
        String comment = comment_text.getText().toString();
        if(comment.length() == 0){
            values.put("comment", "none");
        }
        else{
            values.put("comment", comment);
        }
        
        //DBに登録
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.insert("logtable", null, values);
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
