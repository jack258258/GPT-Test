package com.example.user.gpt_test.BaseView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;


import com.example.user.gpt_test.Navigation.ContactActivity;
import com.example.user.gpt_test.Navigation.PersonalActivity;
import com.example.user.gpt_test.Navigation.RecordActivity;
import com.example.user.gpt_test.R;

import java.util.ArrayList;
import java.util.Map;

public class BaseActivity extends AppCompatActivity{
    protected DrawerLayout drawerLayout;
    protected FrameLayout frameLayout;
    private  Toolbar toolbar;
    private ListView listview;
    private String[] navigations;
    private ArrayList<Map<String,Object>> navigationData;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    /**
     * 重写setContentView，以便于在保留侧滑菜单的同时，让子Activity根据需要加载不同的界面布局
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID){
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        frameLayout = (FrameLayout) drawerLayout.findViewById(R.id.content_frame);
        // 将传入的layout加载到activity_base的content_frame里面
        getLayoutInflater().inflate(layoutResID, frameLayout, true);
        super.setContentView(drawerLayout);
        setUpToolBar();



        navigations = getResources().getStringArray(R.array.navigation_array);
        listview = (ListView) findViewById(R.id.draw_list);

        MyAdapter adapter = new MyAdapter(BaseActivity.this, navigations);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(listviewListener);


    }

        public ListView.OnItemClickListener listviewListener =
                new ListView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        switch (position) {
                            case 0:
                                Intent iPer = new Intent();
                                iPer.setClass(getApplicationContext(), PersonalActivity.class);
                                startActivity(iPer);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                            case 1:
                                Intent iRec = new Intent();
                                iRec.setClass(getApplicationContext(), RecordActivity.class);
                                startActivity(iRec);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                            case 2:
                                Intent iCon = new Intent();
                                iCon.setClass(getApplicationContext(), ContactActivity.class);
                                startActivity(iCon);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                break;
                            case 3:
                                //登出
                                break;

                        }
                    }

        };
    //ToolBar初始化
    private  void setUpToolBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
    }


    //点击ToolBar的home按钮打开Navigation Drawer省略ActionBarDrawerToggle的部分了，影响不是特别大。实际应用中再去添加。
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
