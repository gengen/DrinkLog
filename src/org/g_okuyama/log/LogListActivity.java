package org.g_okuyama.log;

import org.g_okuyama.log.R;

import android.app.TabActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
        tab1.setIndicator("date", /*�A�C�R���w��*/null);
        tab1.setContent(R.id.first_content);
        tabs.addTab(tab1);
        tabs.setCurrentTab(0);
        
        TabSpec tab2 = tabs.newTabSpec("tab2");
        tab2.setIndicator("category", /*�A�C�R���w��*/null);
        tab2.setContent(R.id.second_content);
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

        if(rowcount == 0){
            //�ۑ��f�[�^���Ȃ��ꍇ
        }else{
            c.moveToLast();
            for(int i = 0; i < rowcount; i++){
                //TODO:�\�����X�g�쐬
                TextView view = (TextView)findViewById(R.id.log_name);
                view.setText(c.getString(1));
                c.moveToPrevious();
            }
            c.close();
        }
    }

    public void onTabChanged(String arg0) {
        
    }
}
