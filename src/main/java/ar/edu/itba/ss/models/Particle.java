package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Particle {
    private static int SEQ = 0;
    private final int id;

    private final double radius;
    private final double mass;

    private final Set<Particle> neighbours = new HashSet<>();

    private final DoublePair[] curr = new DoublePair[3];
    private final DoublePair[] prev = new DoublePair[3];
    private final DoublePair[] next = new DoublePair[3];
    private DoublePair predV;

    public Particle(double radius, DoublePair position) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.setCurr(R.POS, position);
        predV = new DoublePair(0.0, 0.0);
    }

    public Particle(int id, double radius, DoublePair position) {
        this.id = id;
        SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.setCurr(R.POS, position);
        predV = new DoublePair(0.0, 0.0);
    }

    public void initRs() {
        curr[R.VEL] = new DoublePair(0.0, 0.0);
        curr[R.ACC] = new DoublePair(0.0, -Constants.GRAVITY);

        prev[R.POS] = new DoublePair(Integration.eulerR(curr[R.POS].getFirst(), 0.0, -Constants.STEP, mass, 0),
                Integration.eulerR(curr[R.POS].getSecond(), 0.0, -Constants.STEP, mass,
                        -Constants.GRAVITY * mass));

        prev[R.VEL] = new DoublePair(Integration.eulerV(0.0, -Constants.STEP, mass, 0),
                Integration.eulerV(0.0, -Constants.STEP, mass, -Constants.GRAVITY * mass));

        prev[R.ACC] = new DoublePair(0.0, -Constants.GRAVITY);
    }

    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        double realDistance = curr[R.POS].distanceTo(other.getCurrent(R.POS));
        return Double.compare(realDistance, radius + other.getRadius()) <= 0;
    }

    public DoublePair calculateForces() {
        double fx = 0;
        double fy = -mass * Constants.GRAVITY;
        for (Particle neighbour : neighbours) {
            DoublePair normalVerser = getCollisionVerser(neighbour);
            double overlap = getOverlap(neighbour);
            double fn = -Constants.KN * overlap;
            double ft = tangentialForce(neighbour, normalVerser, overlap);

            fx += fn * normalVerser.getFirst() - ft * normalVerser.getSecond();
            fy += fn * normalVerser.getSecond() + ft * normalVerser.getFirst();
        }

        return new DoublePair(fx, fy);
    }

    private double tangentialForce(double rVx, double rVy, DoublePair normalVerser, double overlap) {
        double relativeVt = -rVx * normalVerser.getSecond() + rVy * normalVerser.getFirst();
        return -Constants.KT * overlap * relativeVt;
    }

    private double tangentialForce(Particle other, DoublePair normalVerser, double overlap) {
        double relativeVx = predV.getFirst() - other.getPredV().getFirst();
        double relativeVy = predV.getSecond() - other.getPredV().getSecond();
        return tangentialForce(relativeVx, relativeVy, normalVerser, overlap);
    }

    public double getOverlap(Particle other) {
        return Math.abs(radius + other.getRadius() - other.getNext(R.POS).distanceTo(next[R.POS]));
    }

    public void addNeighbour(Particle neighbour) {
        neighbours.add(neighbour);
    }

    public void removeAllNeighbours() {
        neighbours.clear();
    }

    public DoublePair getCollisionVerser(Particle other) {
        double dx = other.getNext(R.POS).getFirst() - next[R.POS].getFirst();
        double dy = other.getNext(R.POS).getSecond() - next[R.POS].getSecond();
        double dR = Math.sqrt(dx * dx + dy * dy);
        return new DoublePair(dx / dR, dy / dR);
    }

    public int getId() {
        return id;
    }

    public double getRadius() {
        return radius;
    }

    public double getMass() {
        return mass;
    }

    public Set<Particle> getNeighbours() {
        return neighbours;
    }

    public DoublePair getNext(int index) {
        return next[index];
    }

    public DoublePair getPredV() {
        return predV;
    }

    public void setPredV(DoublePair predV) {
        this.predV = predV;
    }

    public void setPrev(int index, DoublePair pair) {
        prev[index] = pair;
    }

    public void setNextR(int index, DoublePair pair) {
        this.next[index] = pair;
    }

    public DoublePair getCurrent(int index) {
        return curr[index];
    }

    public void setCurr(int index, DoublePair pair) {
        curr[index] = pair;
    }

    public DoublePair getPrev(int index) {
        return prev[index];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Particle particle = (Particle) o;
        return getId() == particle.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

