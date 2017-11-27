package com.example.user.gpt_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class LogoActivity extends AppCompatActivity {

    private Intent intent;

    //SharedPreferences
    private SharedPreferences settings; //讀取Sharedpreference 物件內容getString(key, "unknow")，讀取被寫入的資料
    private static final String data_token = "DATA";

    private LogoReceiver logoReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        logoReceiver = new LogoReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LogoActivity.RECEIVER");
        registerReceiver(logoReceiver, intentFilter);



    }

    @Override
    protected void onStart() {
        settings = getSharedPreferences(data_token,0);            //讀取Sharedpreference 物件內容getString(key, "unknow")，讀取被寫入的資料
        String token = settings.getString("token", "");
        if(token.isEmpty()) {
            Intent intent_Login = new Intent();
            intent_Login.setClass(LogoActivity.this, LoginActivity.class);
            startActivity(intent_Login);
            finish();
        }else {
            intent = new Intent(LogoActivity.this, SocketService.class);
            startService(intent);
        }
        super.onStart();
        Log.i("LOGO", "onStart");
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public class LogoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //讀取包含在Intent物件中的資料
            String success = intent.getStringExtra("success");
            String msuccess = "true";

            Log.i("Activityreceive",success+"XXX");

            if(msuccess.equals(success)){
                Intent intent_Login = new Intent();
                intent_Login.setClass(LogoActivity.this, MapsActivity.class);
                startActivity(intent_Login);
                finish();
                unregisterReceiver(logoReceiver);
            }else {
                Intent intent_Login = new Intent();
                intent_Login.setClass(LogoActivity.this, LoginActivity.class);
                startActivity(intent_Login);
                finish();

                unregisterReceiver(logoReceiver);

            }
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }
}
