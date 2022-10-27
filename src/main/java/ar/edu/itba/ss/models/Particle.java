package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Particle {
    private static int SEQ = 0;
    private final int id;
    private Point position;
    private final double radius;
    private final double mass;
    private double vx;
    private double vy;

    private double fx = 0;
    private double fy = 0;

    private final Set<Particle> neighbours = new HashSet<>();
    private final Set<Walls> wallNeighbours = new HashSet<>();

    @SuppressWarnings("unchecked")
    private final Pair<Double>[] currR = new Pair<>[3];

    @SuppressWarnings("unchecked")
    private final Pair<Double>[] prevR = new Pair<>[3];


    public Particle(double radius, Point position) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.vx = 0;
        this.vy = 0;
        this.position = position;
        currR[0] = new Pair<>(position.getX(), position.getY());
        currR[1] = new Pair<>(0.0, 0.0);
        currR[2] = new Pair<>(0.0, -Constants.GRAVITY);

        prevR[0] = new Pair<>(Integration.eulerR(position.getX(), vx, -Constants.STEP, mass, 0),
                Integration.eulerR(position.getY(), vy, -Constants.STEP, mass, -Constants.GRAVITY));

        prevR[1] = new Pair<>(Integration.eulerV(0, -Constants.STEP, mass, 0),
                Integration.eulerV(0, -Constants.STEP, mass, -Constants.GRAVITY));

        prevR[2] = new Pair<>(0.0, -Constants.GRAVITY); // TODO: check

    }

    public void beemanX() {
        beeman(Pair::getFirst, Pair::setFirst, fx);
    }

    public void beemanY() {
        beeman(Pair::getSecond, Pair::setSecond, fy);
    }

    public void beeman(Function<Pair<Double>, Double> getter, BiConsumer<Pair<Double>, Double> setter, double force) {
        double currentR = getter.apply(currR[0]);
        double currV = getter.apply(currR[1]);
        double currA = getter.apply(currR[2]);

//        double currA = f(currR, currV) / mass;
//        double prevA = f(prevR, prevV) / mass;
        double prevA = getter.apply(prevR[2]);

        double nextR = Integration.beemanR(currentR, currV, Constants.STEP, currA, prevA);

        double predV = Integration.beemanPredV(currV, Constants.STEP, currA, prevA);

//        double nextA = f(nextR, predV) / mass;
        double nextA = force / mass;

        double nextV = Integration.beemanV(currV, Constants.STEP, currA, prevA, nextA); //TODO: next A como se calcula? ver si se usa predV
//        prevR[0] = currR[0];
//        currR[0] = nextR;

        setter.accept(prevR[0], currentR);
        setter.accept(prevR[1], currV);
        setter.accept(prevR[2], currA);
        setter.accept(currR[0], nextR);
        setter.accept(currR[1], nextV);
        setter.accept(currR[2], nextA);

//        double prevV = currV;
//        double currV = nextV;
    }

    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        double realDistance = position.distanceTo(other.getPosition());

        return Double.compare(realDistance, radius + other.getRadius()) <= 0;
    }

    public void calculateForces() {
        double fx = 0;
        double fy = mass * Constants.GRAVITY;
        for (Particle neighbour : neighbours) {
            Pair<Double> normalVerser = getCollisionVerser(neighbour);
            double overlap = getOverlap(neighbour);

            double fn = -Constants.KN * overlap;
            double ft = tangentialForce(neighbour, normalVerser, overlap);

            fx += fn * normalVerser.getFirst() - ft * normalVerser.getSecond();
            fy += fn * normalVerser.getSecond() + ft * normalVerser.getFirst();
        }

        for (Walls wall : wallNeighbours) {
            Pair<Double> normalVerser = wall.getNormal();
            double overlap = 0;
            switch (wall) {
                case TOP -> overlap = position.getY() + radius - Constants.LENGTH - Space.yPos;
                case LEFT -> overlap = position.getX() - radius;
                case RIGHT -> overlap = position.getX() + radius;
                case BOTTOM -> overlap = position.getY() - radius - Space.yPos;
            }

            double fn = -Constants.KN * overlap;
            double ft = tangentialForceWithWall(normalVerser, overlap);

            fx += fn * normalVerser.getFirst() - ft * normalVerser.getSecond();
            fy += fn * normalVerser.getSecond() + ft * normalVerser.getFirst();
        }

        this.fx = fx;
        this.fy = fy;
    }

    private double tangentialForce(double rVx, double rVy, Pair<Double> normalVerser, double overlap) {
        double relativeVt = -rVx * normalVerser.getSecond() + rVy * normalVerser.getFirst();
        return -Constants.KT * overlap * relativeVt;
    }

    private double tangentialForce(Particle other, Pair<Double> normalVerser, double overlap) {
        double relativeVx = getVx() - other.getVx();
        double relativeVy = getVy() - other.getVy();
        return tangentialForce(relativeVx, relativeVy, normalVerser, overlap);

//        double relativeVt = -relativeVx * normalVerser.getSecond() + relativeVy * normalVerser.getFirst();
//        return -Constants.KT * overlap * relativeVt;
    }

    private double tangentialForceWithWall(Pair<Double> normalVerser, double overlap) {
        double relativeVx = getVx();
        double relativeVy = getVy() - 5; // TODO: Ver que onda w

        return tangentialForce(relativeVx, relativeVy, normalVerser, overlap);

//        double relativeVt = -relativeVx * normalVerser.getSecond() + relativeVy * normalVerser.getFirst();
//        return -Constants.KT * overlap * relativeVt;
    }

    public double getOverlap(Particle other){
        return radius + other.getRadius() - position.distanceTo(other.getPosition());
    }

    public void addWall(Walls wall) {
        wallNeighbours.add(wall);
    }

    public void addNeighbour(Particle neighbour) {
        neighbours.add(neighbour);
    }

    public void removeAllNeighbours() {
        neighbours.clear();
        wallNeighbours.clear();
    }

    public Pair<Double> getCollisionVerser(Particle other){
        double dx = other.getPosition().getX() - position.getX();
        double dy = other.getPosition().getY() - position.getY();
        double dR = Math.abs((radius - other.getRadius()));
        return new Pair<>(dx / dR, dy / dR);
    }

    public int getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }

    public double getMass() {
        return mass;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public Set<Particle> getNeighbours() {
        return neighbours;
    }

    public Set<Walls> getWallNeighbours() {
        return wallNeighbours;
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
