package btcore.co.kr.h_fit.view.step;

import android.app.Activity;
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
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import btcore.co.kr.h_fit.bus.BusEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceProvider;
import btcore.co.kr.h_fit.bus.BusProvider;
import btcore.co.kr.h_fit.sevice.BluetoothLeService;
import btcore.co.kr.h_fit.util.DataUtils;
import btcore.co.kr.h_fit.view.main.MainActivity;
import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.view.step.fragment.StepGraphMonthFragment;
import btcore.co.kr.h_fit.view.step.fragment.StepGraphTodayFragment;
import btcore.co.kr.h_fit.view.step.fragment.StepGraphWeekFragment;
import btcore.co.kr.h_fit.view.step.model.StepData;
import btcore.co.kr.h_fit.view.step.presenter.StepPresenter;


/**
 * Created by leehaneul on 2018-01-30.
 */

public class StepActivity extends AppCompatActivity implements View.OnClickListener, StepData.View {

    private final String TAG = getClass().getSimpleName();

    private TextView mTextStep;
    private Button mButtonToday, mButtonWeek, mButtonMonth;
    private ViewPager mStepViewpager;

    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;

    private BluetoothAdapter mBtAdapter = null;
    private BluetoothLeService mService = null;
    private BluetoothDevice mDevice = null;

    private int mState = UART_PROFILE_DISCONNECTED;
    private Timer task = new Timer();

    StepData.Presenter presenter;
    DataUtils dataUtils;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);


        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        presenter = new StepPresenter(this);

        BusProvider.getInstance().register(this);
        BusPhoneToDeviceProvider.getInstance().register(this);


        initView();

        AutoScan();


    }

    private void saveDataView() {
        String step = pref.getString("step","");
        if (step.length()> 0) {
            mTextStep.setText(step);
        }
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
        dataUtils = new DataUtils();

        Toolbar toolbar = findViewById(R.id.step_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.icon_menu_back);
        actionBar.setTitle("걸음수");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mTextStep = findViewById(R.id.text_steps);

        mButtonToday = findViewById(R.id.btn_today);
        mButtonWeek = findViewById(R.id.btn_week);
        mButtonMonth = findViewById(R.id.btn_month);

        mButtonToday.setOnClickListener(this);
        mButtonWeek.setOnClickListener(this);
        mButtonMonth.setOnClickListener(this);


        mStepViewpager = findViewById(R.id.viewpager_step);
        mStepViewpager.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        mStepViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mButtonToday.setTextColor(Color.parseColor("#5BF6E4"));
                        mButtonWeek.setTextColor(Color.parseColor("#FFFFFF"));
                        mButtonMonth.setTextColor(Color.parseColor("#FFFFFF"));

                        break;
                    case 1:
                        mButtonToday.setTextColor(Color.parseColor("#FFFFFF"));
                        mButtonWeek.setTextColor(Color.parseColor("#5BF6E4"));
                        mButtonMonth.setTextColor(Color.parseColor("#FFFFFF"));
                        break;
                    case 2:
                        mButtonToday.setTextColor(Color.parseColor("#FFFFFF"));
                        mButtonWeek.setTextColor(Color.parseColor("#FFFFFF"));
                        mButtonMonth.setTextColor(Color.parseColor("#5BF6E4"));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        service_init();
    }

    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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


            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {

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

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {

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

        BusProvider.getInstance().unregister(this);
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

    @Subscribe
    public void FinishLoad(BusEvent mBusEvent) {

        Log.d(TAG, mBusEvent.getEventData());
        // 이벤트가 발생한뒤 수행할 작업
        presenter.initBleData(mBusEvent.getEventData());
        // 이벤트가 발생한뒤 수행할 작업

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

        saveDataView();

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
            case R.id.btn_today:
                mStepViewpager.setCurrentItem(0);
                break;
            case R.id.btn_week:
                mStepViewpager.setCurrentItem(1);
                break;
            case R.id.btn_month:
                mStepViewpager.setCurrentItem(2);
                break;
        }
    }

    @Override
    public void showData(String data, int type) {
        if (type == 2) {
            mTextStep.setText(data);
            editor.remove("step");
            editor.putString("step",data);
            editor.commit();
            Log.d(TAG, data);
        }
    }

    @Override
    public void showErrorMesssage(String msg) {

    }

    private class pagerAdapter extends FragmentStatePagerAdapter {
        public pagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:

                    return new StepGraphTodayFragment();
                case 1:

                    return new StepGraphWeekFragment();
                case 2:

                    return new StepGraphMonthFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
