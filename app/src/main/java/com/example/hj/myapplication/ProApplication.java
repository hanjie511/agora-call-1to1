package com.example.hj.myapplication;

import android.app.Application;

import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmMessage;

public class ProApplication extends Application {
    private static RtmClient rtmClient;//全局rtmClient,方便监听程序在运行过程中收到的通话邀请
    public static  boolean flag=true;
    public static int getRtmState() {
        return rtmState;
    }

    public static void setRtmState(int rtmState) {
        ProApplication.rtmState = rtmState;
    }

    private static int rtmState=1;
    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ProApplication.username = username;
    }

    public  static String username;
    @Override
    public void onCreate() {
        super.onCreate();
        initRTM();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
    public void initRTM() {
        try {
            rtmClient = RtmClient.createInstance(getApplicationContext(), getString(R.string.agora_app_id), new RtmClientListener() {

                @Override
                public void onTokenExpired() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onMessageReceived(RtmMessage arg0, String arg1) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onConnectionStateChanged(int arg0, int arg1) {
                    // TODO Auto-generated method stub
//                    state:1,SDK 未连接到 Agora RTM 系统。
//                    state:2,SDK 正在登录 Agora RTM 系统。。
//                    state:3,SDK 已登录 Agora RTM 系统。。。
//                    state:4,SDK 正在尝试自动重连 Agora RTM 系统。。
//                    state:5,另一实例已经以同一用户 ID 登录 Agora RTM 系统。
//                    reason1:SDK 正在登录 Agora RTM 系统。
//                    reason2 SDK 登录 Agora RTM 系统成功。
//                    reason3:SDK 登录 Agora RTM 系统失败。
//                    reason4:SDK 无法登录 Agora RTM 系统超过 6 秒，停止登录。可能原因：用户正处于
//                    reason5:SDK 与 Agora RTM 系统的连接被中断。
//                    reason6:用户已调用 logout() 方法登出 Agora RTM 系统。
//                    reason7:SDK 被服务器禁止登录 Agora RTM 系统。
//                    reason8:另一个用户正以相同的用户 ID 登陆 Agora RTM 系统。
                    System.out.println("onConnectionStateChanged-state:"+arg0);
                    setRtmState(arg0);
                    System.out.println("onConnectionStateChanged-reason:"+arg1);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("You need to check the RTM init process.");
        }
    }
    public static RtmClient getRtmClient(){
        return rtmClient;
    }

}
