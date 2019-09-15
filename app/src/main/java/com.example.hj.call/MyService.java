package com.example.hj.call;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import com.example.hj.myapplication.ProApplication;
import com.example.hj.myapplication.VoicCallActivity;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallEventListener;
import io.agora.rtm.RtmCallManager;

public class MyService extends Service {
	RtmCallManager rtmCallManager;
    LocalInvitation localInvitation;
    RemoteInvitation myRemoteInvitation;
    String name="";//存放被叫者的用户名
    String channelId="";//存放频道id
    String callerId="";//存放呼叫者的用户id
    ServiceBroadCast serviceBroadCast;
    private String msg ="";
    private String calleeName="";
    RtmCallEventListener rtmCallEventListener=new RtmCallEventListener() {
        @Override
        //返回给主叫的回调：被叫已收到呼叫邀请。
        public void onLocalInvitationReceivedByPeer(LocalInvitation localInvitation) {
        		
        }

        @Override
        //返回给主叫的回调：被叫已接受呼叫邀请。
        public void onLocalInvitationAccepted(LocalInvitation localInvitation, String s) {
            System.out.println("主叫：被叫接受了通话邀请");
            Intent intent=new Intent("callService");
            intent.putExtra("msg","accept_call");
            sendBroadcast(intent);
        }

        @Override
        //返回给主叫的回调：被叫已拒绝呼叫邀请。
        public void onLocalInvitationRefused(LocalInvitation localInvitation, String s) {
            System.out.println("主叫：被叫拒绝了通话邀请");
            Intent intent=new Intent("callService");
            intent.putExtra("msg","refuse_call");
            sendBroadcast(intent);
        }
        @Override
        //返回给主叫的回调：呼叫邀请已被成功取消。
        public void onLocalInvitationCanceled(LocalInvitation localInvitation) {
            System.out.println("主叫：被叫呼叫邀请已被成功取消");
        }

        @Override
        //返回给主叫的回调：发出的呼叫邀请过程失败。
        public void onLocalInvitationFailure(LocalInvitation localInvitation, int i) {

        }

        @Override
        //返回给被叫的回调：收到一条呼叫邀请。
        public void onRemoteInvitationReceived(RemoteInvitation remoteInvitation) {
                    System.out.println("被叫：我收到了通话邀请");
                    myRemoteInvitation=remoteInvitation;
                    channelId=remoteInvitation.getChannelId();
                    callerId=remoteInvitation.getContent();
                    System.out.println("channelId:"+channelId);
                    Intent intent = new Intent(getApplicationContext(), VoicCallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("channelId",channelId);
                    intent.putExtra("type","callee");
                    intent.putExtra("callerId",callerId);
                    getApplicationContext().startActivity(intent);

        }

        @Override
        //返回给被叫的回调：接受呼叫邀请成功。
        public void onRemoteInvitationAccepted(RemoteInvitation remoteInvitation) {
            System.out.println("被叫：接受呼叫邀请成功");
        }

        @Override
        //返回给被叫的回调：拒绝呼叫邀请成功。
        public void onRemoteInvitationRefused(RemoteInvitation remoteInvitation) {
            System.out.println("被叫：被叫拒绝了通话邀请");
        }

        @Override
        //返回给被叫的回调：主叫已取消呼叫邀请。
        public void onRemoteInvitationCanceled(RemoteInvitation remoteInvitation) {
            System.out.println("被叫：主叫已取消呼叫邀请");
            Intent intent=new Intent("callService");
            intent.putExtra("msg","local_refuse");
            sendBroadcast(intent);
        }

        @Override
        //返回给被叫的回调：来自主叫的邀请过程失败。
        public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int i) {
            System.out.println("被叫：来自主叫的邀请过程失败");
        }
    };
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("service:我被初始化了");
        initRTMCallManager();
        initReceiver();
    }
    private void initReceiver(){
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("service");
        serviceBroadCast=new ServiceBroadCast();
        registerReceiver(serviceBroadCast,intentFilter);
    }

    private void initRTMCallManager(){
        rtmCallManager= ProApplication.getRtmClient().getRtmCallManager();
        rtmCallManager.setEventListener(rtmCallEventListener);

    }

    private void inviteCall(String calleeId,String channel){
        localInvitation=rtmCallManager.createLocalInvitation(calleeId);
        localInvitation.setChannelId(channel);
        localInvitation.setContent(ProApplication.getUsername());
        rtmCallManager.sendLocalInvitation(localInvitation, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("发送邀请成功");
                System.out.println("发送邀请成功");
            }
            @Override
            public void onFailure(ErrorInfo errorInfo) {
                showToast("发送邀请失败");
                System.out.println("发送邀请失败");
            }
        });
    }
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceBroadCast);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private class ServiceBroadCast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("calleeId")!=null){
                name=intent.getStringExtra("calleeId");
                System.out.println("ServiceBroadCast-calleeId--------:"+name);
            }
            
            if(intent.getStringExtra("msg")!=null){
                msg=intent.getStringExtra("msg");
                System.out.println("ServiceBroadCast-msg--------:"+msg);
            }
            if(intent.getStringExtra("channelId")!=null){
                channelId=intent.getStringExtra("channelId");
            }
            if(!"".equals(name)&&"invite_call".equals(msg)){
                System.out.println("ServiceBroadCast-name-----"+name);
                inviteCall(name,ProApplication.getUsername());
            }
            if(!"".equals(msg)&&"refuse_call".equals(msg)){
            			
                    rtmCallManager.refuseRemoteInvitation(myRemoteInvitation, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("拒绝邀请成功");
                            myRemoteInvitation=null;
                        }

                        @Override
                        public void onFailure(ErrorInfo errorInfo) {
                            System.out.println("拒绝邀请失败");
                            myRemoteInvitation=null;
                        }
                    });
            }else if(!"".equals(msg)&&"accept_call".equals(msg)){
                    rtmCallManager.acceptRemoteInvitation(myRemoteInvitation, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("接收邀请成功");
                        }
                        @Override
                        public void onFailure(ErrorInfo errorInfo) {
                            System.out.println("接收邀请成功");
                        }
                    });
            }else if(!"".equals(msg)&&"local_refuse".equals(msg)){
                rtmCallManager.cancelLocalInvitation(localInvitation, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {

                    }
                });
            }

        }
    }
}
