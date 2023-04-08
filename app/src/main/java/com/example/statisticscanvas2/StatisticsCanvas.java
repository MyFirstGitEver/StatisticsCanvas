package com.example.statisticscanvas2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

public class StatisticsCanvas extends View {
    private final Paint painter = new Paint();
    private final MyTimer.OnTickListener onTickListener = this::invalidate;

    private final MyTimer animateTimer = new MyTimer(onTickListener);
    private final long timeInALoading = 200;

    private ChartManager manager;
    private Pair<Float, Float> endOfCanvasCord;

    public StatisticsCanvas(Context context) {
        super(context);
    }

    public StatisticsCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StatisticsCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(manager == null){
            loadingPhase(canvas);
            return;
        }

        animateTimer.stop();
        manager.draw(canvas, painter);
        manager = null;
    }

    public void drawPieChart(ExcelReader reader, int sheetNum, String numericColName, String categoryColName){
        manager = new PieChartManager(reader, sheetNum, numericColName, categoryColName, endOfCanvasCord);
        invalidate();
    }

    public void drawHistogramOnOneColumn(
            ExcelReader reader,
            int sheetNum, int range,
            String numericColName,
            boolean usingStrength){
        manager = new HistogramChartManager(reader, sheetNum, numericColName, endOfCanvasCord, range, usingStrength);
        invalidate();
    }

    public void drawHistogramOnTwoColumn(
            ExcelReader reader,
            int sheetNum,
            String numericColName,
            String categoryColName,
            CalculationPerCategory calculation,
            boolean usingStrength){

        manager = new HistogramChartManager(reader, sheetNum, numericColName, categoryColName, calculation,
                endOfCanvasCord,  usingStrength);
        invalidate();
    }

    public void scatter(ExcelReader reader,
                         int sheetNum,
                         String numericColName,
                         String inPairWithColName,
                         String categoryColName){
        manager = new ScatterChart(
                reader, sheetNum,
                numericColName, inPairWithColName,
                categoryColName, endOfCanvasCord);
        invalidate();
    }

    public void drawSegments(ExcelReader reader, int sheetNum,
                             @NonNull String mainCategory,
                             @NonNull  String subCategory,
                             String numericColName, int range){
        manager = new SegmentChartManager(reader, sheetNum, mainCategory, subCategory, numericColName, endOfCanvasCord, range);
        invalidate();
    }

    private void loadingPhase(Canvas canvas){
        if(!animateTimer.isTicking()){
            animateTimer.start_infinite(10);
        }

        Drawable d = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.waiting_progress_circle, null);

        long realProgress = animateTimer.getProgress() % timeInALoading;
        d.setLevel((int) ((10_000 / timeInALoading) * realProgress));
        d.setBounds(0, 0, (int)(endOfCanvasCord.first / 13), (int)(endOfCanvasCord.second / 13));
        d.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        endOfCanvasCord = new Pair<>((float)w, (float)h);
    }
}