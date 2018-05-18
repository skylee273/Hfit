package btcore.co.kr.h_fit.view.device;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import btcore.co.kr.h_fit.bus.BusPhoneToDeviceEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceProvider;
import btcore.co.kr.h_fit.util.DataUtils;
import btcore.co.kr.h_fit.view.main.MainActivity;
import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.sevice.BluetoothLeService;

import static btcore.co.kr.h_fit.sevice.BluetoothLeService.mStates;


/**
 * Created by leehaneul on 2018-01-26.
 */

public class DeviceSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    public BluetoothLeService mService = null;

    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;

    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mDevice = null;

    private int mState = UART_PROFILE_DISCONNECTED;
    private Timer HfitTask;
    private TimerTask HfitTimer;
    private ProgressDialog ConnectionLoading;
    private boolean HfitTimeFlag = false;


    private TextView mTextScan, mTextDevice;
    private Button mButtonScan;
    DataUtils dataUtils;
    private SharedPreferences pref = null;
    public SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        initView();
        AutoScan();
    }

    private void AutoScan() {
        HfitTask = new Timer();
        HfitTimer = new TimerTask() {
            @Override
            public void run() {
                try {
                    String address = pref.getString("deviceAddress", "");
                    String mCurrentState = pref.getString("Current", "");
                    if (address.length() > 0 && mCurrentState.equals("off")) {
                        service_init();
                        mService.connect(address);
                    } else {
                        if (HfitTimeFlag == false) {
                            String TimeVal = String.valueOf(dataUtils.getTimeInfo(dataUtils.getDate(), dataUtils.getWeek()));
                            sendCommand(TimeVal);
                            HfitTimeFlag = true;
                        }
                        if (getHour() == 11 && getMin() == 59 && getSec() == 59) {
                            String temp = String.valueOf(dataUtils.getTimeInfo(dataUtils.getDate(), dataUtils.getWeek()));
                            sendCommand(temp);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        };
        HfitTask.schedule(HfitTimer, 0, 5000);
    }

    private void initView() {

        dataUtils = new DataUtils();
        Toolbar toolbar = findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.icon_menu_back);
        actionBar.setTitle("기기설정");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mTextDevice = findViewById(R.id.text_device);
        mTextScan = findViewById(R.id.text_scan);
        mTextScan.setOnClickListener(this);
        mButtonScan = findViewById(R.id.btn_rescan);
        mButtonScan.setOnClickListener(this);

        BusPhoneToDeviceProvider.getInstance().register(this);


        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        service_init();

        if (mStates) {
            mTextDevice.setText("현재 디바이스가 연결되어 있습니다.");
        } else {
            mTextDevice.setText("현재 연결된 디바이스가 없습니다.");
        }

    }

    public void service_init() {
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        Snackbar.make(getWindow().getDecorView().getRootView(), "연결 되었습니다.", Snackbar.LENGTH_LONG).show();
                        mTextDevice.setText("기기 연결 성공");
                        mTextScan.setText("기기 종료");

                        if (HfitTask == null) {
                            AutoScan();
                        }
                        if (ConnectionLoading != null) {
                            ConnectionLoading.dismiss();
                        }
                        mState = UART_PROFILE_CONNECTED;

                    }
                });
            }

            //*********************//
            if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        HfitTimeFlag = false;
                        mTextDevice.setText("기기 연결 실패");
                        mTextScan.setText("기기 찾기");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }


            //*********************//
            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {

                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            Log.d(TAG, "ACTION_DATA_AVAILABLE : " + currentDateTimeString);

                        } catch (Exception e) {
                            android.util.Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                mService.disconnect();
            }
        }
    };


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((BluetoothLeService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    @Subscribe
    public void FinishLoad(BusPhoneToDeviceEvent mBusEvent) {
        Log.d(TAG, mBusEvent.getEventData());
        sendCommand(mBusEvent.getEventData());
    }

    public void sendCommand(String data) {
        mService.writeRXCharacteristic(data);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BusPhoneToDeviceProvider.getInstance().unregister(this);

        if (HfitTask != null) {
            HfitTask.cancel();

        }
        android.util.Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            android.util.Log.e(TAG, ignore.toString());
        }
        if (mService != null) {

            unbindService(mServiceConnection);
            mService.stopSelf();
            mService = null;
        }


    }


    @Override
    protected void onStop() {
        android.util.Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        android.util.Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        android.util.Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            android.util.Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ConnectionLoading = ProgressDialog.show(DeviceSettingActivity.this, "잠시 기다려주세요", "블루투스 연결중입니다.", true, false);
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    if (deviceAddress != null) {
                        editor.remove("deviceAddress");
                        editor.putString("deviceAddress", deviceAddress);
                        editor.commit();
                    }
                    try {
                        mService.connect(deviceAddress);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "해당기기가 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "블루투스를 활성화 했습니다. ", Toast.LENGTH_SHORT).show();

                } else {
                    android.util.Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                android.util.Log.e(TAG, "wrong request code");
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_scan:

                if (!mBtAdapter.isEnabled()) {
                    android.util.Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {


                    if (mStates) {
                        //Disconnect button pressed
                        if (HfitTask != null) {
                            HfitTask.cancel();
                        }
                        mService.disconnect();

                    } else {
                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        Intent newIntent = new Intent(DeviceSettingActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);

                    }
                }


                break;
            case R.id.btn_rescan:

                if (!mBtAdapter.isEnabled()) {
                    android.util.Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {

                    if (mStates) {
                        if (HfitTask != null) {
                            HfitTask.cancel();
                        }
                        mService.disconnect();

                    } else {
                        Intent newIntent = new Intent(DeviceSettingActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);

                    }
                }

                break;
        }
    }

    private int getHour() {
        Calendar cal = Calendar.getInstance();

        int hour = cal.get(cal.HOUR);
        return hour;
    }

    private int getMin() {
        Calendar cal = Calendar.getInstance();

        int min = cal.get(cal.MINUTE);
        return min;
    }

    private int getSec() {
        Calendar cal = Calendar.getInstance();

        int sec = cal.get(cal.SECOND);
        return sec;
    }

}
