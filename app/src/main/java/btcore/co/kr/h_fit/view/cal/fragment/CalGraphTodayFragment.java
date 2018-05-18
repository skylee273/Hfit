package btcore.co.kr.h_fit.view.cal.fragment;

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
import btcore.co.kr.h_fit.database.ContactDBKcal;
import btcore.co.kr.h_fit.database.DB_Cal;

/**
 * Created by leehaneul on 2018-01-30.
 */

public class CalGraphTodayFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    private final String TAG = getClass().getSimpleName();
    private LineChart CGTodayChart;
    private DB_Cal CalDB = null;
    private String dateValue[];
    private int hourValue[];
    private int weekValue[];
    private int kcalValue[];
    private int setKcalValue[];
    private int setTodayValue[];
    private Context mContext;
    private boolean CalDbFlag = false;
    SQLiteDatabase Database;

    public CalGraphTodayFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_kcal_today, container, false);

        mContext = this.getActivity();
        CGTodayChart = rootView.findViewById(R.id.today_chart);
        init_kcalTables();
        load_kcalVaule();
        if(CalDbFlag == false){
            initChartView();
        }else{
            initNonChartView();
        }

        return rootView;
    }
    private void init_kcalTables() {

        CalDB = new DB_Cal(mContext);
    }

    private void load_kcalVaule() {
        int index = 0;
        int kcalSum = 0;
        int checkHour = 0;
        int checkIndex = 0;
        SQLiteDatabase db = CalDB.getReadableDatabase();
        Cursor cursor = db.rawQuery(ContactDBKcal.SQL_SELECT_TODAY + "'" + getDate() + "'", null);

        if(cursor.getCount() > 0){
            dateValue = new String[cursor.getCount()];
            weekValue = new int[cursor.getCount()];
            hourValue = new int[cursor.getCount()];
            kcalValue = new int[cursor.getCount()];
            while (cursor.moveToNext()) {

                Log.d(TAG,"BPM DATA :        " +  String.valueOf(cursor.getInt(3)) + "HOUR DATA :        " +  String.valueOf(cursor.getInt(2)));

                    if(index == 0){

                        dateValue[index] = cursor.getString(0);
                        weekValue[index] = cursor.getInt(1);
                        hourValue[index] = cursor.getInt(2);
                        kcalValue[index] = cursor.getInt(3);
                        checkHour = cursor.getInt(2);
                    }else{
                        if(checkHour == cursor.getInt(2)){
                            kcalSum =  cursor.getInt(3);
                            kcalValue[checkIndex] = kcalSum;
                        }else{
                            ++checkIndex;
                            checkHour = cursor.getInt(2);
                            hourValue[checkIndex] = cursor.getInt(2);
                            kcalValue[checkIndex] = cursor.getInt(3);
                        }
                    }


                index++;

            }
            setKcalValue = new int[checkIndex + 1];
            setTodayValue = new int[checkIndex + 1];
            for(int i = 0; i <= checkIndex; i++){
                setKcalValue[i] = kcalValue[i];
                setTodayValue[i] = hourValue[i];
            }

            if(setKcalValue.length == 1 && setKcalValue[0] == 0){
                nonSetData();
            }else{
                setData(setKcalValue, setTodayValue);
            }
        }else {
            CalDbFlag = true;
            nonSetData();

        }

    }

    private String getDate(){
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd", Locale.KOREA );
        Date currentTime = new Date ();
        String mTime = mSimpleDateFormat.format ( currentTime );

        return mTime;
    }

    private void initNonChartView(){
        CGTodayChart.setOnChartGestureListener(this);
        CGTodayChart.setOnChartValueSelectedListener(this);

        // add data

        // get the legend (only possible after setting data)
        Legend l = CGTodayChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        CGTodayChart.setDescription("");
        CGTodayChart.setNoDataTextDescription("You need to provide data for the chart.");

        int color = getResources().getColor(R.color.color_chart_xy);
        CGTodayChart.setDescriptionColor(Color.WHITE);
        CGTodayChart.getXAxis().setTextColor(color);
        CGTodayChart.getXAxis().setLabelsToSkip(5);
        CGTodayChart.getAxisLeft().setAxisMaxValue(200f);
        CGTodayChart.getAxisLeft().setLabelCount(6, true);
        CGTodayChart.getAxisLeft().setTextColor(color);
        CGTodayChart.getAxisLeft().setAxisMinValue(0f);

        CGTodayChart.getAxisRight().setEnabled(false);
        CGTodayChart.getLegend().setTextColor(Color.WHITE);
        CGTodayChart.animateXY(2000, 2000);
        CGTodayChart.invalidate();
    }

    private void initChartView() {
        CGTodayChart.setOnChartGestureListener(this);
        CGTodayChart.setOnChartValueSelectedListener(this);

        // get the legend (only possible after setting data)
        Legend l = CGTodayChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        CGTodayChart.setDescription("");
        CGTodayChart.setNoDataTextDescription("You need to provide data for the chart.");

        int color = getResources().getColor(R.color.color_chart_xy);
        CGTodayChart.setDescriptionColor(Color.WHITE);
        CGTodayChart.getXAxis().setTextColor(color);
        CGTodayChart.getAxisLeft().setTextColor(color);
        CGTodayChart.getAxisLeft().setAxisMinValue(0f);

        CGTodayChart.getAxisRight().setEnabled(false);
        CGTodayChart.getLegend().setTextColor(Color.WHITE);
        CGTodayChart.animateXY(2000, 2000);
        CGTodayChart.invalidate();
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
            CGTodayChart.highlightValues(null);
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
        Log.i("LOWHIGH", "low: " + CGTodayChart.getLowestVisibleXIndex()
                + ", high: " + CGTodayChart.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + CGTodayChart.getXChartMin()
                + ", xmax: " + CGTodayChart.getXChartMax()
                + ", ymin: " + CGTodayChart.getYChartMin()
                + ", ymax: " + CGTodayChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private ArrayList<String> setXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        for(int i = 0; i < 24; i++ ){
            String date = String.format("%02d",i);
            xVals.add(String.valueOf(date));
        }


        return xVals;
    }

    private ArrayList<Entry> setYAxisValues(int[] kcal , int[] hour) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for(int i = 0; i < 24; i++){
            yVals.add(new Entry(0, i));
        }

        for(int i = 0; i <= kcal.length - 1 ; i++){
            yVals.set(hour[i], new Entry(kcal[i], hour[i]));
        }

        return yVals;
    }
    private ArrayList<String> nonSetXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        for(int i = 0; i < 24; i++ ){
            String date = String.format("%02d",i);
            xVals.add(String.valueOf(date));
        }

        return xVals;
    }

    private ArrayList<Entry> nonSetYAxisValues() {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for(int i = 0; i < 24; i++){
            yVals.add(new Entry(0, i));
        }

        return yVals;
    }

    private void setData(int [] step, int [] hour) {
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues(step, hour);

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "칼로리 선");

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
        CGTodayChart.setData(data);

    }

    private void nonSetData(){
        ArrayList<String> xVals = nonSetXAxisValues();

        ArrayList<Entry> yVals = nonSetYAxisValues();

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "칼로리 선");

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
        set1.setDrawFilled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        CGTodayChart.setData(data);
    }
    @Override
    public void onDestroy() {
        if(Database != null){
            Database.close();
        }
        super.onDestroy();
    }
}