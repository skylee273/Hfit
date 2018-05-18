package btcore.co.kr.h_fit.sevice;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

import btcore.co.kr.h_fit.bus.BusPhoneToDeviceEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceProvider;

/**
 * Created by leehaneul on 2018-01-25.
 */

public class KakaoNotificationService extends NotificationListenerService {
    private final String TAG =  getClass().getSimpleName();
    private Context mContext;
    private String mTitle = null, context = null, mMissText = null, mTime;
    private String title, miss, content;
    private Bundle extras;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO : 서비스 시작시 코드 추가
        mContext =this;
        Log.d(TAG, "서비스 시작 !");
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification){

        Log.d(TAG,"카카오 리시버 등록 완료");
        if(statusBarNotification == null){
            return;
        }

        String Date =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(System.currentTimeMillis());
        Notification mNotification=statusBarNotification.getNotification();

        extras = mNotification.extras;
        String packName = statusBarNotification.getPackageName();


        if (packName.equalsIgnoreCase("com.kakao.talk")) {

            mMissText = extras.getString(Notification.EXTRA_SUB_TEXT);

            mTitle = extras.getString(Notification.EXTRA_TITLE);

            context = extras.getString(Notification.EXTRA_TEXT);

            mTime = Date;

            if(mMissText != null){
                miss = mMissText;
            }
            if(mTitle != null){
                title = mTitle;
            }
            if(context != null){
                content = context;
            }
            if(miss != null && title != null && content != null ){
                sendMessage();
            }
        }

    }

    private void sendMessage(){
        Intent msgrcv = new Intent("LocalMsg");
        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        String data0 = miss.replaceAll("[^0-9]", "");
        String data1 =title.replaceAll(match, "");
        String data2 =content.replaceAll(match, "");
        msgrcv.putExtra("kakaoInfo", data0 + "," +  data1 + "," + data2);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(msgrcv);
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        Log.d(TAG, "Notification Removed:\n");

    }



}


