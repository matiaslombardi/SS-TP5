package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.util.List;

public class Space {
    private final static int[][] DIRECTIONS = new int[][]{new int[]{-1, 0}, new int[]{-1, 1},
            new int[]{0, 0}, new int[]{0, 1}, new int[]{1, 1}};

    private final Cell[][] cells;
    private final List<Particle> particleList;

    private final double colSize;
    private final double rowSize;

    private final int gridM;
    private final int gridN;

    public static double yPos = 0;
    public static double nextYPos;

    private final double angularW;


    public Space(List<Particle> particles, double angularW) {
        this.angularW = angularW;
        this.particleList = particles;

        double maxRadius = particles.stream().mapToDouble(Particle::getRadius).max().orElseThrow(RuntimeException::new); //TODO check exception
        double l = Constants.LENGTH + Constants.RE_ENTRANCE_THRESHOLD; // TODO check
        double w = Constants.WIDTH;
        this.gridM = (int) Math.floor(l / (2 * maxRadius));
        this.gridN = (int) Math.floor(w / (2 * maxRadius));

        this.rowSize = l / gridM;
        this.colSize = w / gridN;
        this.cells = new Cell[gridM][gridN];
        Space.nextYPos = Constants.A * Math.sin(angularW * Constants.STEP);
    }

    public void update(double t) {
//        particleList.forEach(p -> {
//            p.beemanFirstStepX();
//            p.beemanFirstStepY();
//        });

        this.positionParticles();
        this.calculateNeighbours();
//        particleList.forEach(Particle::calculateForces);
//        particleList.forEach(p -> {
//            p.beemanX();
//            p.beemanY();
//        });
        //particleList.forEach(p -> p.update(spaceSize));

        yPos = Constants.A * Math.sin(angularW * t);
        nextYPos = Constants.A * Math.sin(angularW * (t + Constants.STEP));
    }

    public void getNextRs(double elapsed) {
        // First set nextR[0] for each particle
        particleList.forEach(p -> {
            // First set nextR[0] for each particle
            DoublePair currR0 = p.getCurrentR(0);
            DoublePair currR1 = p.getCurrentR(1);
            DoublePair currR2 = p.getCurrentR(2);

            DoublePair prevR2 = p.getPrevR(2);

            double r0X = Integration.beemanR(currR0.getFirst(), currR1.getFirst(), Constants.STEP,
                    currR2.getFirst(), prevR2.getFirst());
            double r0Y = Integration.beemanR(currR0.getSecond(), currR1.getSecond(), Constants.STEP,
                    currR2.getSecond(), prevR2.getSecond());
            p.setNextR(0, new DoublePair(r0X, r0Y));

            // Predict R[1] for each particle
            double r1X = Integration.beemanPredV(currR1.getFirst(), Constants.STEP, currR2.getFirst(),
                    prevR2.getFirst());
            double r1Y = Integration.beemanPredV(currR1.getSecond(), Constants.STEP, currR2.getSecond(),
                    prevR2.getSecond());

            p.setPredV(new DoublePair(r1X, r1Y));
        });

        positionParticles();
        calculateNeighbours(); // TODO: check que este con lo predicho
        // Correct R[1] for each particle
        particleList.forEach(p -> {
            DoublePair force = p.calculateForces();

            //DoublePair currR0 = p.getCurrentR(0);
            DoublePair currR1 = p.getCurrentR(1);
            DoublePair currR2 = p.getCurrentR(2);
            DoublePair prevR2 = p.getPrevR(2);

            double r1X = Integration.beemanV(currR1.getFirst(), Constants.STEP, currR2.getFirst(),
                    prevR2.getFirst(), force.getFirst() / p.getMass());
            double r1Y = Integration.beemanV(currR1.getSecond(), Constants.STEP, currR2.getSecond(),
                    prevR2.getSecond(), force.getSecond() / p.getMass());

            p.setNextR(1, new DoublePair(r1X, r1Y)); // TODO: se usa de vuelta?
            p.setNextR(2, new DoublePair(force.getFirst() / p.getMass(),
                    force.getSecond() / p.getMass()));
        });

        particleList.forEach(p -> {
            p.setPrevR(0, p.getCurrentR(0));
            p.setPrevR(1, p.getCurrentR(1));
            p.setPrevR(2, p.getCurrentR(2));

            p.setCurrR(0, p.getNextR(0));
            p.setCurrR(1, p.getNextR(1));
            p.setCurrR(2, p.getNextR(2));
        });

        // TODO: MOVER PAREDES
    }

    private void positionParticles() {
        for (int i = 0; i < gridM; i++) {
            for (int j = 0; j < gridN; j++) {
                this.cells[i][j] = null;
            }
        }

        for (Particle particle : this.particleList) {
            DoublePair position = particle.getNextR(0);
            int row = getRow(position);
            int col = getCol(position);

            if (cells[row][col] == null)
                cells[row][col] = new Cell();

            cells[row][col].addParticle(particle);
        }
    }

    public void calculateNeighbours() {
        this.particleList.forEach(particle -> {
            particle.removeAllNeighbours();

            DoublePair position = particle.getNextR(0);
            int row = getRow(position);
            int col = getCol(position);

            checkWallCollision(particle, row, col);

            for (int[] dir : DIRECTIONS) {
                int currRow = row + dir[0];
                int currCol = col + dir[1];

                if (currRow < 0 || currRow >= gridM || currCol < 0
                        || currCol >= gridN || cells[currRow][currCol] == null ||
                        cells[currRow][currCol].getParticles().isEmpty())
                    continue;

                cells[currRow][currCol].getParticles().stream()
                        .filter(particle::isColliding)
                        .forEach(p -> {
                            particle.addNeighbour(p);
                            p.addNeighbour(particle);
                        });
            }
        });
    }

    public void reenterParticles() {
        particleList.forEach(p -> {
            if (p.getPosition().getY() <= -Constants.RE_ENTRANCE_THRESHOLD) {
                Point newPos = ParticleGenerator.generateParticlePosition(particleList, p.getId(),
                        p.getRadius(), true);

                p.setPosition(newPos);
            }
        });
    }

    private void checkWallCollision(Particle particle, int row, int col) {
        if (row == 0) {
            double y = particle.getNextR(0).getSecond() - particle.getRadius();
            if (Double.compare(y, nextYPos) <= 0) {
                particle.addWall(Walls.BOTTOM); // TODO: slit
                System.out.println("BOTTOM");
            }

            // TODO: Que pasa si choco desde abajo del slit hacia arriba
        }

        if (row == gridM - 1) {
            double y = particle.getNextR(0).getSecond() + particle.getRadius();
            if (Double.compare(y, nextYPos + Constants.LENGTH) >= 0) {
                particle.addWall(Walls.TOP);
                System.out.println("TOP");
            }
        }

        double y = particle.getNextR(0).getSecond();
        if (Double.compare(y, nextYPos) >= 0) {
            if (col == 0) {
                if (Double.compare(particle.getNextR(0).getFirst(), particle.getRadius()) <= 0) {
                    particle.addWall(Walls.LEFT);
                    System.out.println("LEFT");
                }
            }

            if (col == gridN - 1) {
                if (Double.compare(particle.getNextR(0).getFirst() + particle.getRadius(), Constants.WIDTH) >= 0) {
                    particle.addWall(Walls.RIGHT);
                    System.out.println("RIGHT");
                }
            }
        }
    }

    private int getRow(DoublePair position) {
        int toRet = (int) ((position.getSecond() - nextYPos) / rowSize);
        if (toRet < 0)
            toRet = 0;
        else if (toRet > gridM - 1)
            toRet = gridM - 1;

        return toRet;
    }

    private int getCol(DoublePair position) {
        int toRet = (int) (position.getFirst() / colSize);
        if (toRet < 0)
            toRet = 0;
        else if (toRet > gridN - 1)
            toRet = gridN - 1;

        return toRet;
//        return 0;
    }

    public List<Particle> getParticleList() {
        return particleList;
    }
}
