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

import java.util.ArrayList;
import java.util.Calendar;

import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.database.ContactDBKcal;
import btcore.co.kr.h_fit.database.DB_Cal;

/**
 * Created by leehaneul on 2018-01-30.
 */

public class CalGraphWeekFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    private final String TAG = getClass().getSimpleName();
    private LineChart CGWeekChart;
    private DB_Cal CalDB = null;
    private String dateValue[];
    private int hourValue[];
    private int weekValue[];
    private int kcalValue[];
    private int setKcalValue[];
    private int setWeekValue[];
    private boolean CalDbFlag = false;
    SQLiteDatabase Database;

    private Context mContext;

    public CalGraphWeekFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_kcal_week, container, false);

        mContext = this.getActivity();
        CGWeekChart = rootView.findViewById(R.id.week_chart);

        init_kcalTables();
        load_kcalVaule();
        if(CalDbFlag == true){
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
        int checkWeek = 0;
        int checkIndex = 0;
        SQLiteDatabase db = CalDB.getReadableDatabase();
        Cursor cursor = db.rawQuery(ContactDBKcal.SQL_SELECT_ASC_WEEK, null);

        dateValue = new String[cursor.getCount()];
        weekValue = new int[cursor.getCount()];
        hourValue = new int[cursor.getCount()];
        kcalValue = new int[cursor.getCount()];
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                if(getDate(cursor.getString(0))){
                    CalDbFlag = true;
                    if(index == 0){
                        dateValue[index] = cursor.getString(0);
                        weekValue[index] = cursor.getInt(1);
                        hourValue[index] = cursor.getInt(2);
                        kcalValue[index] = cursor.getInt(3);
                        checkWeek = cursor.getInt(1);
                    }else{
                        if(checkWeek == cursor.getInt(1)){
                            kcalSum = cursor.getInt(3);
                            kcalValue[checkIndex] = kcalSum;
                        }else{
                            ++checkIndex;
                            checkWeek = cursor.getInt(1);
                            weekValue[checkIndex] = cursor.getInt(1);
                            kcalValue[checkIndex] = cursor.getInt(3);
                        }
                    }

                }

                index++;
            }
            setKcalValue = new int[checkIndex + 1];
            setWeekValue = new int[checkIndex + 1];

            for(int i = 0; i <= checkIndex; i++){
                setKcalValue[i] = kcalValue[i];
                setWeekValue[i] = weekValue[i];
            }
            setData(setKcalValue, setWeekValue);

        }else{
            nonSetData();
        }

    }
    private boolean getDate(String date){
        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        String weekdate[] = date.split("-");
        int year = Integer.parseInt(weekdate[0]);
        int mMonth = Integer.parseInt(weekdate[1]);
        int mDate = Integer.parseInt(weekdate[2]);
        c.set(year, mMonth-1, mDate);
        String week = String.valueOf(c.get(Calendar.WEEK_OF_MONTH));
        String week1 = String.valueOf(c1.get(Calendar.WEEK_OF_MONTH));
        Calendar c2 = Calendar.getInstance();

        int month = c2.get(Calendar.MONTH) +  1;
        String checkMonth = String.format("%02d",month);
        if(week.equals(week1) && checkMonth.equals(weekdate[1])){
            return true;
        }else{
            return false;
        }
    }
    private void initNonChartView(){
        CGWeekChart.setOnChartGestureListener(this);
        CGWeekChart.setOnChartValueSelectedListener(this);



        // get the legend (only possible after setting data)
        Legend l = CGWeekChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        CGWeekChart.setDescription("");
        CGWeekChart.setNoDataTextDescription("You need to provide data for the chart.");

        int color = getResources().getColor(R.color.color_chart_xy);
        CGWeekChart.setDescriptionColor(Color.WHITE);
        CGWeekChart.getXAxis().setTextColor(color);
        CGWeekChart.getAxisLeft().setAxisMinValue(0f);
        CGWeekChart.getAxisLeft().setAxisMaxValue(200f);
        CGWeekChart.getAxisLeft().setLabelCount(6, true);
        CGWeekChart.getAxisLeft().setTextColor(color);
        CGWeekChart.getAxisRight().setEnabled(false);
        CGWeekChart.getLegend().setTextColor(Color.WHITE);
        CGWeekChart.animateXY(2000, 2000);
        CGWeekChart.invalidate();
    }


    private void initChartView() {
        CGWeekChart.setOnChartGestureListener(this);
        CGWeekChart.setOnChartValueSelectedListener(this);

        // add data
        load_kcalVaule();

        // get the legend (only possible after setting data)
        Legend l = CGWeekChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        CGWeekChart.setDescription("");
        CGWeekChart.setNoDataTextDescription("You need to provide data for the chart.");
        int color = getResources().getColor(R.color.color_chart_xy);
        CGWeekChart.setDescriptionColor(Color.WHITE);
        CGWeekChart.getXAxis().setTextColor(color);
        CGWeekChart.getAxisLeft().setAxisMinValue(0f);
        CGWeekChart.getAxisLeft().setLabelCount(6, true);
        CGWeekChart.getAxisLeft().setTextColor(color);
        CGWeekChart.getAxisRight().setEnabled(false);
        CGWeekChart.getLegend().setTextColor(Color.WHITE);
        CGWeekChart.animateXY(2000, 2000);
        CGWeekChart.invalidate();
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
            CGWeekChart.highlightValues(null);
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
        Log.i("LOWHIGH", "low: " + CGWeekChart.getLowestVisibleXIndex()
                + ", high: " + CGWeekChart.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + CGWeekChart.getXChartMin()
                + ", xmax: " + CGWeekChart.getXChartMax()
                + ", ymin: " + CGWeekChart.getYChartMin()
                + ", ymax: " + CGWeekChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private ArrayList<String> setXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("일");
        xVals.add("월");
        xVals.add("화");
        xVals.add("수");
        xVals.add("목");
        xVals.add("금");
        xVals.add("토");

        return xVals;
    }

    private ArrayList<Entry> setYAxisValues(int [] week , int[] kcal) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for(int i = 0; i < 7; i++){
            yVals.add(new Entry(0, i));
        }

        for(int i = 0; i <= kcal.length - 1 ; i++){
            if(week[i] != 0){
                yVals.set(week[i] - 1, new Entry(kcal[i], week[i] -1 ));
            }else{
                yVals.set(week[i], new Entry(kcal[i], week[i]));
            }
        }

        return yVals;
    }
    private ArrayList<String> nonSetXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("일");
        xVals.add("월");
        xVals.add("화");
        xVals.add("수");
        xVals.add("목");
        xVals.add("금");
        xVals.add("토");

        return xVals;
    }

    private ArrayList<Entry> nonSetYAxisValues() {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        yVals.add(new Entry(0, 0));
        yVals.add(new Entry(0, 1));
        yVals.add(new Entry(0, 2));
        yVals.add(new Entry(0, 3));
        yVals.add(new Entry(0, 4));
        yVals.add(new Entry(0, 5));
        yVals.add(new Entry(0, 6));

        return yVals;
    }

    private void setData(int [] bpm, int [] week) {
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues(week, bpm);

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
        CGWeekChart.setData(data);

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
        CGWeekChart.setData(data);
    }
    @Override
    public void onDestroy() {
        if(Database != null){
            Database.close();
        }
        super.onDestroy();
    }

}


