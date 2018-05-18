package btcore.co.kr.h_fit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import btcore.co.kr.h_fit.bus.BusEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceProvider;
import btcore.co.kr.h_fit.bus.BusProvider;
import btcore.co.kr.h_fit.sevice.BluetoothLeService;
import btcore.co.kr.h_fit.util.DataUtils;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by leehaneul on 2018-01-25.
 */

public class CallReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();
    protected Context mSavedContext;
    private CallStartEndDetector mCallStartEndDetector = null;
    private DataUtils dataUtils;
    SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            pref = context.getSharedPreferences("pref", MODE_PRIVATE);

            dataUtils = new DataUtils();
            mSavedContext = context;
            if (mCallStartEndDetector == null) {
                mCallStartEndDetector = new CallStartEndDetector();
            }

            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                mCallStartEndDetector.setOutgoingNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
            } else {
                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                telephony.listen(mCallStartEndDetector, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    class CallStartEndDetector extends PhoneStateListener {
        int mLastState = TelephonyManager.CALL_STATE_IDLE;
        boolean mIsIncoming;
        String mSavedNumber;

        public CallStartEndDetector() {
        }

        public void setOutgoingNumber(String number) {
            mSavedNumber = number;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);


            if (mLastState == state) {
                return;
            }

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    mIsIncoming = true;
                    mSavedNumber = incomingNumber;
                    onIncomingCallStarted(incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (mLastState != TelephonyManager.CALL_STATE_RINGING) {
                        mIsIncoming = false;
                        onOutgoingCallStarted(incomingNumber);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (mLastState == TelephonyManager.CALL_STATE_RINGING) {
                        onMissedCall(mSavedNumber);
                    } else if (mIsIncoming) {
                        onIncomingCallEnded(mSavedNumber);
                    } else {
                        onOutgoingCallEnded(mSavedNumber);
                    }
                    break;
                default:
                    break;
            }
            mLastState = state;


        }
    }

    public void sendCommand(String data) {
        try {
            if (pref.getString("call", "").equals("call on")) {
                BusPhoneToDeviceProvider.getInstance().post(new BusPhoneToDeviceEvent(data));
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }


    private void onIncomingCallStarted(String number) {

        try {
            String numberOrName = getDisplayName(mSavedContext, number);
            String data = String.valueOf(dataUtils.callGetByte(numberOrName));
            if (pref.getString("Current", "").equals("on")) {
                sendCommand(data);
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());

        }


    }

    private void onOutgoingCallStarted(String number) {
        String numberOrName = getDisplayName(mSavedContext, number);
        String data = String.valueOf(dataUtils.endCallGetByte(numberOrName));
        if (pref.getString("Current", "").equals("on")) {
            sendCommand(data);
        }
    }

    private void onIncomingCallEnded(String number) {
        try {
            String numberOrName = getDisplayName(mSavedContext, number);
            String data = String.valueOf(dataUtils.endCallGetByte(numberOrName));
            if (pref.getString("Current", "").equals("on")) {
                sendCommand(data);
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void onOutgoingCallEnded(String number) {

    }

    private void onMissedCall(String number) {
        int missedCount = 0;

        String[] projection = {CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE};
        String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1";

        if (ActivityCompat.checkSelfPermission(mSavedContext, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor c = mSavedContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, where, null,
                null);
        c.moveToFirst();
        missedCount = (c.getCount() + 1) & 0xFF;

        String data = String.valueOf(dataUtils.missCallGetByte(missedCount));
        if (pref.getString("Current", "").equals("on")) {
            sendCommand(data);
        }
    }

    private String getDisplayName(Context context, String number) {
        String displayName = number;
        try {

            if (context == null) {
                return displayName;
            }
            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            if (contactUri == null) {
                return displayName;
            }
            String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                displayName = cursor.getString(nameIdx);
                cursor.close();
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return displayName;
    }

}
