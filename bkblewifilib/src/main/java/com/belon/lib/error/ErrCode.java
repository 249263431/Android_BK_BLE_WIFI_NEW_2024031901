package com.belon.lib.error;

import androidx.annotation.NonNull;

public enum ErrCode{
    CRC_DEV_VERTIFY_FAIL,//
    CRC_APP_VERTIFY_FAIL,//
    CON_TEIMOUT,//连接路由器超时了
    PWD_ERROR,//SSID 或 密码错误
    CON_FAIL,
    UUID_NOT_EXIST,
    ConnectErr//升级过程中，连接断开了
    ;

    @NonNull
    @Override
    public String toString() {
        if(this.ordinal() == CRC_DEV_VERTIFY_FAIL.ordinal()){
//            return "Dev CRC失败";
            return "Device CRC failure";
        }

        if(this.ordinal() == CON_TEIMOUT.ordinal()){
            return "Router connection timeout\n";
        }

        if(this.ordinal() == PWD_ERROR.ordinal()){
            return "Failed to connect to the router! SSID or password incorrect!";
        }

        if(this.ordinal() == CON_FAIL.ordinal()){
            return "Connect to the device failure";
        }

        if(this.ordinal() == UUID_NOT_EXIST.ordinal()){
            return "Network configuration service does not exist";
        }

        if(this.ordinal() == CRC_APP_VERTIFY_FAIL.ordinal()){
            return "CRC verification failed";
        }


        return "Unknown error";
    }
}
