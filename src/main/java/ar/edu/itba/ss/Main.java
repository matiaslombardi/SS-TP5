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
        List<Particle> particles = ParticleGenerator.generate("./outFiles/input.txt");

        double elapsed = 0;
        double angularW = 5; // Levantar de args
        Space space = new Space(particles, angularW);
        int iter = 0;

        try (FileWriter outFile = new FileWriter("./outFiles/out.txt");
             FileWriter yPosFile = new FileWriter("./outFiles/yPos.txt")) {

            particles.forEach(Particle::initRs);
            while (Double.compare(elapsed, 1000) < 0) {
                particles = space.getParticleList();
                outFile.write(Constants.PARTICLE_AMOUNT + "\n");
                outFile.write("iter " + iter + "\n");
                iter++;

                // TODO: delta t2 para guardar las posiciones
                for (Particle p : particles)
                    outFile.write(String.format(Locale.ROOT, "%d %f %f %f\n", p.getId(),
                            p.getCurrentR(0).getFirst(),
                            p.getCurrentR(0).getSecond(), p.getRadius())); // TODO: que ponemos

                yPosFile.write(String.format(Locale.ROOT, "%f\n", Space.yPos));
                //space.update(elapsed);

                space.getNextRs(elapsed);
                //TODO: updetear el silo aca
                //Space.yPos = Constants.A * Math.sin(angularW * elapsed);
                //Space.nextYPos = Constants.A * Math.sin(angularW * (elapsed + Constants.STEP));
                elapsed += Constants.STEP;
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }
}