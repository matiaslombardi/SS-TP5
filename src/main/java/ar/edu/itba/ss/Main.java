package main.java.ar.edu.itba.ss;

import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.Space;
import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        List<Particle> particles = ParticleGenerator.generate("");

        double elapsed = 0;
        double angularW = 5; //Levantar de args
        Space space = new Space(particles, angularW);
        int iter = 0;

        try (FileWriter outFile = new FileWriter("out.txt")) {
            while (Double.compare(elapsed, 1000) < 0) {
                elapsed += Constants.STEP;
                particles = space.getParticleList();
                outFile.write(Constants.PARTICLE_AMOUNT + "\n");
                outFile.write("iter " + iter + "\n");
                iter++;

                // TODO: delta t2 para guardar las posiciones
                for (Particle p : particles)
                    outFile.write(String.format(Locale.ROOT, "%d %f %f %f\n", p.getId(),
                            p.getPosition().getX(), p.getPosition().getY(), p.getRadius())); // TODO: que ponemos

                space.update(elapsed);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }


    }
}