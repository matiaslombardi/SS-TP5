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

    // TODO: Ver si lo pasamos a un Pair velocities
    private double vx;
    private double vy;
    

    private final Set<Particle> neighbours = new HashSet<>();
    private final Set<Walls> wallNeighbours = new HashSet<>();

    private final DoublePair[] currR = new DoublePair[3];
    private final DoublePair[] prevR = new DoublePair[3];

    private final DoublePair nextR;

    private final DoublePair predV;

    private final DoublePair forces = new DoublePair(0.0, 0.0);

    public Particle(double radius, Point position) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.vx = 0;
        this.vy = 0;
        this.position = position;

        currR[0] = new DoublePair(position.getX(), position.getY());
        currR[1] = new DoublePair(0.0, 0.0);
        currR[2] = new DoublePair(0.0, -Constants.GRAVITY);

        prevR[0] = new DoublePair(Integration.eulerR(position.getX(), vx, -Constants.STEP, mass, 0),
                Integration.eulerR(position.getY(), vy, -Constants.STEP, mass, -Constants.GRAVITY));

        prevR[1] = new DoublePair(Integration.eulerV(0, -Constants.STEP, mass, 0),
                Integration.eulerV(0, -Constants.STEP, mass, -Constants.GRAVITY));

        prevR[2] = new DoublePair(0.0, -Constants.GRAVITY); // TODO: check

        nextR = new DoublePair(0.0, 0.0);
        predV = new DoublePair(0.0, 0.0);
        beemanFirstStepX();
        beemanFirstStepY();

    }


    public void beemanX() {
        beeman(DoublePair::getFirst, DoublePair::setFirst);
        position.setX(currR[0].getFirst());
        vx = currR[1].getFirst();
    }

    public void beemanY() {
        beeman(DoublePair::getSecond, DoublePair::setSecond);
        position.setY(currR[0].getSecond());
        vy = currR[1].getSecond();
    }

    public void beemanFirstStepX() {
        beemanFirstStep(DoublePair::getFirst, DoublePair::setFirst);
    }

    public void beemanFirstStepY() {
        beemanFirstStep(DoublePair::getSecond, DoublePair::setSecond);
    }

    public void beemanFirstStep(Function<DoublePair, Double> getter, BiConsumer<DoublePair, Double> setter) {
        double currentR = getter.apply(currR[0]);
        double currV = getter.apply(currR[1]);
        double currA = getter.apply(currR[2]);

        double prevA = getter.apply(prevR[2]);

        setter.accept(nextR, Integration.beemanR(currentR, currV, Constants.STEP, currA, prevA));
        setter.accept(predV, Integration.beemanPredV(currV, Constants.STEP, currA, prevA));
    }

    public void beeman(Function<DoublePair, Double> getter, BiConsumer<DoublePair, Double> setter) {
        double nextA = getter.apply(forces) / mass;

        double nextV = Integration.beemanV(getter.apply(currR[1]), Constants.STEP, getter.apply(currR[2]), getter.apply(prevR[2]), nextA); //TODO: next A como se calcula? ver si se usa predV

        setter.accept(prevR[0], getter.apply(currR[0]));
        setter.accept(prevR[1], getter.apply(currR[1]));
        setter.accept(prevR[2], getter.apply(currR[2]));
        setter.accept(currR[0], getter.apply(nextR));
        setter.accept(currR[1], nextV);
        setter.accept(currR[2], nextA);
    }

    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        Point pos = new Point(nextR.getFirst(), nextR.getSecond());
        Point otherPos = new Point(other.nextR.getFirst(), other.nextR.getSecond());
        double realDistance = pos.distanceTo(otherPos);
        return Double.compare(realDistance, radius + other.getRadius()) <= 0;
    }

    public void calculateForces() {
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

        for (Walls wall : wallNeighbours) {
            DoublePair normalVerser = wall.getNormal();
            double overlap = 0;
            switch (wall) {
                case TOP -> overlap = radius - Math.abs((Constants.LENGTH + Space.nextYPos - nextR.getSecond()));
                case LEFT -> overlap = radius - nextR.getFirst();
                case RIGHT -> overlap = radius - Math.abs((Constants.WIDTH - nextR.getFirst()));
                case BOTTOM -> overlap = radius - Math.abs(nextR.getSecond() - Space.nextYPos);
            }

            double fn = -Constants.KN * overlap;
            double ft = tangentialForceWithWall(normalVerser, overlap);

            fx += fn * normalVerser.getFirst() - ft * normalVerser.getSecond();
            fy += fn * normalVerser.getSecond() + ft * normalVerser.getFirst();
        }

        forces.setFirst(fx);
        forces.setSecond(fy);
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

    private double tangentialForceWithWall(DoublePair normalVerser, double overlap) {
        double relativeVx = predV.getFirst();
        double relativeVy = predV.getSecond(); // estabamos haciendo vy-w) ?????

        return tangentialForce(relativeVx, relativeVy, normalVerser, overlap);
    }

    public double getOverlap(Particle other) {
        Point pos = new Point(nextR.getFirst(), nextR.getSecond());
        Point otherPos = new Point(other.nextR.getFirst(), other.nextR.getSecond());
        return radius + other.getRadius() - otherPos.distanceTo(pos);
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

    public DoublePair getCollisionVerser(Particle other) {
        double dx = other.getNextR().getFirst() - nextR.getFirst();
        double dy = other.getNextR().getSecond() - nextR.getSecond();
        double dR = Math.sqrt(dx * dx + dy * dy);
        return new DoublePair(dx / dR, dy / dR);
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

    public DoublePair getNextR() {
        return nextR;
    }

    public DoublePair getPredV() {
        return predV;
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

