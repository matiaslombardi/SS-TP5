package main.java.ar.edu.itba.ss.models;

public class DoublePair {
    private double first;
    private double second;

    public DoublePair(double first, double second) {
        this.first = first;
        this.second = second;
    }

    public double distanceTo(DoublePair other) {
        return Math.sqrt(Math.pow(other.getFirst() - first, 2) + Math.pow(other.getSecond() - second, 2));
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    public double getSecond() {
        return second;
    }

    public void setSecond(double second) {
        this.second = second;
    }
}
