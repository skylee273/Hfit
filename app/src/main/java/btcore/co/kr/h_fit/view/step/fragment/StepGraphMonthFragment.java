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

public class StepGraphMonthFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    private final String TAG = getClass().getSimpleName();
    private LineChart SGMonthChart;
    private DB_Step StepDB = null;
    private int dateValue[];
    private String monthValue[];
    private int hourValue[];
    private int weekValue[];
    private int stepValue[];
    private int setStepValue[];
    private int setMonthValue[];
    private boolean StepDataFlag = false;
    SQLiteDatabase Database;

    private Context mContext;
    public StepGraphMonthFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_step_month, container, false);


        mContext = this.getActivity();
        SGMonthChart = rootView.findViewById(R.id.month_chart);

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

        StepDB = new DB_Step(mContext);
    }

    private void load_StepVaule() {
        int index = 0;
        int stepSum = 0;
        int cehckMonth = 0;
        int checkIndex = 0;
         Database = StepDB.getReadableDatabase();
        Cursor cursor = Database.rawQuery(ContactDBStep.SQL_SELECT_ASC_DATE, null);

        if(cursor.getCount() > 0){
            dateValue = new int[cursor.getCount()];
            weekValue = new int[cursor.getCount()];
            hourValue = new int[cursor.getCount()];
            stepValue = new int[cursor.getCount()];
            while (cursor.moveToNext()) {

                if(getMonth(cursor.getString(0))){
                    StepDataFlag = true;

                    if(index == 0){
                        monthValue = cursor.getString(0).split("-");
                        dateValue[index] = Integer.parseInt(monthValue[2]);
                        weekValue[index] = cursor.getInt(1);
                        hourValue[index] = cursor.getInt(2);
                        stepValue[index] = cursor.getInt(3);
                        cehckMonth = cursor.getInt(1);
                    }else{
                        monthValue = cursor.getString(0).split("-");
                        if(cehckMonth == Integer.parseInt(monthValue[2])){
                            stepSum =  stepSum + cursor.getInt(3);
                            stepValue[checkIndex] = stepSum;
                        }else{
                            stepSum = 0;
                            ++checkIndex;
                            cehckMonth = cursor.getInt(1);
                            dateValue[checkIndex] = Integer.parseInt(monthValue[2]);
                            stepValue[checkIndex] = cursor.getInt(3);
                        }
                    }

                }

                index++;
            }
            setStepValue = new int[checkIndex + 1];
            setMonthValue = new int[checkIndex + 1];

            for(int i = 0; i <= checkIndex; i++){
                setStepValue[i] = stepValue[i];
                setMonthValue[i] = dateValue[i];
            }
            setData(setStepValue, setMonthValue);
        }else{
            nonSetData();
        }

    }

    private boolean getMonth(String date){
        boolean flag = false;
        String mMonth = null;
        Calendar c = Calendar.getInstance();
        String month[] = date.split("-");
        int mm = c.get(c.MONTH) + 1;
        mMonth = String.format("%02d",mm);
        if(month[1].equals(mMonth)){
            flag = true;
        }

        return flag;
    }

    private void initNonChartView(){
        SGMonthChart.setOnChartGestureListener(this);
        SGMonthChart.setOnChartValueSelectedListener(this);

        // add data

        // get the legend (only possible after setting data)
        Legend l = SGMonthChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);


        SGMonthChart.setDescription("");
        SGMonthChart.setNoDataTextDescription("You need to provide data for the chart.");
        SGMonthChart.getAxisLeft().setAxisMinValue(0f);
        int color = getResources().getColor(R.color.color_chart_xy);
        SGMonthChart.setDescriptionColor(Color.WHITE);
        SGMonthChart.getXAxis().setTextColor(color);
        SGMonthChart.getXAxis().setLabelsToSkip(4);
        SGMonthChart.getAxisLeft().setAxisMaxValue(200f);
        SGMonthChart.getAxisLeft().setTextColor(color);
        SGMonthChart.getAxisRight().setEnabled(false);
        SGMonthChart.getLegend().setTextColor(Color.WHITE);
        SGMonthChart.animateXY(2000, 2000);
        SGMonthChart.invalidate();
    }


    private void initChartView() {
        SGMonthChart.setOnChartGestureListener(this);
        SGMonthChart.setOnChartValueSelectedListener(this);

        // add data
        load_StepVaule();

        // get the legend (only possible after setting data)
        Legend l = SGMonthChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);


        SGMonthChart.setDescription("");
        SGMonthChart.setNoDataTextDescription("You need to provide data for the chart.");
        SGMonthChart.getAxisLeft().setAxisMinValue(0f);
        int color = getResources().getColor(R.color.color_chart_xy);
        SGMonthChart.setDescriptionColor(Color.WHITE);
        SGMonthChart.getXAxis().setTextColor(color);
        SGMonthChart.getXAxis().setLabelsToSkip(4);
        SGMonthChart.getAxisLeft().setTextColor(color);
        SGMonthChart.getAxisRight().setEnabled(false);
        SGMonthChart.getLegend().setTextColor(Color.WHITE);
        SGMonthChart.animateXY(2000, 2000);
        SGMonthChart.invalidate();
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
            SGMonthChart.highlightValues(null);
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
        Log.i("LOWHIGH", "low: " + SGMonthChart.getLowestVisibleXIndex()
                + ", high: " + SGMonthChart.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + SGMonthChart.getXChartMin()
                + ", xmax: " + SGMonthChart.getXChartMax()
                + ", ymin: " + SGMonthChart.getYChartMin()
                + ", ymax: " + SGMonthChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }


    private ArrayList<String> setXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();

        for(int i = 0; i <= 31; i++ ){
            String date = String.format("%02d",i);
            xVals.add(String.valueOf(date));
        }

        return xVals;
    }

    private ArrayList<Entry> setYAxisValues(int [] month , int[] step) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for(int i = 0; i <= 31; i++){
            yVals.add(new Entry(0, i));
        }

        for(int i = 0; i <= step.length - 1 ; i++){
            yVals.set(month[i], new Entry(step[i], month[i]));
        }

        return yVals;
    }
    private ArrayList<String> nonSetXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        for(int i = 0; i <= 31; i++ ){
            String date = String.format("%02d",i);
            xVals.add(String.valueOf(date));
        }

        return xVals;
    }

    private ArrayList<Entry> nonSetYAxisValues() {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for(int i = 0; i<= 31; i ++ ){
            yVals.add(new Entry(0, i));
        }

        return yVals;
    }

    private void setData(int [] bpm, int [] month) {
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues(month, bpm);

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
        SGMonthChart.setData(data);

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
        set1.setDrawValues(false);
        set1.setDrawCircleHole(false);
        set1.setDrawFilled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);

        SGMonthChart.setData(data);
    }
    @Override
    public void onDestroy() {
        if(Database != null){
            Database.close();
        }
        super.onDestroy();
    }

}


