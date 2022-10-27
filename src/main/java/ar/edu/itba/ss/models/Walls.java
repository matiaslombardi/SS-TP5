package main.java.ar.edu.itba.ss.models;

public enum Walls {
    TOP(new DoublePair(0.0, 1.0), new DoublePair(-1.0, 0.0)),
    BOTTOM(new DoublePair(0.0, -1.0), new DoublePair(1.0, 0.0)),
    LEFT(new DoublePair(-1.0, 0.0), new DoublePair(0.0, -1.0)),
    RIGHT(new DoublePair(1.0, 0.0), new DoublePair(0.0, 1.0));

    private DoublePair normal;
    private DoublePair tangential;

    Walls(DoublePair normal, DoublePair tangential) {
        this.normal = normal;
        this.tangential = tangential;
    }

    public DoublePair getNormal() {
        return normal;
    }

    public void setNormal(DoublePair normal) {
        this.normal = normal;
    }

    public DoublePair getTangential() {
        return tangential;
    }

    public void setTangential(DoublePair tangential) {
        this.tangential = tangential;
    }
}
