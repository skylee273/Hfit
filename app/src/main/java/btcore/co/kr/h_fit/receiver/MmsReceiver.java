package btcore.co.kr.h_fit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import btcore.co.kr.h_fit.bus.BusPhoneToDeviceEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceProvider;
import btcore.co.kr.h_fit.sevice.BluetoothLeService;
import btcore.co.kr.h_fit.util.DataUtils;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by leehaneul on 2018-02-06.
 */

public class MmsReceiver extends BroadcastReceiver {
    private final String TAG = getClass().getSimpleName();
    DataUtils dataUtils;
    protected Context mSavedContext;

    private static final String ACTION_MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";

    SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSavedContext = context;
        pref = context.getSharedPreferences("pref", MODE_PRIVATE);

        dataUtils = new DataUtils();

        String action = intent.getAction();
        String type = intent.getType();


        if (action.equals(ACTION_MMS_RECEIVED) && type.equals(MMS_DATA_TYPE)) {
            try {
                List<TextMessage> messages = getMessagesFrom(context, intent);
                for (TextMessage message : messages) {
                    receiveMessage(message);
                    break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while extracting messages from intent.", e);
            }
        }


    }


    private void receiveMessage(TextMessage message) {

        Uri mms_content = Uri.parse("content://mms/inbox");
        Cursor c = mSavedContext.getContentResolver().query(mms_content, null, "read = 0", null, null);
        c.moveToFirst();
        String countStr = String.format("%03d", c.getCount());

        String numberOrName = getDisplayName(mSavedContext, message.from);
        String data = message.body;
        String mmsData = String.valueOf(dataUtils.smsGet(c.getCount(), numberOrName, data));
        if(pref.getString("Current", "").equals("on")) {
            sendCommand(mmsData);
        }
    }

    public void sendCommand(String data) {
        if(pref.getString("message","").equals("message on")){
            BusPhoneToDeviceProvider.getInstance().post(new BusPhoneToDeviceEvent(data));
        }
    }

    private String getDisplayName(Context context, String number) {
        String displayName = number;
        if (context == null) {
            return displayName;
        }
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        if (contactUri == null) {
            return displayName;
        }
        try {
            String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                displayName = cursor.getString(nameIdx);
                cursor.close(); // Release resources
            }
        } catch (Exception e) {
        }
        return displayName;
    }

    public static List<TextMessage> getMessagesFrom(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        List<TextMessage> messages = new ArrayList<TextMessage>();

        if (bundle == null || !bundle.containsKey("data")) {
            return messages;
        }

        byte[] data = bundle.getByteArray("data");
        if (data == null || data.length == 0) {
            return messages;
        }

        try {
            String buffer = new String(bundle.getByteArray("data"));

            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                }
                Cursor cur = context.getContentResolver().query(Uri.parse("content://mms/inbox"), null, "m_type in (132,128)", null, "date DESC");
                if (cur == null) {
                    continue;
                }
                try {
                    if (cur.getCount() == 0) {
                        continue;
                    }
                    cur.moveToFirst();
                    int cnt = 0;

                    do {
                        int id = cur.getInt(cur.getColumnIndex("_id"));
                        String mid = cur.getString(cur.getColumnIndex("m_id"));

                        if (!buffer.contains(mid)) {
                            continue;
                        }
                        String subj = cur.getString(cur.getColumnIndex("sub"));
                        String body = "";
                        String from = getMmsAddr(context, id);
                        long date = Long.parseLong(cur.getString(cur.getColumnIndex("date")));

                        Cursor cprt = context.getContentResolver().query(Uri.parse("content://mms/part"), null, "mid = " + id, null, null);
                        try {
                            if (cprt.moveToFirst()) {
                                do {
                                    String pid = cprt.getString(cprt.getColumnIndex("_id"));
                                    String type = cprt.getString(cprt.getColumnIndex("ct"));
                                    if ("text/plain".equals(type)) {
                                        String dat = cprt.getString(cprt.getColumnIndex("_data"));
                                        if (dat != null) {
                                            body += getMmsText(context, pid);
                                        } else {
                                            body += cprt.getString(cprt.getColumnIndex("text"));
                                        }
                                    } else if ("image/jpeg".equals(type) || "image/bmp".equals(type) || "image/gif".equals(type) || "image/jpg".equals(type) || "image/png".equals(type)) {
                                        body += "\n[image]\n";
                                    }
                                } while (cprt.moveToNext());
                            }
                        } finally {
                            if (cprt != null) {
                                cprt.close();
                            }
                        }

                        messages.add(new TextMessage(from, date, body.length() != 0 ? "\n" + body : ""));
                        return messages;
                    } while (cur.moveToNext() && ++cnt < 10);
                } finally {
                    cur.close();
                }
            }
        } catch (Exception ex) {
            return messages;
        }

        return messages;
    }

    private static String getMmsText(Context context, String id) {
        InputStream is = null;
        StringBuilder sb = new StringBuilder();

        try {
            is = context.getContentResolver().openInputStream(Uri.parse("content://mms/part/" + id));
            if (is == null) {
                return sb.toString();
            }

            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String temp = reader.readLine();
            while (temp != null) {
                sb.append(temp);
                temp = reader.readLine();
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return sb.toString();
    }

    private static String getMmsAddr(Context context, int id) {
        String address = "";
        String val;
        Cursor cur = context.getContentResolver().query(Uri.parse("content://mms/" + id + "/addr"), new String[]{"address"}, "type=137 AND msg_id=" + id, null, null);

        if (cur == null) {
            return address;
        }

        try {
            if (cur.moveToFirst()) {
                do {
                    val = cur.getString(cur.getColumnIndex("address"));
                    if (val != null) {
                        address = val;
                        break;
                    }
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
        }
        return address;
    }

    private static class TextMessage {
        double date;
        String from, body;

        public TextMessage(String from, double date, String body) {
            this.from = from;
            this.date = date;
            this.body = body;
        }

        public void append(String body) {
            this.body += body;
        }

    }


}
