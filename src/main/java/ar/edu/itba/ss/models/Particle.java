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

    private final DoublePair[] nextR = new DoublePair[3];

    private DoublePair predV;

    private final DoublePair forces = new DoublePair(0.0, 0.0);

//    private static double toDeleteX = 10;
//    private static double toDeleteY = Constants.RE_ENTRANCE_THRESHOLD;

    public Particle(double radius, Point position) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.vx = 0;
        this.vy = 0;
        this.position = position;
        predV = new DoublePair(0.0, 0.0);
    }


    public void setNextR(int index, DoublePair pair) {
        this.nextR[index] = pair;
    }

    public DoublePair getCurrentR(int index) {
        return currR[index];
    }

    public void setCurrR(int index, DoublePair pair) {
        currR[index] = pair;
    }

    public DoublePair getPrevR(int index) {
        return prevR[index];
    }

    public void initRs() {
        currR[0] = new DoublePair(position.getX(), position.getY());
        currR[1] = new DoublePair(0.0, 0.0);
        currR[2] = new DoublePair(0.0, -Constants.GRAVITY);

        prevR[0] = new DoublePair(Integration.eulerR(position.getX(), vx, -Constants.STEP, mass, 0),
                Integration.eulerR(position.getY(), vy, -Constants.STEP, mass,
                        -Constants.GRAVITY * mass));

        prevR[1] = new DoublePair(Integration.eulerV(vx, -Constants.STEP, mass, 0),
                Integration.eulerV(vy, -Constants.STEP, mass, -Constants.GRAVITY * mass));

        prevR[2] = new DoublePair(0.0, -Constants.GRAVITY);
    }

    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        Point pos = new Point(nextR[0].getFirst(), nextR[0].getSecond());
        Point otherPos = new Point(other.nextR[0].getFirst(), other.nextR[0].getSecond());
        double realDistance = pos.distanceTo(otherPos);
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

        for (Walls wall : wallNeighbours) {
            DoublePair normalVerser = wall.getNormal();
            double overlap = 0;
            switch (wall) {
                case TOP -> overlap = radius - Math.abs((Constants.LENGTH + Space.nextYPos - nextR[0].getSecond()));
                case LEFT -> overlap = radius - nextR[0].getFirst();
                case RIGHT -> overlap = radius - Math.abs((Constants.WIDTH - nextR[0].getFirst()));
                case BOTTOM ->
                        overlap = radius - Math.abs(nextR[0].getSecond() - Space.nextYPos - Constants.RE_ENTRANCE_THRESHOLD);
            }

            double fn = -Constants.KN * Math.abs(overlap);
            double ft = tangentialForceWithWall(normalVerser, overlap);

            fx += fn * normalVerser.getFirst() - ft * normalVerser.getSecond();
            fy += fn * normalVerser.getSecond() + ft * normalVerser.getFirst();
        }

        return new DoublePair(fx,fy);
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
        Point pos = new Point(nextR[0].getFirst(), nextR[0].getSecond());
        Point otherPos = new Point(other.nextR[0].getFirst(), other.nextR[0].getSecond());
        return Math.abs(radius + other.getRadius() - otherPos.distanceTo(pos));
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
        double dx = other.getNextR(0).getFirst() - nextR[0].getFirst();
        double dy = other.getNextR(0).getSecond() - nextR[0].getSecond();
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

    public DoublePair getNextR(int index) {
        return nextR[index];
    }

    public DoublePair getPredV() {
        return predV;
    }

    public void setPredV(DoublePair predV) {
        this.predV = predV;
    }

    public void setPrevR(int index, DoublePair pair) {
        prevR[index] = pair;
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

