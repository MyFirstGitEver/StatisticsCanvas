package com.example.statisticscanvas2;

import android.util.Pair;

class SolverAnswer{
    public Pair<Double, Double> lineInfo;
    public Pair<Double, Double> arcInfo;

    public SolverAnswer(Pair<Double, Double> lineInfo, Pair<Double, Double> arcInfo) {
        this.lineInfo = lineInfo;
        this.arcInfo = arcInfo;
    }
}

class CircleSolver{
    private final Pair<Double, Double> center, mark, firstPoint;
    private int firstTimeFactor = -1;
    private Pair<Double, Double> start; // point A and starting point
    private final double r;

    CircleSolver(Pair<Double, Double> center, double r){
        this.center = center;
        this.r = r;
        this.start = new Pair<>(center.first, center.second - r);
        this.firstPoint = start;
        this.mark = new Pair<>(start.first - center.first, start.second - center.second);
    }

    public SolverAnswer next(double alpha){
        double lastDegree = degree(start.first, start.second);

        if(Math.abs(lastDegree + alpha - 360) < 0.0001){
            return new SolverAnswer(firstPoint, new Pair<>(lastDegree, alpha));
        }

        if(alpha > 180){
            next(180);
            SolverAnswer answer = next(alpha - 180);
            answer.arcInfo = new Pair<>(lastDegree, alpha);

            return answer;
        }

        double a = start.first - center.first;
        double b = start.second - center.second;
        double cosAlpha = Math.cos(Math.toRadians(alpha));

        double A = 1 + (a * a)/(b * b);
        double R0 = r*r*cosAlpha/ b;
        double B = -2 * R0 * a / b;
        double C = R0*R0 - r*r;

        double x1;
        double y1;
        double x2;
        double y2;

        if(Math.abs(b) > 0.0001){
            x1 = Quadratic.getRoot1(A, B, C);
            y1 = (r*r*cosAlpha - a * x1) / b + center.second;
            x2 = Quadratic.getRoot2(A, B, C);
            y2 = (r*r*cosAlpha - a*x2) / b + center.second;
        }
        else{
            x1 = r*r*cosAlpha / a;
            y1 = Math.sqrt(r*r - x1*x1);
            y2 = -y1;
            x2 = x1;

            y1 += center.second;
            y2 += center.second;
        }

        x1 += center.first;
        x2 += center.first;

        double check1 = degree(x1, y1);
        double check2 = degree(x2, y2);

        if(check1 * firstTimeFactor >= check2 * firstTimeFactor){
            start = new Pair<>(x1, y1);
        }
        else{
            start = new Pair<>(x2, y2);
        }

        if(firstTimeFactor == -1){
            firstTimeFactor = 1;
        }

        return new SolverAnswer(start, new Pair<>(lastDegree, alpha));
    }

    public Pair<Double, Double> getStartPoint(){
        return start;
    }

    public Pair<Double, Double> getCenter(){
        return center;
    }

    public double degree(double x, double y){
        double term1 = x - center.first;
        double term2 = y - center.second;

        if(term1 == 0 && term2 == 0){
            return 0;
        }
        double cos = (mark.first * term1 + mark.second * term2) / (r * r);

        if(Math.abs(x - center.first) < 0.001 || x > center.first){
            return Math.toDegrees(Math.acos(cos));
        }
        else{
            return 360 - Math.toDegrees(Math.acos(cos));
        }
    }
}

class Quadratic {
    public static  double getRoot1(double x, double y, double z) {

        return ((-1*y) + (Math.sqrt((Math.pow(y, 2) - (4*x*z)))))/(2*x);
    }

    public static  double getRoot2(double x, double y, double z) {

        return ((-1*y) - (Math.sqrt((Math.pow(y, 2) - (4*x*z)))))/(2*x);
    }
}