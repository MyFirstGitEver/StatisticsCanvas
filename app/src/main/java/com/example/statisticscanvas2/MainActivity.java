package com.example.statisticscanvas2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button drawBtn;
    private StatisticsCanvas statsCanvas;
    private ExcelReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        drawBtn = findViewById(R.id.drawBtn);
        statsCanvas = findViewById(R.id.statsCanvas);

        try {
            reader = new ExcelReader("store.xlsx", this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        drawBtn.setOnClickListener(v ->{
//            statsCanvas.drawHistogramOnOneColumn(reader, 0, 3, "Salary", true);
//            statsCanvas.drawHistogramOnTwoColumn(reader, 0, "Salary",
//                    "Department", CalculationPerCategory.AVG, true);

//            statsCanvas.scatter(reader, 0, "Salary", "Age", null);
//
//            statsCanvas.drawSegments(reader, 0, "Department", "Gender",
//                    "Salary", 6);

//            statsCanvas.drawHistogramOnTwoColumn(reader, 0, "Salary",
//                    "Department", CalculationPerCategory.ADD_UP, true);

//            statsCanvas.scatter(reader, 1, "Sales", "Discount", "Segment");
//            statsCanvas.drawHistogramOnOneColumn(reader, 1, 7, "Sales", true);
            //statsCanvas.drawPieChart(reader, 1, "Sales", "Segment");
//            statsCanvas.drawHistogramOnTwoColumn(reader, 1, "Sales", "Region",
//                    CalculationPerCategory.ADD_UP, true);
//            statsCanvas.drawHistogramOnTwoColumn(reader, 1, "Sales", "Region",
//                    CalculationPerCategory.ADD_UP, false);
//            statsCanvas.drawSegments(reader, 1, "Region", "Segment", "Sales",
//                    5);
        });
    }
}