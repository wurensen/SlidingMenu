package com.yuexunit.slidingmenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.yuexunit.slidingmenu.view.SlidingMenu;


public class MainActivity extends Activity {

    private SlidingMenu slidingMenu;

    private ListView menuLv;
    private ListView contentLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        slidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);

        menuLv = (ListView) findViewById(R.id.menuLv);
        contentLv = (ListView) findViewById(R.id.contentLv);
        int count = 20;
        String[] data = new String[count];
        for (int i = 0; i < count; i++) {
            data[i] = "item" + i;
        }
        menuLv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                data));
        contentLv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                data));
    }

    public void showMenu(View view) {
        slidingMenu.toggleMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
