package com.belon.ble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.belon.lib.ConfigHelper;
import com.belon.lib.error.ErrCode;
import com.belon.lib.listener.ConfigListener;
import com.belon.lib.listener.LogListener;
import com.belon.lib.service.ConfigService;
import com.belon.util.DateUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int SELECT_DEV_REQ = 2;
    private static final int SELECT_SSID_REQ = 3;

    EditText etBLEName,etSSID,etPwd,etLog;
    Button btnSearchBLE,btnSearchSSID,bthStartConfig;
    CheckBox cbPwd;
    private ConfigHelper configHelper;

    private String bleName;
    private String bleMac;
    private String wifiSSID;
    private byte[] wifiSSIDBytes;

    private View that;

    private static final String SP_FILE = "SP_FILE";
    private static final String SP_KEY_BLE_NAME = "SP_KEY_BLE_NAME";
    private static final String SP_KEY_BLE_MAC = "SP_KEY_BLE_MAC";
    private static final String SP_KEY_WIFI_SSID = "SP_KEY_WIFI_SSID";
    private static final String SP_KEY_WIFI_PWD = "SP_KEY_WIFI_PWD";
    private static final String SP_KEY_CHECKBOX_PWD= "SP_KEY_CHECKBOX_PWD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        loadConfig();

        configHelper = new ConfigHelper();
        configHelper.registerLogListener(this,logListener);
        configHelper.registerConfigListener(this,configListener);

        cbPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "isChecked：" + isChecked);
                if(isChecked){
                    etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    ConfigListener configListener = new ConfigListener() {

        @Override
        public void onSuccess() {
            appendLog("Network configuration successful");
            Toast.makeText(MainActivity.this, "Network configuration successful", Toast.LENGTH_SHORT).show();
            enableView(true);
        }

        @Override
        public void onFail(ErrCode errCode) {
            appendLog("Network configuration failed,reason:"+errCode.toString());
            enableView(true);
            Toast.makeText(MainActivity.this, "Network configuration failed,reason:"+errCode.toString(), Toast.LENGTH_SHORT).show();

            configHelper.setPassword(etPwd.getText()+"")
                    .setSsid(wifiSSIDBytes)
                    .setDeviceAddress(bleMac)
                    .setTimeoutMilliscond(10*1000)//40s
                    .startConfig(MainActivity.this, ConfigService.class);

        }
    };

    LogListener logListener = new LogListener() {
        @Override
        public void logInfo(String tag, String message) {
            Log.d(tag, message) ;
            appendLog(message);
        }

        @Override
        public void logError(String tag, Exception exception) {
            exception.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            appendLog(sw.toString());
        }
    };

    private void enableView(boolean enable){
        etBLEName.setEnabled(enable);
        btnSearchBLE.setEnabled(enable);
        etSSID.setEnabled(enable);
        btnSearchSSID.setEnabled(enable);
        etPwd.setEnabled(enable);
        bthStartConfig.setEnabled(enable);
        cbPwd.setEnabled(enable);
    }

    private void findViewById(){
        etBLEName = findViewById(R.id.et_ble_name);
        btnSearchBLE = findViewById(R.id.btn_search_ble);
        etSSID = findViewById(R.id.et_ssid);
        btnSearchSSID = findViewById(R.id.btn_search_ssid);
        etPwd = findViewById(R.id.et_pwd);
        bthStartConfig = findViewById(R.id.btn_start_config);
        etLog = findViewById(R.id.et_log);
        cbPwd = findViewById(R.id.cb_pwd);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_search_ble:
                Intent intent = new Intent(this,SearchBleDevActivity.class);
                startActivityForResult(intent,SELECT_DEV_REQ);
                break;
            case R.id.btn_search_ssid:
                Intent intentSSID = new Intent(this,SearchWiFiActivity.class);
                startActivityForResult(intentSSID,SELECT_SSID_REQ);
                break;
            case R.id.btn_start_config:
                saveConfig();
                wifiSSID = etSSID.getText()+"";
//                if(wifiSSID.length() == 0 || bleMac.length() == 0 || (etPwd.getText()+"").length() == 0){
                if(wifiSSID.length() == 0 || bleMac.length() == 0){
//                    Toast.makeText(this, "请先设置BLEName、WIFi名字 、密码", Toast.LENGTH_SHORT).show();

                     Toast.makeText(this, "Please set BLE Name, Wi-Fi name, and password first", Toast.LENGTH_SHORT).show();
                    return;
                }
                configHelper.setPassword(etPwd.getText()+"")
                        .setSsid(wifiSSIDBytes)
                        .setDeviceAddress(bleMac)
                        .setTimeoutMilliscond(10*1000)//40s
                        .startConfig(this, ConfigService.class);
                enableView(false);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //延迟5秒后执行的操作
                        Message message = mHandler.obtainMessage();
                        message.obj = "log";
                        message.what = 100;
                        mHandler.sendMessage(message);
                    }
                }, 20000); //延迟5秒


                break;
            case R.id.btn_clear_log:
                etLog.setText("");
                Toast.makeText(this, "Clear log successful", Toast.LENGTH_SHORT).show();
                break;
 
        }
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case SELECT_DEV_REQ: {
                bleName = data.getStringExtra("DeviceName");
                bleMac = data.getStringExtra("DeviceMac");
                if(bleName == null){
                    bleName = "Unkonw Device";
                }
                etBLEName.setText(bleName+"");
                break;
            }
            case SELECT_SSID_REQ: {
                wifiSSID = data.getStringExtra("SSID");
                wifiSSIDBytes = data.getByteArrayExtra("SSID_DATA");
                etSSID.setText(wifiSSID+"");

                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        configHelper.unRegisterLogListener(this);
        configHelper.unRegisterConfigListener(this);
    }


    private void loadConfig(){

        SharedPreferences sharedPreferences = getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        bleName = sharedPreferences.getString(SP_KEY_BLE_NAME,"");
        if(bleName.length() > 0)
            etBLEName.setText(bleName);

        wifiSSID = sharedPreferences.getString(SP_KEY_WIFI_SSID,"");
        if(wifiSSID.length() > 0) {
            etSSID.setText(wifiSSID);
        }else {
            wifiSSID = getCurrentSSID();
            etSSID.setText(wifiSSID+"");
        }

        String pwd = sharedPreferences.getString(SP_KEY_WIFI_PWD,"");
        if(pwd.length() > 0)
            etPwd.setText(pwd);
        bleMac = sharedPreferences.getString(SP_KEY_BLE_MAC,"");

        wifiSSIDBytes = (etSSID.getText()+"").getBytes(StandardCharsets.UTF_8);

        boolean isChecked = sharedPreferences.getBoolean(SP_KEY_CHECKBOX_PWD,false);
        cbPwd.setChecked(isChecked);
        if(isChecked){
            etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

    }



    private void saveConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString(SP_KEY_BLE_NAME,etBLEName.getText()+"");
        editor.putString(SP_KEY_WIFI_SSID,etSSID.getText()+"");
        editor.putString(SP_KEY_BLE_MAC,bleMac+"");
        editor.putString(SP_KEY_WIFI_PWD,etPwd.getText()+"");
        editor.putBoolean(SP_KEY_CHECKBOX_PWD,cbPwd.isChecked());
        editor.commit();//提交修改
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_ERROR:
                    String errrMsg = (String) msg.obj;
                    Toast.makeText(MainActivity.this, errrMsg, Toast.LENGTH_SHORT).show();
                case MSG_LOG_UI:
                    String log = (String) msg.obj;
                    if(etLog.getText().length()>10000){//太长了，截掉一些
                        String text = etLog.getText()+"";
                        etLog.setText(text.substring(text.length() - 500,text.length() - 1));
                    }
                    etLog.setText(etLog.getText()+ DateUtil.getCurrentDateStr()+"   "+log+"\r\n");
                    etLog.setSelection(etLog.getText().length(), etLog.getText().length());
                    etLog.setMovementMethod(ScrollingMovementMethod.getInstance());
                    break;
                case 100:
                        enableView(true);
                    break;
            }
        }
    };
    private void appendLog(String log){

        Message message = mHandler.obtainMessage();
        message.obj = log;
        message.what = MSG_LOG_UI;
        mHandler.sendMessage(message);
    }


    public String getCurrentSSID() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return "";
    }

    private static final int MSG_LOG_UI = 99;
    private static final int MSG_ERROR = 98;
}
