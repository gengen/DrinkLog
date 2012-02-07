package org.g_okuyama.log;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DrinkLogActivity extends Activity {
    public static final String TAG = "DrinkLog";
    
    public static final int CATEGORY_WHISKEY = 0;
    public static final int CATEGORY_COCKTAIL = 1;
    public static final int CATEGORY_WINE = 2;
    public static final int CATEGORY_SHOCHU = 3;
    public static final int CATEGORY_SAKE = 4;
    public static final int CATEGORY_BEER = 5;
    public static final int CATEGORY_BRANDY = 6;
    public static final int CATEGORY_OTHER = 7;

    private EditText mEditText = null;
    private int mItemIdx = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setListener();
    }
    
    private void setListener(){
        Button register_btn = (Button)this.findViewById(R.id.register);
        register_btn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                registerConfirm();
            }
        });

        Button refer_btn = (Button)this.findViewById(R.id.refer);
        refer_btn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(DrinkLogActivity.this, LogListActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void registerConfirm(){
        final String[] itemList = getResources().getStringArray(R.array.category_array);
        
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.dialog_category)
        .setSingleChoiceItems(itemList, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mItemIdx = which;
            }
        })
        .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DrinkLogActivity.this, RegisterActivity.class);
                intent.putExtra("category", mItemIdx);
                startActivity(intent);
            }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //‰½‚à‚µ‚È‚¢
            }
        })
       .show();        
    }

    public void finish(){
        new AlertDialog.Builder(this)
        .setTitle(R.string.finish)
        .setMessage(R.string.finish_confirm)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                System.exit(RESULT_OK);
            }
        })
        .setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //‰½‚à‚µ‚È‚¢
                return;
            }
        })
        .show();        
    }
}