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

import java.util.ArrayList;
import java.util.Calendar;

import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.database.ContactDBStep;
import btcore.co.kr.h_fit.database.DB_Step;

/**
 * Created by leehaneul on 2018-01-30.
 */

public class StepGraphWeekFragment extends Fragment  implements OnChartGestureListener, OnChartValueSelectedListener {
    private final String TAG = getClass().getSimpleName();
    private LineChart SGWeekChart;
    private DB_Step StepDb = null;
    private String dateValue[];
    private int hourValue[];
    private int weekValue[];
    private int stepValue[];
    private int setStepValue[];
    private int setWeekValue[];
    SQLiteDatabase Database;
    private boolean StepDataFlag = false;

    private Context mContext;

    public StepGraphWeekFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_step_week, container, false);

        mContext = this.getActivity();
        SGWeekChart = rootView.findViewById(R.id.week_chart);

        init_stepTables();
        load_StepVaule();
        if(StepDataFlag == true){
            initChartView();
        }else{
            initNonChartView();
        }


        return rootView;
    }
    private void init_stepTables() {
        StepDb = new DB_Step(mContext);
    }

    private void load_StepVaule() {
        int index = 0;
        int stepSum = 0;
        int checkWeek = 0;
        int checkIndex = 0;
        Database = StepDb.getReadableDatabase();
        Cursor cursor = Database.rawQuery(ContactDBStep.SQL_SELECT_ASC_WEEK, null);

        dateValue = new String[cursor.getCount()];
        weekValue = new int[cursor.getCount()];
        hourValue = new int[cursor.getCount()];
        stepValue = new int[cursor.getCount()];
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Log.d(TAG,"STEP DATA :        " +  String.valueOf(cursor.getInt(3)) + "HOUR DATA :        " +  String.valueOf(cursor.getInt(2)));

                if(getDate(cursor.getString(0))){
                    StepDataFlag = true;

                    if(index == 0){
                        dateValue[index] = cursor.getString(0);
                        weekValue[index] = cursor.getInt(1);
                        hourValue[index] = cursor.getInt(2);
                        stepValue[index] = cursor.getInt(3);
                        checkWeek = cursor.getInt(1);
                    }else{
                        if(checkWeek == cursor.getInt(1)){
                            stepSum = cursor.getInt(3);
                            stepValue[checkIndex] = stepSum;
                        }else{
                            ++checkIndex;
                            checkWeek = cursor.getInt(1);
                            weekValue[checkIndex] = cursor.getInt(1);
                            stepValue[checkIndex] = cursor.getInt(3);
                        }
                    }

                }
                index++;
            }
            setStepValue = new int[checkIndex + 1];
            setWeekValue = new int[checkIndex + 1];

            for(int i = 0; i <= checkIndex; i++){
                setStepValue[i] = stepValue[i];
                setWeekValue[i] = weekValue[i];
            }
            setData(setStepValue, setWeekValue);

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
        SGWeekChart.setOnChartGestureListener(this);
        SGWeekChart.setOnChartValueSelectedListener(this);

        Legend l = SGWeekChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        SGWeekChart.setDescription("");
        SGWeekChart.setNoDataTextDescription("You need to provide data for the chart.");
        int color = getResources().getColor(R.color.color_chart_xy);
        SGWeekChart.setDescriptionColor(Color.WHITE);
        SGWeekChart.getXAxis().setTextColor(color);
        SGWeekChart.getAxisLeft().setAxisMinValue(0f);
        SGWeekChart.getAxisLeft().setAxisMaxValue(200f);
        SGWeekChart.getAxisLeft().setLabelCount(6, true);
        SGWeekChart.getAxisLeft().setTextColor(color);
        SGWeekChart.getAxisRight().setEnabled(false);
        SGWeekChart.getLegend().setTextColor(Color.WHITE);
        SGWeekChart.animateXY(2000, 2000);
        SGWeekChart.invalidate();
    }
    private void initChartView() {
        SGWeekChart.setOnChartGestureListener(this);
        SGWeekChart.setOnChartValueSelectedListener(this);

        Legend l = SGWeekChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        SGWeekChart.setDescription("");
        SGWeekChart.setNoDataTextDescription("You need to provide data for the chart.");
        int color = getResources().getColor(R.color.color_chart_xy);
        SGWeekChart.setDescriptionColor(Color.WHITE);
        SGWeekChart.getXAxis().setTextColor(color);
        SGWeekChart.getAxisLeft().setAxisMinValue(0f);
        SGWeekChart.getAxisLeft().setTextColor(color);
        SGWeekChart.getAxisRight().setEnabled(false);
        SGWeekChart.getLegend().setTextColor(Color.WHITE);
        SGWeekChart.animateXY(2000, 2000);
        SGWeekChart.invalidate();
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            SGWeekChart.highlightValues(null);
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
        Log.i("LOWHIGH", "low: " + SGWeekChart.getLowestVisibleXIndex()
                + ", high: " + SGWeekChart.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + SGWeekChart.getXChartMin()
                + ", xmax: " + SGWeekChart.getXChartMax()
                + ", ymin: " + SGWeekChart.getYChartMin()
                + ", ymax: " + SGWeekChart.getYChartMax());
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

    private ArrayList<Entry> setYAxisValues(int [] week , int[] step) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for(int i = 0; i < 7; i++){
            yVals.add(new Entry(0, i));
        }

        for(int i = 0; i <= step.length - 1 ; i++){
            if(week[i] != 0){
                yVals.set(week[i] - 1, new Entry(step[i], week[i] -1 ));
            }else{
                yVals.set(week[i], new Entry(step[i], week[i]));
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

        set1 = new LineDataSet(yVals, "걸음 선");
        set1.setFillAlpha(110);
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);
        SGWeekChart.setData(data);

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
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);

        LineData data = new LineData(xVals, dataSets);

        SGWeekChart.setData(data);
    }
    @Override
    public void onDestroy() {
        if(Database != null){
            Database.close();
        }
        super.onDestroy();
    }

}
