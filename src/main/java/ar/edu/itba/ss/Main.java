package main.java.ar.edu.itba.ss;

import main.java.ar.edu.itba.ss.models.Particle;
import main.java.ar.edu.itba.ss.models.R;
import main.java.ar.edu.itba.ss.models.Space;
import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        boolean generate = Boolean.parseBoolean(args[0]);
        Space.SLIT_SIZE = Double.parseDouble(args[1]);
        double angularW = Double.parseDouble(args[2]);

        List<Particle> particles;
        if (generate) {
            particles = ParticleGenerator.generate("./outFiles/input.txt");
        } else {
            particles = ParticleGenerator.read("./outFiles/input.txt");
        }

        double elapsed = Constants.STEP;
        Space space = new Space(particles);
        int iter = 0;

        try (FileWriter outFile = new FileWriter("./outFiles/out.txt");
             FileWriter yPosFile = new FileWriter("./outFiles/yPos.txt");
             FileWriter flowFile = new FileWriter("./outFiles/flow.txt")) {
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
                Space.yPos = Constants.A * Math.sin(angularW * elapsed);
                Space.ySpeed = Constants.A * angularW * Math.cos(angularW * (elapsed));

                space.getNextRs();

                if (iter % 20 == 0) {
                    outFile.write(Constants.PARTICLE_AMOUNT + "\n");
                    outFile.write("iter " + iter + "\n");
                    for (Particle p : particles)
                        outFile.write(String.format(Locale.ROOT, "%d %f %f %f\n", p.getId(),
                                p.getCurrent(R.POS).getFirst(),
                                p.getCurrent(R.POS).getSecond(), p.getRadius()));

                    yPosFile.write(String.format(Locale.ROOT, "%f\n", Space.yPos));
                }

                int flow = space.reenterParticles();
                flowFile.write(String.format(Locale.ROOT, "%f %d\n", elapsed, flow));

                iter++;
                elapsed += Constants.STEP;
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            Date resultDate = new Date(totalTime);
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            System.out.println(sdf.format(resultDate));

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}