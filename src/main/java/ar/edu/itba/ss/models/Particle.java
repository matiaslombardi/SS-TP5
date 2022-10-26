package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Particle {
    private static int SEQ = 0;
    private final int id;
    private Point position;
    private final double radius;

    private final double mass;

    private double vx;

    private double vy;

    private final Set<Particle> neighbours = new HashSet<>();

    public Particle(double radius) {
        this.id = SEQ++;
        this.mass = Constants.MASS;
        this.radius = radius;
        this.vx = 0;
        this.vy = 0;
    }

    public boolean isColliding(Particle other) {
        if (this.equals(other))
            return false;

        double realDistance = position.distanceTo(other.getPosition());

        return Double.compare(realDistance, radius + other.getRadius()) <= 0;
    }

    public double getOverlap(Particle other){
        return radius + other.getRadius() - position.distanceTo(other.getPosition());
    }

    public void addNeighbour(Particle neighbour) {
        neighbours.add(neighbour);
    }

    public void removeAllNeighbours() {
        neighbours.clear();
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
