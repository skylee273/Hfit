package btcore.co.kr.h_fit.view.step.fragment;

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
import btcore.co.kr.h_fit.view.step.StepActivity;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by leehaneul on 2018-01-26.
 */

public class StepFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    private CircleProgressBar mStepProgressBar;
    private Timer mTimer;
    private TextView mTextToday, mTextStep;
    SharedPreferences pref;


    public StepFragment() {
        mStepProgressBar = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_step, container, false);
        pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);

        mStepProgressBar = rootView.findViewById(R.id.step_progressbar);
        mTextStep = rootView.findViewById(R.id.text_step);
        mTextToday = rootView.findViewById(R.id.text_today);
        mStepProgressBar.setMax(60);
        mStepProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), StepActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return rootView;
    }

    private Handler mHandler = new Handler();

    private Runnable mUpdateTimeTask = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {

            try {
                String step = pref.getString("step","");
                if (step.length()> 0) {
                    mTextStep.setText(step);
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

    @Subscribe
    public void FinishLoad(BusFragmentEvent mBusEvent) {
        Log.d(TAG, mBusEvent.getEventData());
        if(mBusEvent.getEventType() == 2){
            mTextStep.setText(mBusEvent.getEventData());
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "destroy");
    }
    private void simulateProgress(final String sec) {
        mStepProgressBar.setProgress(Integer.parseInt(sec));

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