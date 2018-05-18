package btcore.co.kr.h_fit.view.heart.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.database.ContactDBCHeart;
import btcore.co.kr.h_fit.database.DB_HeartRate;

/**
 * Created by leehaneul on 2018-01-30.
 */

public class HeartGraphTodayFragment extends Fragment implements OnChartValueSelectedListener, OnChartGestureListener {
    private final String TAG = getClass().getSimpleName();
    private LineChart HGTodayChart;
    private DB_HeartRate HeartDB = null;
    private String dateValue[];
    private int hourValue[];
    private int weekValue[];
    private int bpmValue[];
    private int setBpmValue[];
    private int setTodayValue[];
    private Context mContext;
    private boolean HeartDbFlag = false;
    SQLiteDatabase Database;

    public HeartGraphTodayFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_heart_today, container, false);


        mContext = this.getActivity();
        HGTodayChart = rootView.findViewById(R.id.today_chart);
        init_heartTables();
        load_HeartVaule();
        if (HeartDbFlag == false) {
            initChartView();
        } else {
            initNonChartView();
        }


        return rootView;
    }


    private void init_heartTables() {

        HeartDB = new DB_HeartRate(mContext);
    }

    private void load_HeartVaule() {
        int index = 0;
        int bpmSum = 0;
        int bpmAvg = 0;
        int bpmCount = 1;
        int checkHour = 0;
        int checkIndex = 0;
        Database = HeartDB.getReadableDatabase();
        Cursor cursor = Database.rawQuery(ContactDBCHeart.SQL_SELECT_TODAY + "'" + getDate() + "'", null);

        if (cursor.getCount() > 0) {
            dateValue = new String[cursor.getCount()];
            weekValue = new int[cursor.getCount()];
            hourValue = new int[cursor.getCount()];
            bpmValue = new int[cursor.getCount()];
            while (cursor.moveToNext()) {

                Log.d(TAG, "BPM DATA :        " + String.valueOf(cursor.getInt(3)) + "HOUR DATA :        " + String.valueOf(cursor.getInt(2)));

                if (index == 0) {

                    dateValue[index] = cursor.getString(0);
                    weekValue[index] = cursor.getInt(1);
                    hourValue[index] = cursor.getInt(2);
                    bpmValue[index] = cursor.getInt(3);
                    checkHour = cursor.getInt(2);
                } else {
                    if (checkHour == cursor.getInt(2)) {
                        bpmSum = bpmSum + cursor.getInt(3);
                        bpmAvg = bpmSum / bpmCount;
                        bpmValue[checkIndex] = bpmAvg;

                        bpmCount += 1;
                    } else {
                        bpmCount = 1;
                        bpmSum = 0;
                        bpmAvg = 0;
                        ++checkIndex;
                        checkHour = cursor.getInt(2);
                        hourValue[checkIndex] = cursor.getInt(2);
                        bpmValue[checkIndex] = cursor.getInt(3);
                    }


                }

                index++;

            }
            setBpmValue = new int[checkIndex + 1];
            setTodayValue = new int[checkIndex + 1];
            for (int i = 0; i <= checkIndex; i++) {
                setBpmValue[i] = bpmValue[i];
                setTodayValue[i] = hourValue[i];
            }

            if (setBpmValue.length == 1 && setBpmValue[0] == 0) {
                nonSetData();
            } else {
                setData(setBpmValue, setTodayValue);
            }
        } else {
            HeartDbFlag = true;
            nonSetData();
        }

    }

    private String getDate() {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Date currentTime = new Date();
        String mTime = mSimpleDateFormat.format(currentTime);

        return mTime;
    }

    private void initNonChartView() {
        HGTodayChart.setOnChartGestureListener(this);
        HGTodayChart.setOnChartValueSelectedListener(this);

        // add data

        // get the legend (only possible after setting data)
        Legend l = HGTodayChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        HGTodayChart.setDescription("");
        HGTodayChart.setNoDataTextDescription("You need to provide data for the chart.");

        int color = getResources().getColor(R.color.color_chart_xy);
        HGTodayChart.setDescriptionColor(Color.WHITE);
        HGTodayChart.getXAxis().setTextColor(color);
        HGTodayChart.getXAxis().setLabelsToSkip(5);
        HGTodayChart.getAxisLeft().setAxisMaxValue(200f);
        HGTodayChart.getAxisLeft().setLabelCount(6, true);
        HGTodayChart.getAxisLeft().setTextColor(color);
        HGTodayChart.getAxisLeft().setAxisMinValue(0f);

        HGTodayChart.getAxisRight().setEnabled(false);
        HGTodayChart.getLegend().setTextColor(Color.WHITE);
        HGTodayChart.animateXY(2000, 2000);
        HGTodayChart.invalidate();
    }

    private void initChartView() {
        HGTodayChart.setOnChartGestureListener(this);
        HGTodayChart.setOnChartValueSelectedListener(this);
        Legend l = HGTodayChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        HGTodayChart.setDescription("");
        HGTodayChart.setNoDataTextDescription("You need to provide data for the chart.");
        int color = getResources().getColor(R.color.color_chart_xy);
        HGTodayChart.setDescriptionColor(Color.WHITE);
        HGTodayChart.getXAxis().setTextColor(color);
        HGTodayChart.getAxisLeft().setTextColor(color);
        HGTodayChart.getAxisLeft().setAxisMinValue(0f);
        HGTodayChart.getAxisRight().setEnabled(false);
        HGTodayChart.getLegend().setTextColor(Color.WHITE);
        HGTodayChart.animateXY(2000, 2000);
        HGTodayChart.invalidate();
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            // or highlightTouch(null) for callback to onNothingSelected(...)
            HGTodayChart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: "
                + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + HGTodayChart.getLowestVisibleXIndex()
                + ", high: " + HGTodayChart.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + HGTodayChart.getXChartMin()
                + ", xmax: " + HGTodayChart.getXChartMax()
                + ", ymin: " + HGTodayChart.getYChartMin()
                + ", ymax: " + HGTodayChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private ArrayList<String> setXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            String date = String.format("%02d", i);
            xVals.add(String.valueOf(date));
        }


        return xVals;
    }

    private ArrayList<Entry> setYAxisValues(int[] bpm, int[] hour) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < 24; i++) {
            yVals.add(new Entry(0, i));
        }

        for (int i = 0; i <= bpm.length - 1; i++) {

            yVals.set(hour[i], new Entry(bpm[i], hour[i]));
        }

        return yVals;
    }

    private ArrayList<String> nonSetXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            String date = String.format("%02d", i);
            xVals.add(String.valueOf(date));
        }

        return xVals;
    }

    private ArrayList<Entry> nonSetYAxisValues() {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < 24; i++) {
            yVals.add(new Entry(0, i));
        }

        return yVals;
    }

    private void setData(int[] bpm, int[] hour) {
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues(bpm, hour);

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "심박수 선");

        set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        // set1.enableDashedLine(10f, 5f, 0f);
        // set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setDrawValues(false);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        HGTodayChart.setData(data);

    }

    private void nonSetData() {
        ArrayList<String> xVals = nonSetXAxisValues();

        ArrayList<Entry> yVals = nonSetYAxisValues();

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "걸음 선");

        set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        // set1.enableDashedLine(10f, 5f, 0f);
        // set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.WHITE);
        //set1.setDrawValues(false);

        set1.setCircleColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawValues(false);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        HGTodayChart.setData(data);
    }

    @Override
    public void onDestroy() {
        if (Database != null) {
            Database.close();
        }
        super.onDestroy();
    }

}