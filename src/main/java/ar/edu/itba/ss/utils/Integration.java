package main.java.ar.edu.itba.ss.utils;

import java.io.IOException;

public class Integration {

    public static double eulerR(double r, double v, double step, double mass, double f) {
        return r + step * v + step * step * f / (2 * mass);
    }

    public static double eulerV(double v, double step, double mass, double f) {
        return v + (step * f) / mass;
    }

    public static double beemanR(double r, double v, double step, double currA, double prevA) {
        return r + v * step + (4 * currA - prevA) * step * step / 6;
    }

    public static double beemanV(double v, double step, double currA, double prevA, double nextA) {
        return v + (2 * nextA + 5 * currA - prevA) * step / 6;
    }

    public static double beemanPredV(double v, double step, double currA, double prevA) {
        return v + (3 * currA - prevA) * step / 2;
    }
}
