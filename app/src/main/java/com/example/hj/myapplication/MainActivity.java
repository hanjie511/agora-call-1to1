package com.example.hj.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String username="";
    Button call_to_zhangsan_btn;
    Button call_to_lisi_btn;
    Button login_out_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }
    private void initData(){
        username=ProApplication.getUsername();
    }
    private void initView(){
        call_to_zhangsan_btn=findViewById(R.id.call_to_zhangsan_btn);
        call_to_lisi_btn=findViewById(R.id.call_to_lisi_btn);
        login_out_btn=findViewById(R.id.login_out_btn);
        if("zhangsan".equals(ProApplication.getUsername())){
            call_to_zhangsan_btn.setVisibility(View.GONE);
        }else if("lisi".equals(ProApplication.getUsername())){
            call_to_lisi_btn.setVisibility(View.GONE);
        }
    }
    public void  call_to_zhangsan(View view){
        Intent intent=new Intent(MainActivity.this,VoicCallActivity.class);
        intent.putExtra("name", "zhangsan");
        intent.putExtra("type","caller");
        Intent intent1=new Intent("service");
        intent1.putExtra("calleeId","zhangsan");
        intent1.putExtra("msg","invite_call");
        intent1.putExtra("calleeName", "zhangsan");
        sendBroadcast(intent1);
        startActivity(intent);
    }
    public void  call_to_lisi(View view){
        Intent intent=new Intent(MainActivity.this,VoicCallActivity.class);
        intent.putExtra("name", "lisi");
        intent.putExtra("type","caller");
        Intent intent1=new Intent("service");
        intent1.putExtra("calleeId","lisi");
        intent1.putExtra("msg","invite_call");
        intent1.putExtra("calleeName", "lisi");
        sendBroadcast(intent1);
        startActivity(intent);
    }
    public void  login_out(View view){
        ProApplication.getRtmClient().logout(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"退出成功",Toast.LENGTH_SHORT).show();
                        ProApplication.flag=true;
                        finish();
                    }
                });

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"退出失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }
}
