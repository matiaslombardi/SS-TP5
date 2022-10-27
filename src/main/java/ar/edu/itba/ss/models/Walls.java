package main.java.ar.edu.itba.ss.models;

public enum Walls {
    TOP(new Pair<>(0.0, 1.0), new Pair<>(-1.0, 0.0)),
    BOTTOM(new Pair<>(0.0, -1.0), new Pair<>(1.0, 0.0)),
    LEFT(new Pair<>(-1.0, 0.0), new Pair<>(0.0, -1.0)),
    RIGHT(new Pair<>(1.0, 0.0), new Pair<>(0.0, 1.0));

    private Pair<Double> normal;
    private Pair<Double> tangential;

    Walls(Pair<Double> normal, Pair<Double> tangential) {
        this.normal = normal;
        this.tangential = tangential;
    }

    public Pair<Double> getNormal() {
        return normal;
    }

    public void setNormal(Pair<Double> normal) {
        this.normal = normal;
    }

    public Pair<Double> getTangential() {
        return tangential;
    }

    public void setTangential(Pair<Double> tangential) {
        this.tangential = tangential;
    }
}
