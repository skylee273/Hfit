package btcore.co.kr.h_fit.view.heart.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.bus.BusFragmentEvent;
import btcore.co.kr.h_fit.bus.BusFragmentProvider;
import btcore.co.kr.h_fit.view.heart.HeartActivity;

import static android.content.Context.MODE_PRIVATE;


public class HeartFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private CircleProgressBar mHeartProgressBar;
    private Timer mTimer;
    private TextView mTextToday, mTextHeart;
    SharedPreferences pref;

    public HeartFragment() {
        mHeartProgressBar = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_heart, container, false);

        pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        mTextHeart = rootView.findViewById(R.id.text_heart);

        mHeartProgressBar = rootView.findViewById(R.id.heart_progressbar);
        mTextToday = rootView.findViewById(R.id.text_heart_today);
        mHeartProgressBar.setMax(60);
        mHeartProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HeartActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return rootView;
    }

    @Subscribe
    public void FinishLoad(BusFragmentEvent mBusEvent) {
        if(mBusEvent.getEventType() == 1){
            mTextHeart.setText(mBusEvent.getEventData());
        }
    }

    private Handler mHandler = new Handler();

    private Runnable mUpdateTimeTask = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            try {
                String heart = pref.getString("heart","");
                if (heart.length()> 0) {
                    mTextHeart.setText(heart);
                }
            }catch (NullPointerException e){
                Log.d(TAG,e.toString());
            }
            String strCurTime = String.format("%02d : %02d", hour(), min());
            String secondTime = String.format("%02d", sec());
            mTextToday.setText("Today  " + strCurTime);
            simulateProgress(secondTime);

        }
    };

    private int hour() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(cal.HOUR);
        return year;
    }

    private int min() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(cal.MINUTE);
        return year;
    }

    private int sec() {
        Calendar cal = Calendar.getInstance();
        int second = cal.get(cal.SECOND);
        return second;
    }


    class MainTimerTask extends TimerTask {
        public void run() {
            mHandler.post(mUpdateTimeTask);
        }
    }

    @Override
    public void onStart() {
        BusFragmentProvider.getInstance().register(this);
        Log.d(TAG, "onStart");
        super.onStart();
    }
    @Override
    public void onStop() {
        BusFragmentProvider.getInstance().unregister(this);
        super.onStop();
    }


    private void simulateProgress(final String sec) {
        mHeartProgressBar.setProgress(Integer.parseInt(sec));
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        if(isVisibleToUser)
        {
            MainTimerTask timerTask = new MainTimerTask();
            mTimer = new Timer();
            mTimer.schedule(timerTask, 0, 1000);
        }
        else
        {
            if(mTimer != null){
                mTimer.cancel();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }


}

