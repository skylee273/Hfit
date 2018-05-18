package btcore.co.kr.h_fit;

        import android.annotation.TargetApi;
        import android.app.Notification;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Build;
        import android.os.Bundle;
        import android.service.notification.NotificationListenerService;
        import android.service.notification.StatusBarNotification;
        import android.support.v4.content.LocalBroadcastManager;
        import android.util.Log;

        import java.text.SimpleDateFormat;
        import java.util.Locale;

/**
 * Created by leehaneul on 2018-01-23.
 */

public class NotificationService extends NotificationListenerService {

    private static final String TAG = NotificationService.class.getSimpleName();
    private String mTitle = null, body = null, mMissText = null, mTime;
    private String title, miss, content;
    private static boolean MissedMessageFlag = false;
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context =this;
        Log.d(TAG, "Notification Listener created!");
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        if (sbn == null) return;

        mTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(System.currentTimeMillis());

        Notification mNotification=sbn.getNotification();

        Bundle extras = mNotification.extras;


        String packName = sbn.getPackageName();


        if (packName.equalsIgnoreCase("com.kakao.talk")) {

            mMissText = extras.getString(Notification.EXTRA_SUB_TEXT);

            mTitle = extras.getString(Notification.EXTRA_TITLE);

            body = extras.getString(Notification.EXTRA_TEXT);

            if(mMissText != null){
                miss = mMissText;
            }
            if(mTitle != null){
                title = mTitle;
            }
            if(context != null){
                content = body;
            }
            if(miss == null && title != null && content != null && MissedMessageFlag == false ){
                sendZeroMessage();
            }
            if(miss != null && title != null && content != null ){
                sendMessage();
                MissedMessageFlag = true;
                miss = null;
                title = null;
                content = null;
            }

        }


    }
    private void sendZeroMessage(){
        Intent msgrcv = new Intent("LocalMsg");
        String data0 = "0";
        String data1 =title;
        String data2 =content;
        Log.i(TAG, "부재중 메시지 : "+ data0  + " 제목 : "+  data1 + " 본문 : "+ data2  + " 시간 : " + mTime);
        msgrcv.putExtra("kakaoInfo", data0 + "," +  data1 + "," + data2);
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

    private void sendMessage(){
        Intent msgrcv = new Intent("LocalMsg");
        String data0 = miss.replaceAll("[^0-9]", "");
        String data1 =title;
        String data2 =content;
        Log.i(TAG, "부재중 메시지 : "+ data0  + " 제목 : "+  data1 + " 본문 : "+ data2  + " 시간 : " + mTime);
        msgrcv.putExtra("kakaoInfo", data0 + "," +  data1 + "," + data2);
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        Log.d(TAG, "Notification Removed:\n");

    }
}