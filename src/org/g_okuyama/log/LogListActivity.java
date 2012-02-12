package org.g_okuyama.log;

import java.util.ArrayList;

import org.g_okuyama.log.R;

import android.app.TabActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class LogListActivity extends TabActivity implements OnTabChangeListener{
    DatabaseHelper dbhelper = null;
    
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
        
        ArrayList<LogListData> loglist = new ArrayList<LogListData>();

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

            	int id = Integer.valueOf(c.getString(1/*category*/));
            	logitem.setImageID(id);
            	logitem.setTextData(c.getString(2/*name*/));
            	String rate = c.getString(4/*evaluate*/);
            	if(!rate.equals("")){
            		float f = Float.valueOf(rate);
            		logitem.setRatingData(f);
            		loglist.add(logitem);
            	}

            	c.moveToPrevious();
            }
            c.close();
        }
        
        LogArrayAdapter adapter = new LogArrayAdapter(this, android.R.layout.simple_list_item_1, loglist);
        ListView listview = (ListView)findViewById(R.id.log_list_tab1);
        listview.setAdapter(adapter);
    }

    public void onTabChanged(String arg0) {
        
    }
}
