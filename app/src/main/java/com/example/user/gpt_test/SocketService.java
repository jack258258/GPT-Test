package com.example.user.gpt_test;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketService extends Service {
    //接收器
    private ServiceReceiver serviceReceiver;
    private ServiceReceiver2 serviceReceiver2;
    private ServiceReceiver3 serviceReceiver3;
    private String success;

    //ActionString  發送
    private String actionString= "LogoActivity.RECEIVER";
    private Intent intent = new Intent(actionString);

    private String actionStringMan= "MainActivity.RECEIVER";
    private Intent main_intent = new Intent(actionStringMan);

    private String actionStringWait= "WaitActivity.RECEIVER";
    private Intent map_intent = new Intent(actionStringWait);

    private String actionStringMAP= "MAPActivity2.RECEIVER";
    private Intent updateintent = new Intent(actionStringMAP);

    //確認位置
    private String passengerLoaction;
    private String passengerLat;
    private String passengerLng;
    private String callDriverType;

    //SharedPreferences
    private SharedPreferences settings; //讀取Sharedpreference 物件內容getString(key, "unknow")，讀取被寫入的資料
    private static final String data_token = "DATA";
    //socket.io
    private Socket socket;
    //確認訂單
    private String onlocation;
    private String offlocation;
    private String cartid;
    private String passengerNum;
    private String passengerComment;
    private String payType;





    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        settings = getSharedPreferences(data_token,0);            //讀取Sharedpreference 物件內容getString(key, "unknow")，讀取被寫入的資料
        String token = settings.getString("token", "");

        Log.e("TOKEN-LOGO-Main", token);
        {
            try{
                IO.Options options = new IO.Options();
                options.forceNew=true;
                options.reconnection = false;
                options.query = "token="+token;  //token認證
                socket = IO.socket("http://172.104.110.249/", options);
                Log.e("Connecting",String.valueOf(socket));
                Log.e("Connecting","連線成功");
                Log.e("字串字串",options.query);
            }catch (Exception e){
                e.printStackTrace();
                Log.d("Error connecting","連線失敗");
            }
        }
        Log.i("mylog", "onCreate() ");
        //接收器註冊
        if(serviceReceiver == null){
            serviceReceiver = new ServiceReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("MaptoService.RECEIVER");
            registerReceiver(serviceReceiver, intentFilter);
        }
        if(serviceReceiver2 == null){
            serviceReceiver2 = new ServiceReceiver2();
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction("FilltoService.RECEIVER");
            registerReceiver(serviceReceiver2, intentFilter2);
        }
        if(serviceReceiver3 == null){
            serviceReceiver3 = new ServiceReceiver3();
            IntentFilter intentFilter3= new IntentFilter();
            intentFilter3.addAction("WaitoService.RECEIVER");
            registerReceiver(serviceReceiver3, intentFilter3);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("mylog", "onStartCommand");
        socket.connect();                       //連線
        socket.on("ConnectStatus", handling);   //連線狀態
        socket.on("GetNearstDriverGPS", Location); //接收最近司機資料
        socket.on("SubmitOrder", SubmitOrder);      //接收回傳司機資料
        socket.on("UpdateTaxiGPS", UpdateTaxiGPS);      //接收回傳司機資料
        socket.on("CancelOrder", CancelOrder);
        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.i("mylog", "onDestroy");
        super.onDestroy();
    }
    //解析Socket.io連線狀態回傳訊息
    private Emitter.Listener handling = new Emitter.Listener() {  //後台發送，接收
        @Override
        public void call( final Object... args ) {
            JSONObject data = (JSONObject) args[0];
            String JSONdata = data.toString();
            //Json 字串解析'
            String json = JSONdata;
            try {
                JSONObject parentObject = new JSONObject(json);
                success = parentObject.getString("success");
                String message = parentObject.getString("message");
                Log.d("JSON-connect-socket:", success);
                //廣播 sendLogoActivity
                intent.putExtra("success", success);            //broadcast 傳送
                sendBroadcast(intent);

                Log.d("JSON-connect:", success);//顯示在電腦上
                Log.d("JSON message:", message);
            } catch (JSONException e) {
                Log.d("JSONException", "有錯誤");
                e.printStackTrace();

            }
        }

    };

    //解析司機最近的資料(時間)
    private Emitter.Listener Location = new Emitter.Listener() {  //後台發送，接收
        @Override
        public void call( final Object... args ) {
            JSONObject data = (JSONObject) args[0];
            try {
                Log.i("Json-time0",String.valueOf(data));
                String exclusiveCarTeam_time = new JSONObject(data.getString("exclusiveCarTeam")).getString("time_from_here_to_you");
                Log.i("Json-time",exclusiveCarTeam_time);
                String otherCarTeam_time = new JSONObject(data.getString("otherCarTeam")).getString("time_from_here_to_you");
                Log.i("Json-time1",otherCarTeam_time);
                //廣播 send MainActivity
                try{
                    // delay 1 second，不然會建立不成功
                    Thread.sleep(500);
                    main_intent.putExtra("exclusiveCarTeam_time", exclusiveCarTeam_time);            //broadcast 傳送
                    main_intent.putExtra("otherCarTeam_time", otherCarTeam_time);
                    sendBroadcast(main_intent);
                    Log.i("Json-time-send",String.valueOf(main_intent));

                } catch(InterruptedException e){
                    e.printStackTrace();

                }
            } catch (JSONException e) {
                Log.d("LocationJSONException", "有錯誤");
                e.printStackTrace();
            }
        }

    };
    //接收並解析，配對司機的資料
    /*
     1. 經度、2. 緯度、3. 司機姓名、4. 司機電話、5. 司機評價、6. 單號、7. 呼號、8. 車牌、9. 車隊名、10. 預估時間
    解析完後，利用廣播將資料傳到MainActivity
                                */
    private Emitter.Listener SubmitOrder = new Emitter.Listener() {  //後台發送，接收
        @Override
        public void call( final Object... args ) {
            JSONObject data = (JSONObject) args[0];
            try {
                Log.i("Json-time0",String.valueOf(data));
                String success = data.getString("success");
                Double driverLat = new JSONObject(data.getString("taxiInfo")).getDouble("drivarLat");
                Double driverLng = new JSONObject(data.getString("taxiInfo")).getDouble("driverLng");
                String driverName = new JSONObject(data.getString("taxiInfo")).getString("driverName");
                String driverTel = new JSONObject(data.getString("taxiInfo")).getString("driverTel");
                String driverStar = new JSONObject(data.getString("taxiInfo")).getString("driverStar");
                String orderNumber = new JSONObject(data.getString("taxiInfo")).getString("orderNumber");
                String callNumber = new JSONObject(data.getString("taxiInfo")).getString("callNumber");
                String carILicense = new JSONObject(data.getString("taxiInfo")).getString("carILicense");
                String carTeamName = new JSONObject(data.getString("taxiInfo")).getString("carTeamName");
                String estimatedTime = new JSONObject(data.getString("taxiInfo")).getString("estimatedTime");
                Log.i("Json-Driverinformation",driverLat+" "+driverLng+" "+driverName+" "+driverTel+" "+driverStar+" "+orderNumber+" "+callNumber+" "+
                        carILicense+" "+carTeamName+" "+estimatedTime);
                //廣播 send WaitActivity
                try{
                    // delay 1 second
                    Thread.sleep(500);
                    map_intent.putExtra("driverLat", driverLat);
                    map_intent.putExtra("driverLng", driverLng);
                    map_intent.putExtra("success", success);
                    map_intent.putExtra("driverName", driverName);            //broadcast 傳送
                    map_intent.putExtra("callNumber", callNumber);
                    map_intent.putExtra("carILicense", carILicense);
                    map_intent.putExtra("carTeamName", carTeamName);
                    map_intent.putExtra("estimatedTime", estimatedTime);
                    sendBroadcast(map_intent);
                    Log.i("Json-time-send",String.valueOf(map_intent));

                } catch(InterruptedException e){
                    e.printStackTrace();

                }
            } catch (JSONException e) {
                Log.d("LocationJSONException", "有錯誤");
                e.printStackTrace();
            }
        }

    };


    private Emitter.Listener UpdateTaxiGPS = new Emitter.Listener() {  //後台發送，接收
        @Override
        public void call( final Object... args ) {
            JSONObject data = (JSONObject) args[0];
            try {
                Double driverLat = new JSONObject(data.getString("taxiInfo")).getDouble("drivarLat");
                Double driverLng = new JSONObject(data.getString("taxiInfo")).getDouble("driverLng");
                Log.d("JSON-Update", driverLat+" "+driverLng);
                //廣播 sendMapActivity2
                updateintent.putExtra("driverLat", driverLat);            //broadcast 傳送
                updateintent.putExtra("driverLng", driverLng);
                sendBroadcast(updateintent);
            } catch (JSONException e) {
                Log.d("JSONException", "有錯誤");
                e.printStackTrace();
            }
        }

    };

    private Emitter.Listener CancelOrder = new Emitter.Listener() {  //後台發送，接收
        @Override
        public void call( final Object... args ) {
            JSONObject data = (JSONObject) args[0];
            try {
                String cancelsuccess= data.getString("success");
                if(cancelsuccess.equals("true")) {
                    Log.i("取消完成", cancelsuccess);
                }else {
                    Log.i("取消失敗", cancelsuccess);
                }
            } catch (JSONException e) {
                Log.d("LocationJSONException", "有錯誤");
                e.printStackTrace();
            }
        }

    };

    @NonNull
    private JSONObject getpasJsonObject() {
        JSONObject data = new JSONObject();
        try {
            data.put("passengerLoaction", passengerLoaction);
            data.put("passengerLat", passengerLat);
            data.put("passengerLng", passengerLng);
            data.put("callDriverType", callDriverType);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("觀看經緯度Service",String.valueOf(data));
        return data;
    }

    //放入乘客訂單資料。準備傳送給後台
    @NonNull
    private JSONObject getOrderJsonObject() {
        JSONObject data = new JSONObject();
        try {
            data.put("onLocation", onlocation);                 //上車地點
            data.put("offLocation", offlocation);                //下車地點
            data.put("callCarTeamID", cartid);                  //車隊號
            data.put("passengerNum", passengerNum);          //乘客人數
            data.put("passengerComment", passengerComment); //乘客備註
            data.put("payType", payType);                     //付款方式

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("觀看地點", String.valueOf(data));
        return data;
    }
    //Service廣播接收器，ServiceReceiver: 來自MapActivtiy
    //ServiceReceiver2: 來自FillOrderActivity
    public class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //讀取包含在Intent物件中的資料
            //receive ConfirmLocation imformation

            String message = intent.getStringExtra("message");
            passengerLoaction = intent.getStringExtra("passengerLoaction");
            passengerLat = intent.getStringExtra("passengerLat");
            passengerLng = intent.getStringExtra("passengerLng");
            callDriverType = intent.getStringExtra("callDriverType");
            Log.i("觀看經緯度Service",message);
            if(message!= null){
                if(message.equals("ConfirmLocation")){
                    //Log.i("message",message);
                    JSONObject data = getpasJsonObject();
                    socket.emit("GetNearstDriverGPS", data);
                }
            }
        }
    }

    public class ServiceReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            onlocation = intent.getStringExtra("onlocation");
            offlocation = intent.getStringExtra("offlocation");
            cartid = intent.getStringExtra("cartid");
            passengerNum = intent.getStringExtra("passengerNum");
            passengerComment = intent.getStringExtra("passengerComment");
            payType = intent.getStringExtra("payType");
            Log.i("觀看經緯度Service2",message);
            if(message!= null){
                if(message.equals("ConfirmOrderToSend")){
                    JSONObject data = getOrderJsonObject();
                    Log.i("訂單紀錄", String.valueOf(data));
                    socket.emit("SubmitOrder", data);
                }else{
                    Log.i("沒有message","");
                }
            }
        }
    }

    public class ServiceReceiver3 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("CancelOrder");
            Log.i("觀看經緯度Service2",message);
            if(message.equals("CancelOrder")){
                socket.emit("CancelOrder", "");
            }else{
                Log.i("沒有message","");
            }
        }
    }
}
