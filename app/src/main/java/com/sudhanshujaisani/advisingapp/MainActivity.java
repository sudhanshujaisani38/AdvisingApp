package com.sudhanshujaisani.advisingapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    AdviceService.AdviceBinder adviceBinder;
    AdviceService adviceService;
    BroadcastReceiver broadcastReceiver;
    ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            adviceBinder=(AdviceService.AdviceBinder)service;
            adviceService=adviceBinder.getAdviceService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView=findViewById(R.id.textView);
        Button button=findViewById(R.id.button);



        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait while we fetch your advice..");
        progressDialog.setMessage("Loading....");

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("ShowDialog");
        intentFilter.addAction("DismissDialog");
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("ShowDialog"))
                    progressDialog.show();
                else if(intent.getAction().equals("DismissDialog"))
                    progressDialog.dismiss();
            }
        };
        registerReceiver(broadcastReceiver,intentFilter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String advice="No Advice";
                advice=adviceService.getAdvice();
                textView.setText(advice);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=new Intent(MainActivity.this,AdviceService.class);
        bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
    }
}
