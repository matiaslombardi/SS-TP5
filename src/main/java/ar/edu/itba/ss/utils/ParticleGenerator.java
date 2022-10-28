package main.java.ar.edu.itba.ss.utils;

import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.Point;
import main.java.ar.edu.itba.ss.models.Space;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParticleGenerator {
    public static List<Particle> generate(String staticFile) {
        List<Particle> particles = new ArrayList<>();
        System.out.println("Begin particle generation");
        for (int i = 0; i < Constants.PARTICLE_AMOUNT; i++) {
            double newRadius = 1;//randomNum(Constants.MIN_RADIUS, Constants.MAX_RADIUS);
            //Particle newParticle = new Particle(newRadius);
            Point position = generateParticlePosition(particles, -1, newRadius, false);
            //newParticle.setPosition(position);
            Particle newParticle = new Particle(newRadius, position);

            particles.add(newParticle);
        }

        try (FileWriter writer = new FileWriter(staticFile)) {
            writer.write(Constants.PARTICLE_AMOUNT + "\n");
            writer.write("Space width " + Constants.WIDTH + "\n");
            writer.write("Space height " + Constants.LENGTH + "\n");
        } catch (
                IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("End particle generator");

        return particles;
    }

    public static Point generateParticlePosition(List<Particle> particles, int id, double radius,
                                                 boolean reentrant) {
        boolean colliding;
        Point position;
        do {
            position = randomPosition(radius, reentrant);
            colliding = false;
            for (Particle p : particles) {
                if (id != p.getId() && isColliding(p.getPosition().getX() - position.getX(),
                        p.getPosition().getY() - position.getY(),
                        radius + p.getRadius())) {
                    colliding = true;
                    break;
                }
            }
        } while (colliding);
        return position;
    }

    private static Point randomPosition(double radius, boolean reentrant) {
        double x = randomNum(radius, Constants.WIDTH - radius);
        double y = randomNum(radius + Space.yPos + Constants.RE_ENTRANCE_THRESHOLD +
                        (reentrant ? Constants.RE_ENTRANCE_MIN_Y : 0),
                Space.yPos + Constants.LENGTH - radius);
        //+ Constants.RE_ENTRANCE_THRESHOLD - radius);

        return new Point(x, y);
    }

    private static double randomNum(double min, double max) {
        return min + Math.random() * (max - min);
    }

    private static boolean isColliding(double deltaX, double deltaY, double deltaR) {
        return Double.compare(Math.pow(deltaX, 2) + Math.pow(deltaY, 2), Math.pow(deltaR, 2)) < 0;
        // TODO: chequear
    }
}