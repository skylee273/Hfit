package btcore.co.kr.h_fit.sevice;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import btcore.co.kr.h_fit.bus.BusEvent;
import btcore.co.kr.h_fit.bus.BusProvider;
import btcore.co.kr.h_fit.database.ContactDBCHeart;
import btcore.co.kr.h_fit.database.ContactDBKcal;
import btcore.co.kr.h_fit.database.ContactDBStep;
import btcore.co.kr.h_fit.database.DB_HeartRate;
import btcore.co.kr.h_fit.database.DB_Cal;
import btcore.co.kr.h_fit.database.DB_Step;
import btcore.co.kr.h_fit.util.ParserUtils;


/**
 * Created by leehaneul on 2018-01-19.
 */

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mbluetoothManager;
    private BluetoothAdapter mbluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private String Data[];
    private String oYear, oMonth, oDate;
    private Long DataLength;
    private int DataType;
    private int oStep, oCal, oHr;
    private static int mStepData = 0;
    private static int mCalData = 0;
    public static boolean mStates = false;
    private boolean oldFlag = false;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.nordicsemi.nrfUART.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static SharedPreferences.Editor editor;
    public static SharedPreferences pref = null;

    /**
     * DATA BASE AREA
     */

    public DB_HeartRate heartDbHelper = null;
    public DB_Step stepDBHelper = null;
    public DB_Cal CalDBHelper = null;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            pref = getSharedPreferences("pref", MODE_PRIVATE);
            editor = pref.edit();

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                mStates = true;
                editor.remove("Current");
                editor.putString("Current", "on");
                editor.commit();
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mStates = false;
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                editor.remove("Current");
                editor.putString("Current", "off");
                editor.commit();
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }


        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final String data = ParserUtils.parse(characteristic);

            final BluetoothGattDescriptor cccd = characteristic.getDescriptor(CCCD);
            final boolean notifications = cccd == null || cccd.getValue() == null || cccd.getValue().length != 2 || cccd.getValue()[0] == 0x01;
            Log.d(TAG,data);
            if (notifications) {
                HfitData(data);
                onCharacteristicNotified(gatt, characteristic);
            }


            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

        }

        protected void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

        }

    };


    private int getOldWeek(String date) {
        Calendar c = Calendar.getInstance();
        String weekdate[] = date.split("-");
        int year = Integer.parseInt(weekdate[0],16);
        int mMonth = Integer.parseInt(weekdate[1],16);
        int mDate = Integer.parseInt(weekdate[2],16);
        Log.d(TAG, String.valueOf(year) +  String.valueOf(mMonth) + String.valueOf(mDate));
        c.set(year, mMonth - 1, mDate);
        String week = String.valueOf(c.get(Calendar.WEEK_OF_MONTH));

        return Integer.parseInt(week);
    }

    private boolean checkDevice(String data) {
        String _data[] = data.split("-");
        boolean flag = false;
        if (_data[0].equals("FE") && _data[1].equals("DC")) {
            flag = true;
        }
        if (_data[2].equals("0F")) {
            flag = false;
        }

        return flag;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        } else {
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        editor.remove("Current");
        editor.remove("deviceAddress");
        editor.remove("heart");
        editor.remove("step");
        editor.remove("kcal");
        editor.remove("battery");
        editor.putString("Current", "off");
        editor.commit();
        close();
        return super.onUnbind(intent);
    }


    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mbluetoothManager == null) {
            mbluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mbluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mbluetoothAdapter = mbluetoothManager.getAdapter();
        if (mbluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mbluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                Log.d("connect", "STATE_CONNECTING");
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mbluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;

        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mbluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        // mBluetoothGatt.close();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mbluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
    }

    public void enableTXNotification() {
        if (mBluetoothGatt == null) {
            showMessage("mBluetoothGatt null" + mBluetoothGatt);
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            showMessage("enable TXNotification - Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("enable RXNotification - Tx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        } else {
            this.readCharacteristic(TxChar);
        }

        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
        mBluetoothGatt.readCharacteristic(TxChar);
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void writeRXCharacteristic(String value) {
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);

        showMessage("mBluetoothGatt null" + mBluetoothGatt);
        if (RxService == null) {
            showMessage("write RX Characteristic - Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

        Log.d(TAG, "write TXchar - status=" + status);
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


    private void init_heartTables() {

        heartDbHelper = new DB_HeartRate(this);
    }

    private void init_stepTables() {

        stepDBHelper = new DB_Step(this);
    }

    private void init_kcalTables() {

        CalDBHelper = new DB_Cal(this);
    }

    private void save_kcalValue(String date, int week, int hour, int kcal) {
        SQLiteDatabase db = CalDBHelper.getWritableDatabase();

        String sqlInsert = ContactDBKcal.SQL_INSERT +
                " (" +
                "'" + date + "', " +
                Integer.toString(week) + ", " +
                Integer.toString(hour) + ", " +
                Integer.toString(kcal) +
                ")";

        db.execSQL(sqlInsert);
    }

    private void save_stepValue(String date, int week, int hour, int step) {
        SQLiteDatabase db = stepDBHelper.getWritableDatabase();

        String sqlInsert = ContactDBStep.SQL_INSERT +
                " (" +
                "'" + date + "', " +
                Integer.toString(week) + ", " +
                Integer.toString(hour) + ", " +
                Integer.toString(step) +
                ")";

        db.execSQL(sqlInsert);
    }

    private void save_heartValue(String date, int week, int hour, int bpm) {
        Log.d(TAG, date + week + hour + bpm);
        SQLiteDatabase db = heartDbHelper.getWritableDatabase();

        String sqlInsert = ContactDBCHeart.SQL_INSERT +
                " (" +
                "'" + date + "', " +
                Integer.toString(week) + ", " +
                Integer.toString(hour) + ", " +
                Integer.toString(bpm) +
                ")";

        db.execSQL(sqlInsert);
    }


    private void setData(String bleData) {
        this.Data = bleData.split("-");
        this.DataLength = Long.parseLong(Data[2], 16);
        this.DataType = Integer.parseInt(Data[3]);
    }


    private String getDate() {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Date currentTime = new Date();
        String mTime = mSimpleDateFormat.format(currentTime);

        return mTime;
    }

    private int setType() {
        return DataType;
    }

    private int getHfitData() {
        int startIndex = 4;
        int endIndex = 0;
        String temp = null;

        for (int i = 0; i < DataLength + 2; i++) {
            if (Data[i].equals("AB") && Data[i + 1].equals("CD")) {
                endIndex = i;
            }
        }
        temp = Data[startIndex];

        for (int i = startIndex + 1; i < endIndex; i++) {
            temp += Data[i];
        }
        int intValue = Integer.parseInt(temp, 16);


        return intValue;
    }

    private void setStrHfitData() {
        int startIndex = 4;
        String DataConverter = null;

        oYear = Data[startIndex];
        oYear += Data[startIndex + 1];
        oMonth = Data[startIndex + 2];
        oDate = Data[startIndex + 3];
        DataConverter = Data[startIndex + 4];
        DataConverter += Data[startIndex + 5];
        oStep = Integer.parseInt(DataConverter, 16);
        DataConverter = Data[startIndex + 6];
        DataConverter += Data[startIndex + 7];
        oCal = Integer.parseInt(DataConverter, 16);
        DataConverter = Data[startIndex + 8];
        DataConverter += Data[startIndex + 9];
        oHr = Integer.parseInt(DataConverter, 16);
        Log.d(TAG, String.valueOf(oStep)  +  String.valueOf(oCal) + String.valueOf(oHr));

    }

    private int getWeek() {
        Calendar cal = Calendar.getInstance();

        int week = cal.get(cal.DAY_OF_WEEK);

        return week;
    }

    private int getHour() {

        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH", Locale.KOREA);
        Date currentTime = new Date();
        String mHour = mSimpleDateFormat.format(currentTime);

        return Integer.parseInt(mHour);
    }

    private void HfitData(String data){
        if (checkDevice(data)) {
            setData(data);
            oldFlag = true;
            switch (setType()) {
                case 1:
                    init_heartTables();
                    save_heartValue(getDate(), getWeek(), getHour(), getHfitData());
                    break;
                case 2:
                    init_stepTables();
                    if (mStepData == 0) {
                        mStepData = getHfitData();
                        if (mStepData != 0) {
                            save_stepValue(getDate(), getWeek(), getHour(), getHfitData());
                        }
                    } else if (mStepData < getHfitData()) {
                        save_stepValue(getDate(), getWeek(), getHour(), getHfitData());
                        mStepData = getHfitData();
                    }
                    break;
                case 4:
                    init_kcalTables();
                    if (mCalData == 0) {
                        mCalData = getHfitData();
                        if (mCalData != 0){
                            save_kcalValue(getDate(), getWeek(), getHour(), getHfitData());
                        }
                    } else if (mCalData < getHfitData()) {
                        save_kcalValue(getDate(), getWeek(), getHour(), getHfitData());
                        mCalData = getHfitData();
                    }
                    break;
                case 6:
                    try {
                        oldFlag = false;
                        init_heartTables();
                        init_stepTables();
                        init_kcalTables();
                        setStrHfitData();
                        int StrToIntYear, StrToIntMonth, StrToData;
                        StrToIntYear = Integer.parseInt(oYear, 16);
                        StrToIntMonth = Integer.parseInt(oMonth, 16);
                        StrToData = Integer.parseInt(oDate, 16);

                        String temp = String.format("%04d",StrToIntYear) + "-" +  String.format("%02d",StrToIntMonth) + "-" + String.format("%02d",StrToData);
                        save_heartValue(temp, getOldWeek(temp), 23, oHr);
                        save_stepValue(getDate(), getOldWeek(temp), 23, oStep);
                        save_kcalValue(getDate(), getOldWeek(temp), 23, oCal);
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
            }
            if(oldFlag){
                BusProvider.getInstance().post(new BusEvent(data));
            }
        }
    }


}
