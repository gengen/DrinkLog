package org.g_okuyama.log;

import java.util.List;

import org.g_okuyama.log.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class LogArrayAdapter extends ArrayAdapter<LogListData> {
	private LayoutInflater mInflater;

	public LogArrayAdapter(
			Context context, int textViewResourceId, List<LogListData> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.logitem, null);
		}
		
		//現在の行に設定するオブジェクト
		LogListData data = (LogListData)getItem(position);
		//イメージ
		ImageView image = (ImageView)convertView.findViewById(R.id.item_image);
		image.setImageBitmap(data.getImageData());
		//名前
		TextView text = (TextView)convertView.findViewById(R.id.item_name);
		text.setText(data.getTextData());
		//評価
		RatingBar rate = (RatingBar)convertView.findViewById(R.id.item_rating);
		rate.setRating(data.getRatingData());
		
		return convertView;
	}
}
