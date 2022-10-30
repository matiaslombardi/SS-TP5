package main.java.ar.edu.itba.ss;

import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.R;
import main.java.ar.edu.itba.ss.models.Space;
import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        boolean generate = Boolean.parseBoolean(args[0]);
        List<Particle> particles;
        if (generate) {
            particles = ParticleGenerator.generate("./outFiles/input.txt");
        } else {
            particles = ParticleGenerator.read("./outFiles/input.txt");
        }

        double elapsed = Constants.STEP;
        double angularW = 30; // Levantar de args
        Space.SLIT_SIZE = 4;
        Space space = new Space(particles, angularW);
        int iter = 0;

        try (FileWriter outFile = new FileWriter("./outFiles/out.txt");
             FileWriter yPosFile = new FileWriter("./outFiles/yPos.txt")) {

            particles.forEach(Particle::initRs);

            outFile.write(Constants.PARTICLE_AMOUNT + "\n");
            outFile.write("iter " + iter + "\n");
            iter++;

            for (Particle p : particles)
                outFile.write(String.format(Locale.ROOT, "%d %f %f %f\n", p.getId(),
                        p.getCurrent(R.POS).getFirst(),
                        p.getCurrent(R.POS).getSecond(), p.getRadius()));

            while (Double.compare(elapsed, Constants.SIMULATION_TIME) < 0) {
                particles = space.getParticleList();
//                //TODO: updetear el silo aca
//                Space.yPos = Constants.A * Math.sin(angularW * elapsed);
//                Space.nextYPos = Constants.A * Math.sin(angularW * (elapsed)); //+ Constants.STEP));
//                Space.ySpeed = Constants.A * angularW * Math.cos(angularW * (elapsed)); //+ Constants.STEP));

                space.getNextRs(elapsed);

                // TODO: reentrar las particulas afuera y calcular caudal
//                space.reenterParticles();

                outFile.write(Constants.PARTICLE_AMOUNT + "\n");
                outFile.write("iter " + iter + "\n");
                iter++;


                // TODO: delta t2 para guardar las posiciones
                for (Particle p : particles)
                    outFile.write(String.format(Locale.ROOT, "%d %f %f %f\n", p.getId(),
                            p.getCurrent(R.POS).getFirst(),
                            p.getCurrent(R.POS).getSecond(), p.getRadius())); // TODO: que ponemos

                yPosFile.write(String.format(Locale.ROOT, "%f\n", Space.yPos));

                elapsed += Constants.STEP;
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}