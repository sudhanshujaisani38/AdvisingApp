package com.sudhanshujaisani.advisingapp;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class AdviceService extends Service {
    AdviceBinder adviceBinder=new AdviceBinder();
    static Boolean signal=false;
    String advice="No advice";
    public AdviceService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return adviceBinder;
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

    @Override
    public void onCreate() {
        super.onCreate();



    }

}
