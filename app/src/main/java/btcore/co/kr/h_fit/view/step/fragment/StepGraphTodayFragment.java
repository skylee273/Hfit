package btcore.co.kr.h_fit.view.step.fragment;

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
import btcore.co.kr.h_fit.database.ContactDBStep;
import btcore.co.kr.h_fit.database.DB_Step;

import static btcore.co.kr.h_fit.database.ContactDBStep.COL_HOUR;

/**
 * Created by leehaneul on 2018-01-30.
 */

public class StepGraphTodayFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    private final String TAG = getClass().getSimpleName();
    private LineChart SGTodayChart;
    private DB_Step StepDB = null;
    private String dateValue[];
    private int hourValue[];
    private int weekValue[];
    private int stepValue[];
    private int setStepValue[];
    private int setTodayValue[];
    private boolean StepDbFlag = false;
    SQLiteDatabase Database;

    private Context mContext;

    public StepGraphTodayFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_step_today, container, false);

        mContext = this.getActivity();
        SGTodayChart = rootView.findViewById(R.id.today_chart);
        init_stepTables();
        load_stepVaule();
        if(StepDbFlag == false){
            initChartView();
        }else{
            initNonChartView();
        }

        return rootView;
    }

    private void init_stepTables() {

        StepDB = new DB_Step(mContext);
    }

    private void load_stepVaule() {
        int index = 0;
        int stepSum = 0;
        int checkHour = 0;
        int checkIndex = 0;
        Database = StepDB.getReadableDatabase();
        Cursor cursor = Database.rawQuery(ContactDBStep.SQL_SELECT_TODAY + "'" + getDate()+ "'" + " ORDER BY " + COL_HOUR + " ASC", null);

        if(cursor.getCount() > 0){
            dateValue = new String[cursor.getCount()];
            weekValue = new int[cursor.getCount()];
            hourValue = new int[cursor.getCount()];
            stepValue = new int[cursor.getCount()];
            while (cursor.moveToNext()) {

                    if(index == 0){
                        dateValue[index] = cursor.getString(0);
                        weekValue[index] = cursor.getInt(1);
                        hourValue[index] = cursor.getInt(2);
                        stepValue[index] = cursor.getInt(3);
                        checkHour = cursor.getInt(2);
                    }else{
                        if(checkHour == cursor.getInt(2)){
                            stepSum =  cursor.getInt(3);
                            stepValue[checkIndex] = stepSum;
                        }else{
                            ++checkIndex;
                            checkHour = cursor.getInt(2);
                            hourValue[checkIndex] = cursor.getInt(2);
                            stepValue[checkIndex] = cursor.getInt(3);
                        }
                    }
                index++;

                }

            setStepValue = new int[checkIndex + 1];
            setTodayValue = new int[checkIndex + 1];
            for(int i = 0; i <= checkIndex; i++){
                setStepValue[i] = stepValue[i];
                setTodayValue[i] = hourValue[i];
            }

            if(setStepValue.length == 1 && setStepValue[0] == 0){
                nonSetData();
            }else{
                setData(setStepValue, setTodayValue);
            }
        }else {
            StepDbFlag = true;
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
        SGTodayChart.setOnChartGestureListener(this);
        SGTodayChart.setOnChartValueSelectedListener(this);

        Legend l = SGTodayChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        SGTodayChart.setDescription("");
        SGTodayChart.setNoDataTextDescription("You need to provide data for the chart.");

        int color = getResources().getColor(R.color.color_chart_xy);
        SGTodayChart.setDescriptionColor(Color.WHITE);
        SGTodayChart.getXAxis().setTextColor(color);
        SGTodayChart.getXAxis().setLabelsToSkip(5);
        SGTodayChart.getAxisLeft().setAxisMaxValue(200f);
        SGTodayChart.getAxisLeft().setLabelCount(6, true);
        SGTodayChart.getAxisLeft().setTextColor(color);
        SGTodayChart.getAxisLeft().setAxisMinValue(0f);

        SGTodayChart.getAxisRight().setEnabled(false);
        SGTodayChart.getLegend().setTextColor(Color.WHITE);
        SGTodayChart.animateXY(2000, 2000);
        SGTodayChart.invalidate();
    }

    private void initChartView() {
        SGTodayChart.setOnChartGestureListener(this);
        SGTodayChart.setOnChartValueSelectedListener(this);

        load_stepVaule();
        Legend l = SGTodayChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        SGTodayChart.setDescription("");
        SGTodayChart.setNoDataTextDescription("You need to provide data for the chart.");

        int color = getResources().getColor(R.color.color_chart_xy);
        SGTodayChart.setDescriptionColor(Color.WHITE);
        SGTodayChart.getXAxis().setTextColor(color);
        SGTodayChart.getAxisLeft().setTextColor(color);
        SGTodayChart.getAxisLeft().setAxisMinValue(0f);

        SGTodayChart.getAxisRight().setEnabled(false);
        SGTodayChart.getLegend().setTextColor(Color.WHITE);
        SGTodayChart.animateXY(2000, 2000);
        SGTodayChart.invalidate();
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            SGTodayChart.highlightValues(null);
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
        Log.i("LOWHIGH", "low: " + SGTodayChart.getLowestVisibleXIndex()
                + ", high: " + SGTodayChart.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + SGTodayChart.getXChartMin()
                + ", xmax: " + SGTodayChart.getXChartMax()
                + ", ymin: " + SGTodayChart.getYChartMin()
                + ", ymax: " + SGTodayChart.getYChartMax());
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

    private ArrayList<Entry> setYAxisValues(int[] step , int[] hour) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for(int i = 0; i < 24; i++){
            yVals.add(new Entry(0, i));
        }
        for(int i = 0; i <= step.length - 1 ; i++){
            yVals.set(hour[i], new Entry(step[i], hour[i]));
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

        set1 = new LineDataSet(yVals, "걸음 선");
        set1.setFillAlpha(110);
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
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);
        SGTodayChart.setData(data);

    }

    private void nonSetData(){
        ArrayList<String> xVals = nonSetXAxisValues();

        ArrayList<Entry> yVals = nonSetYAxisValues();

        LineDataSet set1;

        set1 = new LineDataSet(yVals, "걸음 선");
        set1.setFillAlpha(110);
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
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);
        SGTodayChart.setData(data);
    }
    @Override
    public void onDestroy() {
        if(Database != null){
            Database.close();
        }
        super.onDestroy();
    }


}