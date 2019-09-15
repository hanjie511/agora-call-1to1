package com.example.hj.myapplication;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import android.graphics.PorterDuff;

import com.example.hj.call.RtcTokenBuilderSample;

public class VoicCallActivity extends Activity {

	private static final String LOG_TAG = VoicCallActivity.class.getSimpleName();

	private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
	private TextView alert_text;//通话界面提示文字的textVIew
	private String type = "";//通话的角色类型
	private String callerId = "";//来电的userId
	private String name = "";//拨打用户的userId
	private String channelId = "";//主叫创建的channelId
	private String msg = "";//接收Service传过来的消息
	private LinearLayout is_accept_layout;//用户犹豫接听电话时，显示在底部的控件布局
	private LinearLayout accept_layout;//用户接听后，显示在底部的控件布局
	private BroadcastReceiver callBroadCast;//本activity的广播接收者
	private RtcEngine mRtcEngine; // 用于建立通信的RtcEngine实例
	private ImageView not_received_hangUp;//对方还未接听时，页面的挂断电话的按钮
	private ImageView received_hungUp;//对方接听后，页面显示的挂断电话的按钮
	private Button local_refuse_btn;//主叫取消呼叫后，被叫页面显示的返回按钮
	//RtcEngine实例的事件监听处理器
	private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

		@Override
		//当对方结束通话时，调用的方法
		public void onUserOffline(final int uid, final int reason) { // Tutorial Step 4
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onRemoteUserLeft(uid, reason);
				}
			});
		}

		@Override
		//当对方开始说话时，执行的方法
		public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onRemoteUserVoiceMuted(uid, muted);
				}
			});
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voic_call);
		receiveData();
		initView();
		initReceiver();
		if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
			initAgoraEngineAndJoinChannel();
		}
	}

	//接收从另一个页面传过来的数据
	private void receiveData() {
		Intent intent = getIntent();
		if (intent.getStringExtra("type") != null) {
			type = intent.getStringExtra("type");
		}
		if (intent.getStringExtra("callerId") != null) {
			callerId = intent.getStringExtra("callerId");
		}
		if (intent.getStringExtra("name") != null) {
			name = intent.getStringExtra("name");
		}
		if (intent.getStringExtra("channelId") != null) {
			channelId = intent.getStringExtra("channelId");
		}
	}

	//初始化本页面的广播接收器的方法
	private void initReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("callService");
		callBroadCast = new CallBroadCast();
		registerReceiver(callBroadCast, intentFilter);
	}

	//初始化控件的方法
	private void initView() {
		alert_text = (TextView) findViewById(R.id.alert_text);
		is_accept_layout = (LinearLayout) findViewById(R.id.is_accept_layout);
		accept_layout = (LinearLayout) findViewById(R.id.accept_layout);
		not_received_hangUp = (ImageView) findViewById(R.id.not_received_hangUp);
		received_hungUp = (ImageView) findViewById(R.id.received_hangUp);
		local_refuse_btn = (Button) findViewById(R.id.local_refuse_btn);
		if ("caller".equals(type)) {
			is_accept_layout.setVisibility(View.INVISIBLE);
			accept_layout.setVisibility(View.VISIBLE);
			alert_text.setText("正在呼叫" + name + "....");
		} else if ("callee".equals(type)) {
			is_accept_layout.setVisibility(View.VISIBLE);
			accept_layout.setVisibility(View.INVISIBLE);
			alert_text.setText("来自" + callerId + "的通话邀请");
		}

	}

	//初始化通话引擎并加入通话频道的方法
	private void initAgoraEngineAndJoinChannel() {
		if ("caller".equals(type)) {//呼叫者通话界面的逻辑，初始化引擎后，加入频道，等待对方加入
			initializeAgoraEngine();     // Tutorial Step 1
			joinChannel(ProApplication.getUsername());               // Tutorial Step 2
		} else if ("callee".equals(type)) {//被呼叫者通话界面的逻辑，初始化通话引擎，当点击确定接听按钮后，加入频道开始对话
			initializeAgoraEngine();     // Tutorial Step 1
		}
	}

	//显示文本提示框的方法
	public final void showLongToast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(callBroadCast);
		leaveChannel();
		RtcEngine.destroy();
		mRtcEngine = null;
	}

	public boolean checkSelfPermission(String permission, int requestCode) {
		Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
		if (ContextCompat.checkSelfPermission(this,
				permission)
				!= PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(this,
					new String[]{permission},
					requestCode);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String permissions[], @NonNull int[] grantResults) {
		Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

		switch (requestCode) {
			case PERMISSION_REQ_ID_RECORD_AUDIO: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initAgoraEngineAndJoinChannel();
				} else {
					showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
					finish();
				}
				break;
			}
		}
	}
	// Tutorial Step 7
	//当用户点击了取消采集本地音频的按钮执行的方法
	public void onLocalAudioMuteClicked(View view) {
		ImageView iv = (ImageView) view;
		if (iv.isSelected()) {
			iv.setSelected(false);
			iv.clearColorFilter();
		} else {
			iv.setSelected(true);
			iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
		}
		mRtcEngine.muteLocalAudioStream(iv.isSelected());//是否停止采集本地音频 的方法
	}

	// Tutorial Step 5
	//用户点击是否开启外放的按钮后执行的方法
	public void onSwitchSpeakerphoneClicked(View view) {
		ImageView iv = (ImageView) view;
		if (iv.isSelected()) {
			iv.setSelected(false);
			iv.clearColorFilter();
		} else {
			iv.setSelected(true);
			iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
		}

		mRtcEngine.setEnableSpeakerphone(view.isSelected());//是否关闭外放的方法
	}

	//拒绝接听按钮的方法
	public void refuse_phone(View v) {
		Intent intent = new Intent("service");
		intent.putExtra("msg", "refuse_call");
		sendBroadcast(intent);
		finish();
	}

	//主叫取消呼叫后，被叫页面显示的“返回按钮的执行方法”
	public void local_refuse(View v) {
		finish();
	}

	//接听按钮的方法
	public void accept_phone(View v) {
		showLongToast("received我被点击了");
		Intent intent = new Intent("service");
		intent.putExtra("msg", "accept_call");
		sendBroadcast(intent);
		joinChannel(channelId);
		accept_layout.setVisibility(View.VISIBLE);
		is_accept_layout.setVisibility(View.INVISIBLE);
		not_received_hangUp.setVisibility(View.GONE);
		received_hungUp.setVisibility(View.VISIBLE);
		alert_text.setText(getString(R.string.accept_alert_text));
	}

	//本地呼叫取消按钮的点击方法
	public void cancel_call(View v) {
		showLongToast("not_received我被点击了");
		Intent intent = new Intent("service");
		intent.putExtra("msg", "local_refuse");
		sendBroadcast(intent);
		finish();
	}

	// Tutorial Step 3
	//接听电话后，挂断电话按钮执行的方法
	public void onEncCallClicked(View view) {
		finish();
	}

	// Tutorial Step 1
	//初始化通话引擎的方法
	private void initializeAgoraEngine() {
		try {
			mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
			mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
		} catch (Exception e) {
			Log.e(LOG_TAG, Log.getStackTraceString(e));

			throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
		}
	}

	// Tutorial Step 2
	//加入频道进行通话的方法
	private void joinChannel(String channel) {
		String token = RtcTokenBuilderSample.buildTokenWithUserAccount(getString(R.string.agora_app_id), getString(R.string.agora_app_certificate), channel, ProApplication.getUsername());
		System.out.println("VoiceChatViewActivity-token:" + token);
		System.out.println("VoiceChatViewActivity-channel:" + channel);
//	        mRtcEngine.joinChannel(token, channel, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
		mRtcEngine.joinChannelWithUserAccount(token, channel, ProApplication.getUsername());
	}

	// Tutorial Step 3
	//用户离开频道要调用的方法
	private void leaveChannel() {
		mRtcEngine.leaveChannel();
	}

	// Tutorial Step 4
	//对方离开频道要调用的方法
	private void onRemoteUserLeft(int uid, int reason) {
		alert_text.setText(getString(R.string.finished_alert_text));
		accept_layout.setVisibility(View.GONE);
		local_refuse_btn.setVisibility(View.VISIBLE);
//	        showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
//	        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//	        tipMsg.setVisibility(View.VISIBLE);
	}

	// Tutorial Step 6
	//对方开始说法后，调用的方法
	private void onRemoteUserVoiceMuted(int uid, boolean muted) {
		alert_text.setText(getString(R.string.accept_alert_text));
		showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
	}

	//定义的该Activity的广播接收者
	private class CallBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("msg") != null) {
				msg = intent.getStringExtra("msg");
				System.out.println("CallBroadCast-msg:" + msg);
			}
			if ("refuse_call".equals(msg)) {
				alert_text.setText("对方已拒绝和你通话！！！");
				received_hungUp.setVisibility(View.GONE);
				not_received_hangUp.setVisibility(View.GONE);
				accept_layout.setVisibility(View.GONE);
				local_refuse_btn.setVisibility(View.VISIBLE);
			} else if ("accept_call".equals(msg)) {
				alert_text.setText(getString(R.string.accept_alert_text));
				not_received_hangUp.setVisibility(View.VISIBLE);
				received_hungUp.setVisibility(View.GONE);
			} else if ("local_refuse".equals(msg)) {
				alert_text.setText("对方已取消呼叫！！！");
				is_accept_layout.setVisibility(View.GONE);
				local_refuse_btn.setVisibility(View.VISIBLE);
			}
		}
	}
}