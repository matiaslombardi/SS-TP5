package main.java.ar.edu.itba.ss.models;

public class Pair<K> {
    private K first;
    private K second;

    public Pair(K first, K second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public K getSecond() {
        return second;
    }

    public void setSecond(K second) {
        this.second = second;
    }
}
