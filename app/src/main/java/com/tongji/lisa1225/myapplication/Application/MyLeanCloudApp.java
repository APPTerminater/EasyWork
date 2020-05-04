package com.tongji.lisa1225.myapplication.Application;

import android.app.Application;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;

public class MyLeanCloudApp extends Application {
    private String userName,email,occu,password;
    private boolean firstLogin;

    public String getUserName() {
        return userName;
    }
    public String getEmail() {
        return email;
    }
    public String getOccu() {
        return occu;
    }
    public String getPassword()
    {
        return password;
    }

    public Boolean getFirstLogin(){ return firstLogin; }

    public void setUserInfo(String email,String userName, String occu, String password) {
        this.userName = userName;
        this.email = email;
        this.occu = occu;
        this.password = password;
    }

    public void setFirstLogin(boolean firstLogin)
    {
        this.firstLogin = firstLogin;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 在 AVOSCloud.initialize() 之前调用
        AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
        // 提供 this、App ID、App Key、Server Host 作为参数
        // 注意这里千万不要调用 cn.leancloud.core.AVOSCloud 的 initialize 方法，否则会出现 NetworkOnMainThread 等错误。
        AVOSCloud.initialize(this, "WRK92AAAMomdzkt0d4Tcn4sU-gzGzoHsz", "i0hedRjpSPDXAt1fOzyAgR5U");
    }
}

