package btcore.co.kr.h_fit.view.setting;

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
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import btcore.co.kr.h_fit.bus.BusPhoneToDeviceEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceProvider;
import btcore.co.kr.h_fit.sevice.BluetoothLeService;
import btcore.co.kr.h_fit.util.DataUtils;
import btcore.co.kr.h_fit.view.dfu.DfuActivity;
import btcore.co.kr.h_fit.view.main.MainActivity;
import btcore.co.kr.h_fit.R;


public class SettingActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private final String TAG = getClass().getSimpleName();
    private Switch mSwitchAlarm, mSwitchCall, mSwitchMessage, mSwitchKakao;
    private Button mButtonDownload;
    private TextView mTextFirmware;
    public static SharedPreferences.Editor editor;
    public static SharedPreferences pref = null;
    public BluetoothLeService mService = null;


    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;

    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mDevice = null;

    private int mState = UART_PROFILE_DISCONNECTED;
    private Timer task = new Timer();
    DataUtils dataUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        initView();
        AutoScan();
    }


    private void AutoScan() {
        TimerTask mnTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    String address = pref.getString("deviceAddress", "");
                    String mCurrentState = pref.getString("Current", "");
                    Log.d(TAG, "기기 주소  : " + address + "현재 상태 : " + mCurrentState);
                    if (address.length() > 0 && mCurrentState.equals("off")) {
                        service_init();
                        mService.connect(address);
                    } else {

                    }
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        };
        task.schedule(mnTask, 0, 5000);
    }


    private void initView() {


        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.icon_menu_back);
        actionBar.setTitle("설정");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSwitchAlarm = findViewById(R.id.switch_alarm);
        mSwitchCall = findViewById(R.id.switch_call);
        mSwitchMessage = findViewById(R.id.switch_message);
        mSwitchKakao = findViewById(R.id.switch_kakao);


        mSwitchAlarm.setOnCheckedChangeListener(this);
        mSwitchCall.setOnCheckedChangeListener(this);
        mSwitchMessage.setOnCheckedChangeListener(this);
        mSwitchKakao.setOnCheckedChangeListener(this);

        mButtonDownload = findViewById(R.id.btn_download);
        mButtonDownload.setOnClickListener(this);

        mTextFirmware = findViewById(R.id.text_frimware);
        mTextFirmware.setOnClickListener(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (pref.getString("alarm", "").equals("alarm on")) {
                        mSwitchAlarm.setChecked(true);
                    }
                    if (pref.getString("call", "").equals("call on")) {
                        mSwitchCall.setChecked(true);
                    }
                    if (pref.getString("message", "").equals("message on")) {
                        mSwitchMessage.setChecked(true);
                    }
                    if (pref.getString("kakao", "").equals("kakao on")) {
                        mSwitchKakao.setChecked(true);
                    } else {
                        Log.d(TAG, "No SharedPreference Data");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        BusPhoneToDeviceProvider.getInstance().register(this);


        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        service_init();
    }

    public void sendCommand(String data) {
        mService.writeRXCharacteristic(data);
    }


    @Subscribe
    public void FinishLoad(BusPhoneToDeviceEvent mBusEvent) {
        Log.d(TAG, mBusEvent.getEventData());
        sendCommand(mBusEvent.getEventData());
    }

    public void service_init() {
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        android.util.Log.d(TAG, "UART_CONNECT_MSG");
                        mState = UART_PROFILE_CONNECTED;

                    }
                });
            }

            //*********************//
            if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        android.util.Log.d(TAG, "UART_DISCONNECT_MSG");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }


            //*********************//
            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {

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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {

            dataUtils = new DataUtils();
            String kakaoMessage[] = null;
            String Kakao = intent.getStringExtra("kakaoInfo");
            if (Kakao != null) {
                kakaoMessage = Kakao.split(",");
            }
            if (pref.getString("kakao", "").equals("kakao on") && pref.getString("Current", "").equals("on") && kakaoMessage != null) {
                try {
                    if (pref.getString("Current", "").equals("on")) {
                        String value = dataUtils.kakaoGet(Integer.parseInt(kakaoMessage[0]), kakaoMessage[1], kakaoMessage[2]);
                        sendCommand(value);
                    } else {
                        Log.d(TAG, kakaoMessage[0].toString() + kakaoMessage[1].toString());
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, e.toString());
                }

            }

        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();


        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);

        BusPhoneToDeviceProvider.getInstance().unregister(this);

        if (task != null) {
            task.cancel();
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("LocalMsg"));
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
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    //((TextView) findViewById(R.id.tag_name)).setText(mDevice.getName() );
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
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
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
            case R.id.btn_download:
                if (task != null) {
                    task.cancel();
                }
                if (mService != null) {
                    mService.disconnect();
                }
                Intent intent1 = new Intent(getApplicationContext(), DfuActivity.class);
                startActivity(intent1);
                finish();
                break;

            case R.id.text_frimware:
                if (mService != null) {
                    mService.disconnect();
                }
                if (task != null) {
                    task.cancel();
                }
                Intent intent = new Intent(getApplicationContext(), DfuActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_alarm:
                if (isChecked) {
                    editor.remove("alarm");
                    editor.putString("alarm", "alarm on");
                    editor.commit();
                } else {
                    editor.remove("alarm");
                    editor.putString("alarm", "alarm off");
                    editor.commit();
                }
                break;
            case R.id.switch_call:
                if (isChecked) {
                    editor.remove("call");
                    editor.putString("call", "call on");
                    editor.commit();
                } else {
                    editor.remove("call");
                    editor.putString("call", "call off");
                    editor.commit();
                }
                break;
            case R.id.switch_message:
                if (isChecked) {
                    editor.remove("message");
                    editor.putString("message", "message on");
                    editor.commit();
                } else {
                    editor.remove("message");
                    editor.putString("message", "message off");
                    editor.commit();
                }
                break;
            case R.id.switch_kakao:
                if (isChecked) {
                    editor.remove("kakao");
                    editor.putString("kakao", "kakao on");
                    editor.commit();
                } else {
                    editor.remove("kakao");
                    editor.putString("kakao", "kakao off");
                    editor.commit();
                }
                break;
        }
    }
}
