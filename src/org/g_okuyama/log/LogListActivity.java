package org.g_okuyama.log;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class LogListActivity extends TabActivity implements OnTabChangeListener{
	public static final String TAG = "DrinkLog";
	public static final int REQUEST_CODE = 1111;
	public static final int RESPONSE_DELETE = 7777;
	
    DatabaseHelper dbhelper = null;
    
    String mDate = "";
    ArrayList<LogListData> mLogList = null; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tabs);
        setTab();
        setLogList();
    }
    
    private void setTab(){
        TabHost tabs = getTabHost();
        tabs.setOnTabChangedListener(this);
        
        TabSpec tab1 = tabs.newTabSpec("tab1");
        tab1.setIndicator("date", 
                getResources().getDrawable(android.R.drawable.ic_menu_my_calendar));
        tab1.setContent(R.id.log_list_tab1);
        tabs.addTab(tab1);
        tabs.setCurrentTab(0);
        
        TabSpec tab2 = tabs.newTabSpec("tab2");
        tab2.setIndicator("category", 
                getResources().getDrawable(android.R.drawable.ic_menu_agenda));
        tab2.setContent(R.id.log_list_tab2);
        tabs.addTab(tab2);
    }
    
    private void setLogList(){
        if(dbhelper == null){
            dbhelper = new DatabaseHelper(this);
        }
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String query = "select * from logtable;";
        Cursor c = db.rawQuery(query, null);
        int rowcount = c.getCount();
        
        mLogList = new ArrayList<LogListData>();

        if(rowcount == 0){
            //保存データがない場合
        }else{
            c.moveToLast();
            for(int i = 0; i < rowcount; i++){
                /*
                 * 表示リスト作成
                 * ・カテゴリイメージ、名前、評価
                 */
            	LogListData logitem = new LogListData();
            	
            	String curDate = c.getString(9/*date*/);
            	if(!curDate.equals(mDate)){
                	//日付入れる
            		logitem.setImageID(-9999);
            		logitem.setTextData(curDate);
            		mDate = curDate;
            		mLogList.add(logitem);
            		i--;
            		continue;
            	}

            	int dbid = c.getInt(0/*id*/);
            	logitem.setDBID(dbid);
            	int id = Integer.valueOf(c.getString(1/*category*/));
            	logitem.setImageID(id);
            	logitem.setTextData(c.getString(2/*name*/));
            	String rate = c.getString(4/*evaluate*/);
            	if(!rate.equals("")){
            		float f = Float.valueOf(rate);
            		logitem.setRatingData(f);
            		mLogList.add(logitem);
            	}

            	c.moveToPrevious();
            }
            c.close();
        }
        
        LogArrayAdapter adapter = new LogArrayAdapter(this, android.R.layout.simple_list_item_1, mLogList);
        ListView listview = (ListView)findViewById(R.id.log_list_tab1);
        listview.setAdapter(adapter);
        
		listview.setOnItemClickListener(new ClickAdapter());
		listview.setOnItemLongClickListener(new LongClickAdapter());
    }

    public void onTabChanged(String arg0) {
        
    }
    
    
    private class ClickAdapter implements OnItemClickListener{
        public void onItemClick(AdapterView<?> adapter,
                View view, int position, long id) {
        	if(mLogList == null){
        		return;
        	}
        	
        	Log.d(TAG, "position = " + position);

        	Intent intent = new Intent(LogListActivity.this, LogDetailActivity.class);
        	LogListData logitem = mLogList.get(position);
        	intent.putExtra("dbid", logitem.getDBID());
        	startActivityForResult(intent, REQUEST_CODE);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode == REQUEST_CODE){
    		if(resultCode == RESPONSE_DELETE){
    			mLogList.clear();
    			mDate = "";
        		//再表示
        		setLogList();    			
    		}
    	}
    }
    
    private class LongClickAdapter implements OnItemLongClickListener{
    	int position = -1;
    	
		public boolean onItemLongClick(AdapterView<?> adapter, View view,
				int pos, long id) {
        	//TODO:データがない場合
			/*
			if(savedata == null || savedata[0].equalsIgnoreCase("No data")){
        		return true;
        	}
        	*/

			position = pos;

			new AlertDialog.Builder(LogListActivity.this)
				.setTitle(R.string.list_alert_select)
				.setItems(R.array.list_alert_array, new DialogInterface.OnClickListener() {
				
					public void onClick(DialogInterface dialog, int item) {
						switch(item){
						case 0://削除
							delete(position);
							break;
						case 1://キャンセル
							break;
						}
					}
				}).show();				
			
			return true;
		}
    }
    
    private void delete(int position){
    	final int pos = position;
        	
    	new AlertDialog.Builder(this)
        	.setTitle(R.string.cancel_confirm_title)
        	.setMessage(getString(R.string.list_alert_confirm))
        	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				//ログ削除
    				removeData(pos);
    				mLogList.clear();
    				mDate = "";
    				//ログリスト再表示
    				setLogList();
    			}
    		})
    		.setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				//何もしない
    			}
    		})
    		.show();    
    }
    
    private void removeData(int position){
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();

    	LogListData logitem = mLogList.get(position);
    	int id = logitem.getDBID();
    	
    	db.delete("logtable", "rowid = ?", new String[]{Integer.toString(id)});
    }
}
