package com.example.prefixa_01.odgtest1;

import android.content.Context;

/**
 * Created by Humberto on 12/02/2016.
 */
public class Circle {

    private double mX;
    private double mY;
    private double mRadius;
    private static Circle circle;

    public static Circle get(Context context){
        if(circle == null){
            circle = new Circle(context);
        }
        return circle;
    }


    private Circle(Context context){
    }

    /*public Circle(double x, double y, double radius){
        mX = x;
        mY = y;
        mRadius = radius;
    }*/

    public double getX() {
        return mX;
    }

    public void setX(double x) {
        mX = x;
    }

    public double getY() {
        return mY;
    }

    public void setY(double y) {
        mY = y;
    }

    public double getRadius() {
        return mRadius;
    }

    public void setRadius(double radius) {
        mRadius = radius;
    }
}