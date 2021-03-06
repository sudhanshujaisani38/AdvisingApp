package com.sudhanshujaisani.advisingapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final int RESPONDED_ADVICE=2;
    AdviceService.AdviceBinder adviceBinder;
    AdviceService adviceService;
    BroadcastReceiver broadcastReceiver;
    Messenger messenger;
    TextView textView;
    ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger=new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            messenger=null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
         textView=findViewById(R.id.textView);
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
                Message message=Message.obtain(null,AdviceService.GIVE_ADVICE);
                message.replyTo=new Messenger(new ResponseHandler());
                try{
                    messenger.send(message);
                }
                catch (RemoteException e){
                    Toast.makeText(adviceService, "error", Toast.LENGTH_SHORT).show();
                }

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
    class  ResponseHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int respCode=msg.what;
            if(respCode==RESPONDED_ADVICE){
                String advice=msg.getData().getString("advice");
                textView.setText(advice);
            }

        }
    }
}
