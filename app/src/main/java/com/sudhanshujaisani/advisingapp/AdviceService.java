package com.sudhanshujaisani.advisingapp;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class AdviceService extends Service {
    public static final int GIVE_ADVICE=1;
    String advice="No advice";
    Messenger messenger=new Messenger(new AdviceHandler());
    public AdviceService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    class AdviceBinder extends Binder{
        AdviceService getAdviceService(){
            return AdviceService.this;
        };
    }
    String getAdvice(){
        Intent showDialog=new Intent("ShowDialog");
        sendBroadcast(showDialog);
        OkHttpClient okHttpClient=new OkHttpClient();
        String url="https://api.adviceslip.com/advice";
        Request request=new Request.Builder().url(url).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                Intent dismissDialog=new Intent("DismissDialog");
                sendBroadcast(dismissDialog);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    Intent dismissDialog=new Intent("DismissDialog");
                    sendBroadcast(dismissDialog);
                    String jsonResponse=response.body().string();
                    int indexOfStart=jsonResponse.indexOf("advice")+9;
                    int indexOfEnd=jsonResponse.indexOf("slip_id")-3;
                    advice=jsonResponse.substring(indexOfStart,indexOfEnd);
                }

            }
        });
        return advice;
    }

    class AdviceHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {

            int msgType=msg.what;
            if(msgType==GIVE_ADVICE)
            {
                try {
                    Message responseMsg = Message.obtain(null, MainActivity.RESPONDED_ADVICE);
                    Bundle bundle = new Bundle();
                    bundle.putString("advice", getAdvice());
                    responseMsg.setData(bundle);
                    msg.replyTo.send(responseMsg);
                }
                catch (RemoteException e){
                    Log.d("Sudhanshu",e.getMessage());
                }
            }
            else
            {
                super.handleMessage(msg);
            }
        }
    }

}
