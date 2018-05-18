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

import java.util.ArrayList;
import java.util.Calendar;

import btcore.co.kr.h_fit.R;
import btcore.co.kr.h_fit.database.ContactDBCHeart;
import btcore.co.kr.h_fit.database.DB_HeartRate;

/**
 * Created by leehaneul on 2018-01-30.
 */

public class HeartGraphMonthFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    private final String TAG = getClass().getSimpleName();
    private LineChart HGMonthChart;
    private DB_HeartRate HeartDB = null;
    private int dateValue[];
    private String monthValue[];
    private int hourValue[];
    private int weekValue[];
    private int bpmValue[];
    private int setBpmValue[];
    private int setMonthValue[];
    private boolean HeartDataFlag = false;
    SQLiteDatabase Database;
    private Context mContext;


    public HeartGraphMonthFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_heart_month, container, false);

        mContext = this.getActivity();
        HGMonthChart = rootView.findViewById(R.id.month_chart);

        init_heartTables();
        load_HeartVaule();
        if(HeartDataFlag == true){
            initChartView();
        }else{
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
        int cehckMonth = 0;
        int checkIndex = 0;
         Database = HeartDB.getReadableDatabase();
        Cursor cursor = Database.rawQuery(ContactDBCHeart.SQL_SELECT_ASC_DATE, null);

        if(cursor.getCount() > 0){
            dateValue = new int[cursor.getCount()];
            weekValue = new int[cursor.getCount()];
            hourValue = new int[cursor.getCount()];
            bpmValue = new int[cursor.getCount()];
            while (cursor.moveToNext()) {

                Log.d(TAG,"BPM DATA :        " +  String.valueOf(cursor.getInt(3)) + "HOUR DATA :        " +  String.valueOf(cursor.getInt(2)));

                if(getMonth(cursor.getString(0))){
                    HeartDataFlag = true;
                    if(index == 0){
                        monthValue = cursor.getString(0).split("-");
                        dateValue[index] = Integer.parseInt(monthValue[2]);
                        weekValue[index] = cursor.getInt(1);
                        hourValue[index] = cursor.getInt(2);
                        bpmValue[index] = cursor.getInt(3);
                        cehckMonth = cursor.getInt(1);
                    }else{
                        monthValue = cursor.getString(0).split("-");
                        if(cehckMonth == Integer.parseInt(monthValue[2])){
                            bpmSum =  bpmSum + cursor.getInt(3);
                            bpmAvg = bpmSum / bpmCount;
                            bpmValue[checkIndex] = bpmAvg;
                            bpmCount += 1;
                        }else{
                            bpmCount = 1;
                            bpmSum = 0;
                            bpmAvg = 0;
                            ++checkIndex;
                            cehckMonth = cursor.getInt(1);
                            dateValue[checkIndex] = Integer.parseInt(monthValue[2]);
                            bpmValue[checkIndex] = cursor.getInt(3);
                        }
                    }

                }

                index++;
            }
            setBpmValue = new int[checkIndex + 1];
            setMonthValue = new int[checkIndex + 1];

            for(int i = 0; i <= checkIndex; i++){
                setBpmValue[i] = bpmValue[i];
                setMonthValue[i] = dateValue[i];
            }
            setData(setBpmValue, setMonthValue);
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
        HGMonthChart.setOnChartGestureListener(this);
        HGMonthChart.setOnChartValueSelectedListener(this);


        Legend l = HGMonthChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);


        HGMonthChart.setDescription("");
        HGMonthChart.setNoDataTextDescription("You need to provide data for the chart.");
        HGMonthChart.getAxisLeft().setAxisMinValue(0f);
        int color = getResources().getColor(R.color.color_chart_xy);
        HGMonthChart.setDescriptionColor(Color.WHITE);
        HGMonthChart.getXAxis().setTextColor(color);
        HGMonthChart.getXAxis().setLabelsToSkip(4);
        HGMonthChart.getAxisLeft().setAxisMaxValue(200f);
        HGMonthChart.getAxisLeft().setTextColor(color);
        HGMonthChart.getAxisRight().setEnabled(false);
        HGMonthChart.getLegend().setTextColor(Color.WHITE);
        HGMonthChart.animateXY(2000, 2000);
        HGMonthChart.invalidate();
    }
    
    private void initChartView() {
        HGMonthChart.setOnChartGestureListener(this);
        HGMonthChart.setOnChartValueSelectedListener(this);


        Legend l = HGMonthChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        HGMonthChart.setDescription("");
        HGMonthChart.setNoDataTextDescription("You need to provide data for the chart.");
        HGMonthChart.getAxisLeft().setAxisMinValue(0f);
        int color = getResources().getColor(R.color.color_chart_xy);
        HGMonthChart.setDescriptionColor(Color.WHITE);
        HGMonthChart.getXAxis().setTextColor(color);
        HGMonthChart.getXAxis().setLabelsToSkip(4);
        HGMonthChart.getAxisLeft().setTextColor(color);
        HGMonthChart.getAxisRight().setEnabled(false);
        HGMonthChart.getLegend().setTextColor(Color.WHITE);
        HGMonthChart.animateXY(2000, 2000);
        HGMonthChart.invalidate();
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            HGMonthChart.highlightValues(null);
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
        Log.i("LOWHIGH", "low: " + HGMonthChart.getLowestVisibleXIndex()
                + ", high: " + HGMonthChart.getHighestVisibleXIndex());

        Log.i("MIN MAX", "xmin: " + HGMonthChart.getXChartMin()
                + ", xmax: " + HGMonthChart.getXChartMax()
                + ", ymin: " + HGMonthChart.getYChartMin()
                + ", ymax: " + HGMonthChart.getYChartMax());
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

    private ArrayList<Entry> setYAxisValues(int [] month , int[] bpm) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for(int i = 0; i <= 31; i++){
            yVals.add(new Entry(0, i));
        }

        for(int i = 0; i <= bpm.length - 1 ; i++){
            yVals.set(month[i], new Entry(bpm[i], month[i]));
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
        for(int i = 0; i <= 31; i++ ){
            yVals.add(new Entry(0, i));
        }

        return yVals;
    }

    private void setData(int [] bpm, int [] month) {
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues(month, bpm);

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
        HGMonthChart.setData(data);

    }

    private void nonSetData(){
        ArrayList<String> xVals = nonSetXAxisValues();

        ArrayList<Entry> yVals = nonSetYAxisValues();

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
        set1.setLineWidth(1f);
        set1.setDrawValues(false);

        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setDrawFilled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        HGMonthChart.setData(data);
    }

    @Override
    public void onDestroy() {
        if(Database != null){
            Database.close();
        }
        super.onDestroy();
    }
}

