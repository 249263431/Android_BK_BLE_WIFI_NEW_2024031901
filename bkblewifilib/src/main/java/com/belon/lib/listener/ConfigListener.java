package com.belon.lib.listener;

import com.belon.lib.error.ErrCode;

public abstract class ConfigListener {

    public void onConnect(){

    }
    public void onDisConnect(){

    }

    //配网成功
    public abstract void onSuccess();
    //配网成功
    public abstract void onFail(ErrCode errCode);
}
