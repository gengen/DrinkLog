package org.g_okuyama.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TwitterActivity extends Activity {
	public static final String TAG = "DrinkLog";
	private static final String CONSUMER_KEY = "Xf6Rt9AL8tbzyVpv6D0CIw";
	private static final String CONSUMER_SECRET = "0HOulWNwBRtxADaAIz5p9f8B6RrYT3tLIacbD7wlm28";
	private static final String CALLBACK_URL = "myapp://callback";
	
	private CommonsHttpOAuthConsumer mConsumer;
	private OAuthProvider mProvider;
	
	String mName;
	String mRate;
	String mComment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter);
    	doOauth();
    	
        Bundle extras = getIntent().getExtras();
        mName = extras.getString("name");
        mRate = extras.getString("rate");
        mComment = extras.getString("comment");
        
    	setLayout();
    }
    
    private void doOauth() {
        try {
            mConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
            mProvider = new DefaultOAuthProvider(
                "https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "https://api.twitter.com/oauth/authorize");
            
            //トークンの読み込み
            SharedPreferences pref= getSharedPreferences("token", MODE_PRIVATE);
            String token      = pref.getString("token", null);
            String tokenSecret= pref.getString("tokenSecret", null);
            
            //認証済み
            if (token != null && tokenSecret != null) {
                mConsumer.setTokenWithSecret(token, tokenSecret);
                
                //Twitter操作
                //doTweet("てすと２");
            } 
            //認証処理のためブラウザ起動
            else {
                String authUrl = mProvider.retrieveRequestToken(mConsumer, CALLBACK_URL);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onNewIntent(final Intent intent){
    	super.onNewIntent(intent);
    	final Uri uri = intent.getData();
    	// return from Twitter OAuth
    	if(uri != null && uri.toString().startsWith(CALLBACK_URL)){
    		try {
    			String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
    			mProvider.retrieveAccessToken(mConsumer, verifier);
    			// set prefs
    			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    			SharedPreferences.Editor editor = prefs.edit();
    			editor.putString("token", mConsumer.getToken());
    			editor.putString("tokenSecret", mConsumer.getTokenSecret());
    			editor.commit();
    			
    			/*ここからTwitter操作*/
    			//doTweet("てすと");
    			
    		} catch (Exception e) {
    			Log.e(TAG, e.getMessage(), e);
    			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    		}   
    	}
    }
    
    private void setLayout(){
    	String text = mName + ":"
    				+ getString(R.string.evaluate) + mRate + ", "
    				+ mComment 
    				+ " from " + getString(R.string.app_name);
    	EditText view = (EditText)findViewById(R.id.tweet_text);
    	view.setText(text);
    	
    	Button button = (Button)findViewById(R.id.tweet);
    	button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
		    	EditText edit = (EditText)findViewById(R.id.tweet_text);
				String text = edit.getText().toString();
				doTweet(text);
			}
    	});
    	
    }

    private void doTweet(String text){
        try {
            HttpPost post=new HttpPost("http://api.twitter.com/1/statuses/update.xml"); 
            final List<NameValuePair> params=new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("status", text));  
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));  
            post.getParams().setBooleanParameter(
                CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            mConsumer.sign(post);
            DefaultHttpClient http=new DefaultHttpClient();
            http.execute(post);
        } catch (Exception e) {
        	Log.e(TAG, "failed to tweet");
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }    
}
