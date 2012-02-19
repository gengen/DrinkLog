package org.g_okuyama.log;

import java.util.List;

import org.g_okuyama.log.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class LogArrayAdapter extends ArrayAdapter<LogListData> {
	public static final String TAG = "DrinkLog"; 
	private LayoutInflater mInflater;

	public LogArrayAdapter(
			Context context, int textViewResourceId, List<LogListData> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		/*
		 * メモリ不足になるかもしれないので、その場合は使いまわす。
		 * ただし、VISIBLE設定を初期化したほうがよい。
		 */
		//if(convertView == null){
			convertView = mInflater.inflate(R.layout.logitem, null);
		//}
		
		//現在の行に設定するオブジェクト
		LogListData data = (LogListData)getItem(position);

		if(!isEnabled(position)){
			//イメージと評価を非表示にし、区切りとしての日付を入れる
			ImageView image = (ImageView)convertView.findViewById(R.id.item_image);
			image.setVisibility(View.INVISIBLE);
			
			RatingBar rate = (RatingBar)convertView.findViewById(R.id.item_rating);
			rate.setVisibility(View.INVISIBLE);
			
			TextView text = (TextView)convertView.findViewById(R.id.item_name);
			text.setText(data.getTextData());
			text.setTextSize(Float.valueOf("15.0"));
			
			//デザイン
			//TODO:高さを調節したい
			/*
			convertView.setLayoutParams(
					new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 35));
					*/
			convertView.setBackgroundColor(Color.GRAY);
			
			return convertView;
		}
		
		//イメージ
		ImageView image = (ImageView)convertView.findViewById(R.id.item_image);
		int imageID = data.getImageID();
		switch(imageID){
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
            return null;
		}
		
		//名前
		TextView text = (TextView)convertView.findViewById(R.id.item_name);
		text.setText(data.getTextData());
		//評価
		RatingBar rate = (RatingBar)convertView.findViewById(R.id.item_rating);
		rate.setRating(data.getRatingData());
		
		return convertView;
	}

	@Override  
	public boolean isEnabled(int position) {
		if(getItem(position).getImageID() != -9999){
			return true;
		}
		else{
			return false;
		}
	}  
}
