package btcore.co.kr.h_fit.view.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import btcore.co.kr.h_fit.view.device.DeviceSettingActivity;
import btcore.co.kr.h_fit.view.main.MainActivity;
import btcore.co.kr.h_fit.R;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    public BluetoothLeService mService = null;

    private Spinner mSpinner;
    private TextView mTextGender;
    private EditText mEditName, mEditHeight, mEditWeight;
    private Button mButtonNext, mButtonNextTo;
    public static SharedPreferences.Editor editor;
    public static SharedPreferences pref = null;
    String[] list = {"선택 해주세요", "남자", "여자"};

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
        setContentView(R.layout.activity_private);

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

        Toolbar toolbar = findViewById(R.id.private_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.icon_menu_back);
        actionBar.setTitle("개인정보 설정");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mTextGender = findViewById(R.id.text_gender);

        mEditName = findViewById(R.id.edit_name);
        mEditHeight = findViewById(R.id.edit_height);
        mEditWeight = findViewById(R.id.edit_weight);

        mButtonNext = findViewById(R.id.btn_next);
        mButtonNextTo = findViewById(R.id.btn_nextto);

        mButtonNext.setOnClickListener(this);
        mButtonNextTo.setOnClickListener(this);

        mSpinner = findViewById(R.id.spinner_gender);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    if (pref.getString("gender", "").length() > 0) {
                        mTextGender.setText(pref.getString("gender", ""));
                    } else {
                        mTextGender.setText("선택해주세요.");
                    }
                } else {
                    mTextGender.setText(list[position]);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTextGender.setText("");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                SharedSave();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        SharedSave();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }


    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("LocalMsg"));

        android.util.Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            android.util.Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if (pref.getString("name", "").length() > 0) {
            mEditName.setText(pref.getString("name", ""));
        }
        if (pref.getString("height", "").length() > 0) {
            mEditHeight.setText(pref.getString("height", ""));
        }
        if (pref.getString("weight", "").length() > 0) {
            mEditWeight.setText(pref.getString("weight", ""));
        }
        if (pref.getString("gender", "").length() > 0) {
            mTextGender.setText(pref.getString("gender", ""));
        } else {
            Log.d(TAG, "No SharedPreference Data");
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                SharedSave();
                Intent intent = new Intent(getApplicationContext(), DeviceSettingActivity.class);
                startActivity(intent);
                finish();

                break;
            case R.id.btn_nextto:
                SharedSave();
                Intent intent1 = new Intent(getApplicationContext(), DeviceSettingActivity.class);
                startActivity(intent1);
                finish();

                break;

        }
    }

    private void SharedSave() {
        editor.remove("name");
        editor.remove("height");
        editor.remove("weight");
        editor.remove("gender");

        if (mEditName.getText().length() > 0) {
            editor.putString("name", mEditName.getText().toString());
        }
        if (mEditHeight.getText().length() > 0) {
            editor.putString("height", mEditHeight.getText().toString());
        }
        if (mEditWeight.getText().length() > 0) {
            editor.putString("weight", mEditWeight.getText().toString());
        }
        if (mTextGender.getText().length() > 0) {
            editor.putString("gender", mTextGender.getText().toString());

        }
        editor.commit();
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

    @Subscribe
    public void FinishLoad(BusPhoneToDeviceEvent mBusEvent) {
        Log.d(TAG, mBusEvent.getEventData());
        sendCommand(mBusEvent.getEventData());
    }

    public void sendCommand(String data) {
        mService.writeRXCharacteristic(data);
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
            if (pref.getString("kakao", "").equals("kakao on") && kakaoMessage != null) {
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

        task.cancel();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
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
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
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


}
