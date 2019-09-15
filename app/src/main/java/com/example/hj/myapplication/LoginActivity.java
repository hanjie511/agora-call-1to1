package com.example.hj.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hj.call.MyService;
import com.example.hj.call.RtmTokenBuilder;

public class LoginActivity extends AppCompatActivity {
    private RtcEngine rtcEngine;
    private RtmClient mRtmClient;
    EditText text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent myService=new Intent(LoginActivity.this, MyService.class);
        startService(myService);
        text=findViewById(R.id.username);
    }
    public void loginIn(View view){
        if(!"".equals(text.getText().toString())){
            registerStringUserCount();
        }else{
            Toast.makeText(this, "用户名不能为空！！！", Toast.LENGTH_SHORT).show();
        }

    }
    //注册String类型的RtcEngine账户的方法
    private void  registerStringUserCount(){
        try{
            rtcEngine= RtcEngine.create(LoginActivity.this, getString(R.string.agora_app_id), new IRtcEngineEventHandler() {
                @Override
                public void onLocalUserRegistered(int uid, String userAccount) {
                    System.out.println("LoginActivity-userAccount:"+userAccount);
                    ProApplication.setUsername(userAccount);
                    doLogin();
                }
            });
            rtcEngine.registerLocalUserAccount(getString(R.string.agora_app_id),text.getText().toString());
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    //登录RTM服务器的方法，登陆后便于发出通话邀请
    private void doLogin() {
        String appId = getString(R.string.agora_app_id);
        String appCertificate = getString(R.string.agora_app_certificate);
        int expireTimestamp = 0;
        RtmTokenBuilder token = new RtmTokenBuilder();
        String rtm_token="";
        try{
            rtm_token = token.buildToken(appId, appCertificate, ProApplication.getUsername(), RtmTokenBuilder.Role.Rtm_User, expireTimestamp);
            System.out.println("LoginActivity-rtm_token:"+rtm_token);
        }catch(Exception e){
            e.printStackTrace();
        }
        mRtmClient=ProApplication.getRtmClient();
        mRtmClient.login(rtm_token, ProApplication.getUsername(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(ProApplication.flag){
                            Toast.makeText(LoginActivity.this, "登录成功！！！", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            ProApplication.flag=false;
                        }
                    }
                });
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                System.out.println("errorInfo:"+errorInfo);
                final String str=errorInfo.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       if("RTM ERROR 8 LOGIN_ERR_ALREADY_LOGGED_IN".equals(str)){
                           if(ProApplication.flag){
                               Toast.makeText(LoginActivity.this, "登录成功！！！", Toast.LENGTH_SHORT).show();
                               Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                               startActivity(intent);
                               ProApplication.flag=false;
                           }
                       }else{
                           Toast.makeText(LoginActivity.this, "登录失败！！！", Toast.LENGTH_SHORT).show();
                       }
                    }
                });
            }
        });
    }
}
