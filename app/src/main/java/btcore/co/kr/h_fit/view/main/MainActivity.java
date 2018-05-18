package btcore.co.kr.h_fit.view.main;

import android.Manifest;
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
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.otto.Subscribe;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.bus.BusEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceEvent;
import btcore.co.kr.h_fit.bus.BusPhoneToDeviceProvider;
import btcore.co.kr.h_fit.bus.BusProvider;
import btcore.co.kr.h_fit.bus.BusFragmentEvent;
import btcore.co.kr.h_fit.bus.BusFragmentProvider;
import btcore.co.kr.h_fit.sevice.BluetoothLeService;
import btcore.co.kr.h_fit.util.DataUtils;
import btcore.co.kr.h_fit.view.device.DeviceSettingActivity;
import btcore.co.kr.h_fit.view.heart.HeartActivity;
import btcore.co.kr.h_fit.view.heart.fragment.HeartFragment;
import btcore.co.kr.h_fit.view.cal.KcalActivity;
import btcore.co.kr.h_fit.view.cal.fragment.CalFragment;
import btcore.co.kr.h_fit.view.main.model.HfitData;
import btcore.co.kr.h_fit.view.main.presenter.HfitPresenter;
import btcore.co.kr.h_fit.view.profile.ProfileActivity;
import btcore.co.kr.h_fit.view.setting.SettingActivity;
import btcore.co.kr.h_fit.view.step.StepActivity;
import btcore.co.kr.h_fit.view.step.fragment.StepFragment;

import static btcore.co.kr.h_fit.sevice.BluetoothLeService.mStates;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, HfitData.View {

    public static final String TAG = "MainActivity";
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private BluetoothAdapter mBtAdapter = null;

    private int mState = UART_PROFILE_DISCONNECTED;

    private TextView mHeart, mHeartSub, mBpm, mKcal, mKcalSub, mKcals, mStep, mStepSub, mSteps, mBattery, mBatterySub, mLevel;
    private Button mHeartButton, mKcalButton, mStepButton, mBatteryButton, mFragmentHeart, mFragmentStep, mFragmentKcal;
    private ViewPager mViewPager;
    private FloatingActionButton DataSynButton;
    private Timer task;
    private boolean HfitTimeFlag = false;

    TimerTask mnTask;
    public BluetoothLeService mService = null;
    public BluetoothDevice mDevice = null;
    HfitData.Presenter presenter;
    Context mContext;
    DataUtils dataUtils;
    SharedPreferences pref;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();
        presenter = new HfitPresenter(this);
        initView();
        AutoScan();


    }
    private void AutoScan() {
        task = new Timer();
        mnTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    String address = pref.getString("deviceAddress", "");
                    String mCurrentState = pref.getString("Current", "");
                    Log.d(TAG, "기기 주소  : " + address + "현재 상태 : " + mCurrentState);
                    if (address.length() > 0 && mCurrentState.equals("off")) {
                        service_init();
                        mService.connect(address);
                    }else{
                        if (HfitTimeFlag == false) {
                            String TimeVal = String.valueOf(dataUtils.getTimeInfo(dataUtils.getDate(), dataUtils.getWeek()));
                            sendCommand(TimeVal);
                            HfitTimeFlag = true;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        };
        task.schedule(mnTask, 0, 5000);
    }

    private void saveDataView() {

        String bpm = pref.getString("heart","");
        String step = pref.getString("step","");
        String kcal = pref.getString("kcal","");
        String battery = pref.getString("battery","");

        if (bpm.length() > 0) {
            mHeart.setText(bpm);
        }
        if (kcal.length() > 0 ) {
            mKcal.setText(kcal);
        }
        if (step.length() > 0 ) {
            mStep.setText(step);
        }
        if (battery.length() > 0 ) {
            if (battery.equals("0")) {
                mBatteryButton.setBackgroundResource(R.drawable.icon_battery0);
            } else if (battery.equals("1")) {
                mBatteryButton.setBackgroundResource(R.drawable.icon_battery1);
            } else if ((battery.equals("2"))) {
                mBatteryButton.setBackgroundResource(R.drawable.icon_battery2);
            } else if ((battery.equals("3"))) {
                mBatteryButton.setBackgroundResource(R.drawable.icon_battery3);
            } else if (battery.equals("4")) {
                mBatteryButton.setBackgroundResource(R.drawable.icon_battery4);
            } else {
                mBatteryButton.setBackgroundResource(R.drawable.icon_battery0);
            }
            mBattery.setText(battery);
        }
    }

    private void initView() {
        mContext = this;
        dataUtils = new DataUtils();
        // 테드 퍼미션 라이브러리 상용
        TedPermission.with(mContext)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("권한을 거부하면,H-Fit 서비스를 사용할 수 없습니다.\n\n 권한을 설정해주세요 [Setting] > [Permission]")
                .setPermissions(android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.READ_CONTACTS
                        , android.Manifest.permission.READ_SMS, android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECEIVE_MMS)
                .check();


        BusProvider.getInstance().register(this);
        BusPhoneToDeviceProvider.getInstance().register(this);


        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setTitle("H-Fit");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mHeart = findViewById(R.id.text_bpm);
        mHeartSub = findViewById(R.id.heartsub);
        mBpm = findViewById(R.id.bpm);
        mKcal = findViewById(R.id.text_kcal);
        mKcalSub = findViewById(R.id.kcalsub);
        mKcals = findViewById(R.id.kcals);
        mStep = findViewById(R.id.text_step);
        mStepSub = findViewById(R.id.stepsub);
        mSteps = findViewById(R.id.steps);
        mBattery = findViewById(R.id.text_battery);
        mBatterySub = findViewById(R.id.batterysub);
        mLevel = findViewById(R.id.level);

        mFragmentHeart = findViewById(R.id.btn_fragmentheart);
        mFragmentStep = findViewById(R.id.btn_fragmentstep);
        mFragmentKcal = findViewById(R.id.btn_fragmentkcal);

        mHeartButton = findViewById(R.id.btn_heart);
        mKcalButton = findViewById(R.id.btn_kcal);
        mStepButton = findViewById(R.id.btn_step);
        mBatteryButton = findViewById(R.id.btn_battery);


        mHeartButton.setOnClickListener(this);
        mKcalButton.setOnClickListener(this);
        mStepButton.setOnClickListener(this);
        mBatteryButton.setOnClickListener(this);

        mFragmentHeart.setOnClickListener(this);
        mFragmentStep.setOnClickListener(this);
        mFragmentKcal.setOnClickListener(this);

        mHeart.setOnClickListener(this);
        mHeartSub.setOnClickListener(this);
        mBpm.setOnClickListener(this);
        mKcal.setOnClickListener(this);
        mKcalSub.setOnClickListener(this);
        mKcals.setOnClickListener(this);
        mStep.setOnClickListener(this);
        mStepSub.setOnClickListener(this);
        mSteps.setOnClickListener(this);
        mBattery.setOnClickListener(this);
        mBatterySub.setOnClickListener(this);
        mLevel.setOnClickListener(this);

        service_init();


        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mFragmentStep.setBackgroundResource(R.drawable.icon_fullcircle);
                        mFragmentHeart.setBackgroundResource(R.drawable.icon_circle);
                        mFragmentKcal.setBackgroundResource(R.drawable.icon_circle);
                        break;
                    case 1:
                        mFragmentStep.setBackgroundResource(R.drawable.icon_circle);
                        mFragmentHeart.setBackgroundResource(R.drawable.icon_fullcircle);
                        mFragmentKcal.setBackgroundResource(R.drawable.icon_circle);
                        break;
                    case 2:
                        mFragmentStep.setBackgroundResource(R.drawable.icon_circle);
                        mFragmentHeart.setBackgroundResource(R.drawable.icon_circle);
                        mFragmentKcal.setBackgroundResource(R.drawable.icon_fullcircle);
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(0);

        DataSynButton = findViewById(R.id.fab_ble);
        DataSynButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mBtAdapter.isEnabled()) {
                    android.util.Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    try {
                        if (mStates) {
                            DataSynButton.setImageResource(R.drawable.icon_sync_click);
                            sendCommand(dataUtils.requsetData());
                        } else {
                            Snackbar.make(getWindow().getDecorView().getRootView(), "블루투스가 연결되어 있지 않습니다.", Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }

                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {


        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {

        }

    };

    public void service_init() {
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
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


    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (task == null) {
                            AutoScan();
                        }
                        Snackbar.make(getWindow().getDecorView().getRootView(), "연결 되었습니다.", Snackbar.LENGTH_LONG).show();
                        mState = UART_PROFILE_CONNECTED;

                    }
                });
            }

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
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            Log.d(TAG, "ACTION_DATA_AVAILABLE : " + currentDateTimeString);
                            Snackbar.make(getWindow().getDecorView().getRootView(), currentDateTimeString + " 동기화 완료", Snackbar.LENGTH_LONG).show();
                            DataSynButton.setImageResource(R.drawable.icon_sync);

                        } catch (Exception e) {
                            android.util.Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                Log.d(TAG, "DEVICE_DOES_NOT_SUPPORT_UART : " + currentDateTimeString);
                mService.disconnect();
            }
        }
    };


    public void sendCommand(String data) {
        mService.writeRXCharacteristic(data);
    }


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
    public void showData(String data, int type) {
        Log.d(TAG, data);
        switch (type) {
            case 1:
                mHeart.setText(data);
                editor.remove("heart");
                editor.putString("heart",data);
                editor.commit();
                BusFragmentProvider.getInstance().post(new BusFragmentEvent(data, type));
                break;
            case 2:
                mStep.setText(data);
                editor.remove("step");
                editor.putString("step",data);
                editor.commit();
                BusFragmentProvider.getInstance().post(new BusFragmentEvent(data, type));
                break;
            case 3:
                editor.remove("battery");
                editor.putString("battery",data);
                editor.commit();
                if (data.equals("0")) {
                    mBatteryButton.setBackgroundResource(R.drawable.icon_battery0);
                } else if (data.equals("1")) {
                    mBatteryButton.setBackgroundResource(R.drawable.icon_battery1);
                } else if ((data.equals("2"))) {
                    mBatteryButton.setBackgroundResource(R.drawable.icon_battery2);
                } else if ((data.equals("3"))) {
                    mBatteryButton.setBackgroundResource(R.drawable.icon_battery3);
                } else if (data.equals("4")) {
                    mBatteryButton.setBackgroundResource(R.drawable.icon_battery4);
                } else {
                    mBatteryButton.setBackgroundResource(R.drawable.icon_battery0);
                }
                mBattery.setText(data);
                break;
            case 4:
                editor.remove("kcal");
                editor.putString("kcal",data);
                editor.commit();
                mKcal.setText(data);
                BusFragmentProvider.getInstance().post(new BusFragmentEvent(data, type));
                break;
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

                    return new StepFragment();

                case 1:

                    return new HeartFragment();
                case 2:

                    return new CalFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }


    @Subscribe
    public void FinishLoad(BusEvent mBusEvent) {

        presenter.initBleData(mBusEvent.getEventData());

    }

    @Subscribe
    public void FinishLoad(BusPhoneToDeviceEvent mBusEvent) {
        Log.d(TAG, mBusEvent.getEventData());
        sendCommand(mBusEvent.getEventData());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        if (task != null) {
            task.cancel();
        }
        BusProvider.getInstance().unregister(this);
        BusPhoneToDeviceProvider.getInstance().unregister(this);

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

        if (mStates) {
            saveDataView();
        }


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
                    Toast.makeText(this, "블루투스를 활성화 했습니다.", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    android.util.Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "블루투스를 활성화 해주세요", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                android.util.Log.e(TAG, "wrong request code");
                break;
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (mState == UART_PROFILE_CONNECTED) {
            moveTaskToBack(true);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_private) {

            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_device) {
            Intent intent = new Intent(getApplicationContext(), DeviceSettingActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_permission) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_bpm:
                try {
                    Intent intent = new Intent(getApplicationContext(), HeartActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.heartsub:
                try {
                    Intent intent = new Intent(getApplicationContext(), HeartActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.bpm:
                try {
                    Intent intent = new Intent(getApplicationContext(), HeartActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.text_kcal:
                try {
                    Intent intent = new Intent(getApplicationContext(), KcalActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.kcalsub:
                try {
                    Intent intent = new Intent(getApplicationContext(), KcalActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.kcals:
                try {
                    Intent intent = new Intent(getApplicationContext(), KcalActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.text_step:
                try {
                    Intent intent = new Intent(getApplicationContext(), StepActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stepsub:
                try {
                    Intent intent = new Intent(getApplicationContext(), StepActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.steps:
                try {
                    Intent intent = new Intent(getApplicationContext(), StepActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_kcal:
                try {
                    Intent intent = new Intent(getApplicationContext(), KcalActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_heart:
                try {
                    Intent intent = new Intent(getApplicationContext(), HeartActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_step:
                try {
                    Intent intent = new Intent(getApplicationContext(), StepActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


}
