package main.java.ar.edu.itba.ss.utils;

import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParticleGenerator {
    public static List<Particle> generate(String staticFile, int totalParticles, double height, double width,
                                          double speed, double mass, double radius) {
        List<Particle> particles = new ArrayList<>();
        Point position;
        boolean colliding;
        for (int i = 0; i < totalParticles; i++) {
            double newRadius = randomNum(0.85, 1.15); // TODO en constantes y fijarse unidades
            Particle newParticle = new Particle(newRadius);
            do {
                position = randomPosition(Constants.LENGTH, Constants.WIDTH, newRadius);
                colliding = false;
                for (Particle p : particles) {
                    if (isColliding(p.getPosition().getX() - position.getX(),
                            p.getPosition().getY() - position.getY(),
                            newRadius + p.getRadius())) {
                        colliding = true;
                        break;
                    }
                }
            } while (colliding);

            newParticle.setPosition(position);
            particles.add(newParticle);
        }

        try (FileWriter writer = new FileWriter(staticFile)) {
            writer.write(totalParticles + "\n");
            writer.write("Space width " + width + "\n");
            writer.write("Space height " + height + "\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return particles;
    }

    private static Point randomPosition(double height, double width, double radius) {
        double x = randomNum(radius, width - radius);
        double y = randomNum(radius, height - radius);

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