package main.java.ar.edu.itba.ss.models;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private final List<Particle> particles = new ArrayList<>();
    public void addParticle(Particle particle) {
        particles.add(particle);
    }
    public List<Particle> getParticles() {
        return particles;
    }
}
