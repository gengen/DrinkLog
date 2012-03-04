package org.g_okuyama.log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class TwitterActivity extends Activity {
	public static final String TAG = "DrinkLog";
	private static final String CONSUMER_KEY = "Xf6Rt9AL8tbzyVpv6D0CIw";
	private static final String CONSUMER_SECRET = "0HOulWNwBRtxADaAIz5p9f8B6RrYT3tLIacbD7wlm28";
	private static final String CALLBACK_URL = "http://neging01.blog87.fc2.com/";

	private static final String MARKET_URL = "https://market.android.com/details?id=org.g_okuyama.log";
	
	private static final int REQUEST_OAUTH = 3333;
	
	String mName;
	String mRate;
	String mComment;
	String mPath = "";
	
	String mToken = null;
	String mTokenSecret = null;
	Twitter mTwitter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter);
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        Bundle extras = getIntent().getExtras();
        mName = extras.getString("name");
        mRate = extras.getString("rate");
        mComment = extras.getString("comment");
        mPath = extras.getString("path");
        setLayout();
    }
    
    private void setLayout(){
    	String comment = "";
    	if(!mComment.equals("none")){
    		comment = mComment;
    	}
        String text = mName + ":"
                    + getString(R.string.evaluate) + mRate + "  "
                    + comment 
                    + " from " + getString(R.string.app_name);
        int limit;
        if(!mPath.equals("none")){
        	limit = 55;/*140(Twitter)-55(�}�[�P�b�gURL��)-30(�摜URL��)*/
        }
        else{
        	limit = 85;
        }
        if(text.length() < limit){
        	text = text + " " + MARKET_URL;
        }
        
        EditText view = (EditText)findViewById(R.id.tweet_text);
        view.setText(text);
        
        Button button = (Button)findViewById(R.id.tweet);
        button.setOnClickListener(new OnClickListener(){
            public void onClick(View arg0) {
            	if(isAuthenticated()){
            		tweet();
            	}
            	else{
            		Intent intent = new Intent(TwitterActivity.this, OAuthActivity.class);
            		intent.putExtra(OAuthActivity.CALLBACK, CALLBACK_URL);
            		intent.putExtra(OAuthActivity.CONSUMER_KEY, CONSUMER_KEY);
            		intent.putExtra(OAuthActivity.CONSUMER_SECRET, CONSUMER_SECRET);
            		startActivityForResult(intent, REQUEST_OAUTH);
            	}
            }
        });
    }
    
    private boolean isAuthenticated(){
        
        //�g�[�N���̓ǂݍ���
        SharedPreferences pref= getSharedPreferences("token", MODE_PRIVATE);
        mToken = pref.getString("token", null);
        mTokenSecret= pref.getString("tokenSecret", null);
        
        //�F�؍ς�
        if (mToken != null && mTokenSecret != null) {
            return true;
        }
    	
    	return false;
    }
    
    private void tweet(){
        if(mPath.equals("none")){
            //twitter�I�u�W�F�N�g�̍쐬 
            mTwitter = new TwitterFactory().getInstance();
            //AccessToken�I�u�W�F�N�g�̍쐬 
            AccessToken at = new AccessToken(mToken, mTokenSecret);
            //Consumer key��Consumer key seacret�̐ݒ�
            mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);   
            //AccessToken�I�u�W�F�N�g��ݒ� 
            mTwitter.setOAuthAccessToken(at);
            
            EditText view = (EditText)findViewById(R.id.tweet_text);
            
            try {
                mTwitter.updateStatus(view.getText().toString());
                Toast.makeText(this, R.string.tweet_finish, Toast.LENGTH_LONG).show();
            } catch (TwitterException e){
                if(e.isCausedByNetworkIssue()){
                    Toast.makeText(this, R.string.tweet_nw_error, Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, R.string.tweet_error, Toast.LENGTH_LONG).show();
                }
            }            
        }
        else{
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(CONSUMER_KEY);
            builder.setOAuthConsumerSecret(CONSUMER_SECRET);
            builder.setOAuthAccessToken(mToken);
            builder.setOAuthAccessTokenSecret(mTokenSecret);
            // ������MediaProvider��TWITTER�ɂ���
            builder.setMediaProvider("TWITTER");

            Configuration conf = builder.build();
            ImageUpload imageUpload = new ImageUploadFactory(conf)
                    .getInstance();

            Bitmap bitmap = RegisterActivity.uri2bmp(this, Uri.parse(mPath), 240, 320);
            try {
                byte[] w = bmp2data(bitmap, Bitmap.CompressFormat.JPEG, 80);
                writeDataFile("tmp.jpg", w);

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            //File file = new File(mPath);
            //�A�b�v���[�h�p�̃C���[�W���t�@�C���ɏ�������
            //twitter4j��upload��������file����邽�߁A��x��������
            File file = new File(getFilesDir() + "/tmp.jpg");
            Log.d(TAG, "path = " + file.getPath());
            
            EditText view = (EditText)findViewById(R.id.tweet_text);
            Log.d(TAG, "text = " + view.getText().toString());
            
            try {
                imageUpload.upload(file, view.getText().toString());
                Toast.makeText(this, R.string.tweet_finish, Toast.LENGTH_LONG).show();

            } catch (TwitterException e) {
                if(e.isCausedByNetworkIssue()){
                    Toast.makeText(this, R.string.tweet_nw_error, Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, R.string.tweet_error, Toast.LENGTH_LONG).show();
                }
            }
            
            //�A�b�v���[�h�p�̃C���[�W�폜
            file.delete();
        }
		
		finish();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_OAUTH){
        	if(resultCode == Activity.RESULT_OK){
        		//long userId = data.getLongExtra(OAuthActivity.USER_ID, 0);
        		//String screenName = data.getStringExtra(OAuthActivity.SCREEN_NAME);
        		mToken = data.getStringExtra(OAuthActivity.TOKEN);
        		mTokenSecret = data.getStringExtra(OAuthActivity.TOKEN_SECRET);
            
        		//Preference��token�o�^
    			SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
    			SharedPreferences.Editor editor = prefs.edit();
    			editor.putString("token", mToken);
    			editor.putString("tokenSecret", mTokenSecret);
    			editor.commit();
    			
    			tweet();
        	}
        }
    }
    
    //Bitmap���o�C�g�f�[�^
    private static byte[] bmp2data(Bitmap src,
        Bitmap.CompressFormat format,int quality) {
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        src.compress(format,quality,os);            
        return os.toByteArray();
    }
   
    //�t�@�C���ւ̃o�C�g�f�[�^��������
    private void writeDataFile(String name, byte[] w) throws Exception {
        OutputStream out=null;
        try {
            out= openFileOutput(name, Context.MODE_WORLD_READABLE);
            out.write(w, 0, w.length);
            out.close();
        } catch (Exception e) {
            try {
                if (out!=null) out.close();
            } catch (Exception e2) {
            }
            throw e;
        }
    }

}
