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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

	//private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=org.g_okuyama.log";
	private static final String MARKET_URL = "http://goo.gl/m70NO";
	private static final int REQUEST_OAUTH = 3333;
	private static final String HASH_TAG = "#drinklog";
	private ProgressDialog mDialog = null;
	
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
        
        setTitle(getString(R.string.tweet_title));
        setLayout();
        
        mDialog = new ProgressDialog(this);
    }
    
    private void setLayout(){
    	String comment = "";
    	if(!mComment.equals("none")){
    		comment = mComment;
    	}
    	/*
    	 * 80�����ȓ��Ƃ��� TODO:�L���ł͍L������(110�����܂�OK)
    	 * (140(Twitter)-20(�}�[�P�b�gURL��(�Z�k))-30(�摜URL��)-10(" #drinklog"))
    	 */
        String text = mName + ":"
                    + getString(R.string.evaluate) + mRate + "  "
                    + comment; 
        
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
        final Handler handler = new Handler();
        Thread thread = new Thread(){
            public void run(){
                handler.post(new Runnable(){
                    public void run() {
                        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mDialog.show();                        
                    }
                });

                EditText view = (EditText)findViewById(R.id.tweet_text);
                String tweetText = view.getText().toString();
                tweetText = tweetText + " " + HASH_TAG + " " + MARKET_URL;
                
                //�摜���o�^����Ă��Ȃ��A�������͉摜��Y�t���Ȃ��ݒ�ɂ��Ă���ꍇ
                if(mPath.equals("none") || !DrinkLogPreference.isAttached(TwitterActivity.this)){
                    //twitter�I�u�W�F�N�g�̍쐬 
                    mTwitter = new TwitterFactory().getInstance();
                    //AccessToken�I�u�W�F�N�g�̍쐬 
                    AccessToken at = new AccessToken(mToken, mTokenSecret);
                    //Consumer key��Consumer key secret�̐ݒ�
                    mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);   
                    //AccessToken�I�u�W�F�N�g��ݒ� 
                    mTwitter.setOAuthAccessToken(at);
                    
                    String message = getString(R.string.tweet_finish);
                    try {
                        mTwitter.updateStatus(tweetText);
                    } catch (TwitterException e){
                        if(e.isCausedByNetworkIssue()){
                            message = getString(R.string.tweet_nw_error);
                        }
                        else{
                            message = getString(R.string.tweet_error);
                        }
                    } finally{
                        if(mDialog != null){
                            handler.post(new Runnable(){
                                public void run() {
                                    mDialog.dismiss();
                                }
                            });
                        }
                        
                        final String m = message;
                        handler.post(new Runnable(){
                            public void run() {
                                Toast.makeText(TwitterActivity.this, m, Toast.LENGTH_LONG).show();
                            }
                        });                        
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
                    ImageUpload imageUpload = new ImageUploadFactory(conf).getInstance();

                    Bitmap bitmap = RegisterActivity.uri2bmp(TwitterActivity.this, Uri.parse(mPath), 240, 320);
                    try {
                        byte[] w = bmp2data(bitmap, Bitmap.CompressFormat.JPEG, 80);
                        writeDataFile("tmp.jpg", w);

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    //�A�b�v���[�h�p�̃C���[�W���t�@�C���ɏ�������
                    //twitter4j��upload��������file����邽�߁A��x��������
                    File file = new File(getFilesDir() + "/tmp.jpg");
                    Log.d(TAG, "path = " + file.getPath());
                    Log.d(TAG, "text = " + view.getText().toString());
                    
                    String message = getString(R.string.tweet_finish);
                    try {
                        imageUpload.upload(file, tweetText);
                    } catch (TwitterException e){
                        if(e.isCausedByNetworkIssue()){
                            message = getString(R.string.tweet_nw_error);
                        }
                        else{
                            message = getString(R.string.tweet_error);
                        }
                    } finally{
                        if(mDialog != null){
                            handler.post(new Runnable(){
                                public void run() {
                                    mDialog.dismiss();
                                }
                            });
                        }
                        
                        final String m = message;
                        handler.post(new Runnable(){
                            public void run() {
                                Toast.makeText(TwitterActivity.this, m, Toast.LENGTH_LONG).show();
                            }
                        });                        
                    }
                    //�A�b�v���[�h�p�̃C���[�W�폜
                    file.delete();
                }
                finish();                
            }
        };
        thread.start();
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
