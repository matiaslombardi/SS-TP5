package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.util.List;

public class Space {
    private final static int[][] DIRECTIONS = new int[][]{new int[]{-1, 0}, new int[]{-1, 1},
            new int[]{0, 0}, new int[]{0, 1}, new int[]{1, 1}};

    public static double SLIT_SIZE = 3;
    private final Cell[][] cells;
    private final List<Particle> particleList;

    private final double colSize;
    private final double rowSize;

    private final int gridM;
    private final int gridN;

    public static double yPos = 0;
    public static double ySpeed = 0;

    public Space(List<Particle> particles) {
        this.particleList = particles;

        double maxRadius = particles.stream().mapToDouble(Particle::getRadius).max()
                .orElseThrow(RuntimeException::new);

        double l = Constants.LENGTH;
        double w = Constants.WIDTH;
        this.gridM = (int) Math.floor(l / (2 * maxRadius));
        this.gridN = (int) Math.floor(w / (2 * maxRadius));

        this.rowSize = l / gridM;
        this.colSize = w / gridN;
        this.cells = new Cell[gridM][gridN];
    }

    public void getNextRs() {
        // First set nextR[0] and predict nextR[1] for each particle
        for (Particle particle : particleList) {
            DoublePair currPos = particle.getCurrent(R.POS);
            DoublePair currVel = particle.getCurrent(R.VEL);

            DoublePair currAcc = particle.getCurrent(R.ACC);
            DoublePair prevAcc = particle.getPrev(R.ACC);

            // Next Position for each particle
            double r0X = Integration.beemanR(currPos.getFirst(), currVel.getFirst(), Constants.STEP,
                    currAcc.getFirst(), prevAcc.getFirst());
            double r0Y = Integration.beemanR(currPos.getSecond(), currVel.getSecond(), Constants.STEP,
                    currAcc.getSecond(), prevAcc.getSecond());
            particle.setNextR(0, new DoublePair(r0X, r0Y));

            // Predict Speed for each particle
            double r1X = Integration.beemanPredV(currVel.getFirst(), Constants.STEP, currAcc.getFirst(),
                    prevAcc.getFirst());
            double r1Y = Integration.beemanPredV(currVel.getSecond(), Constants.STEP, currAcc.getSecond(),
                    prevAcc.getSecond());

            particle.setPredV(new DoublePair(r1X, r1Y));

        }

        calculateNeighbours();

        // Correct R[1] for each particle
        particleList.forEach(p -> {
            DoublePair force = p.calculateForces();
            DoublePair currVel = p.getCurrent(R.VEL);
            DoublePair currAcc = p.getCurrent(R.ACC);
            DoublePair prevAcc = p.getPrev(R.ACC);

            double r1X = Integration.beemanV(currVel.getFirst(), Constants.STEP, currAcc.getFirst(),
                    prevAcc.getFirst(), force.getFirst() / p.getMass());
            double r1Y = Integration.beemanV(currVel.getSecond(), Constants.STEP, currAcc.getSecond(),
                    prevAcc.getSecond(), force.getSecond() / p.getMass());

            p.setNextR(R.VEL, new DoublePair(r1X, r1Y));
        });

        particleList.forEach(p -> p.setPredV(p.getNext(R.VEL)));

        particleList.forEach(p -> {
            DoublePair force = p.calculateForces();
            p.setNextR(R.ACC, new DoublePair(force.getFirst() / p.getMass(),
                    force.getSecond() / p.getMass()));
        });

        particleList.forEach(p -> {
            p.setPrev(R.POS, p.getCurrent(R.POS));
            p.setPrev(R.VEL, p.getCurrent(R.VEL));
            p.setPrev(R.ACC, p.getCurrent(R.ACC));

            p.setCurr(R.POS, p.getNext(R.POS));
            p.setCurr(R.VEL, p.getNext(R.VEL));
            p.setCurr(R.ACC, p.getNext(R.ACC));
        });
    }

    private void positionParticles() {
        for (int i = 0; i < gridM; i++) {
            for (int j = 0; j < gridN; j++) {
                this.cells[i][j] = null;
            }
        }

        for (Particle particle : this.particleList) {
            DoublePair position = particle.getNext(R.POS);
            int row = getRow(position);
            int col = getCol(position);

            if (cells[row][col] == null)
                cells[row][col] = new Cell();

            cells[row][col].addParticle(particle);
        }
    }

    public void calculateNeighbours() {
        positionParticles();
        this.particleList.forEach(Particle::removeAllNeighbours);
        this.particleList.forEach(particle -> {
            DoublePair position = particle.getNext(R.POS);
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

    public int reenterParticles() {
        int count = 0;

        for (Particle p : particleList) {
            if (p.getCurrent(R.POS).getSecond() <= yPos - Constants.RE_ENTRANCE_THRESHOLD) {
                DoublePair newPos = ParticleGenerator.generateParticlePosition(particleList, p.getId(),
                        p.getRadius(), true);

                p.setCurr(R.POS, newPos);
                count++;
                p.initRs();
            }
        }
        return count;
    }

    private void checkWallCollision(Particle particle, int row, int col) {
        double x = particle.getNext(R.POS).getFirst();
        double y = particle.getNext(R.POS).getSecond();
        double r = particle.getRadius();

        // BOTTOM
        if (row == 0) {
            double dy = Math.abs(y - yPos);

            if (Double.compare(r, dy) >= 0) {
                if (((x <= Constants.WIDTH / 2 - Space.SLIT_SIZE / 2) ||
                        (x >= Constants.WIDTH / 2 + Space.SLIT_SIZE / 2))) {
                    // Choque vertical con la pared
                    DoublePair position = new DoublePair(x, yPos - r);
                    particle.addNeighbour(getWallParticle(position, r));
                } else if ((x - r <= Constants.WIDTH / 2 - Space.SLIT_SIZE / 2)) {
                    // Borde izquierdo slit
                    DoublePair position = new DoublePair(Constants.WIDTH / 2 - Space.SLIT_SIZE / 2, yPos);
                    particle.addNeighbour(getWallParticle(position, 0));
                } else if (x + r >= Constants.WIDTH / 2 + Space.SLIT_SIZE / 2) {
                    // Borde derecho slit
                    DoublePair position = new DoublePair(Constants.WIDTH / 2 + Space.SLIT_SIZE / 2, yPos);
                    particle.addNeighbour(getWallParticle(position, 0));
                }
            }
        }

        // TOP
        if (row == gridM - 1) {
            double topY = y + particle.getRadius();
            if (Double.compare(topY, yPos + Constants.LENGTH) >= 0) {
                DoublePair position = new DoublePair(x, yPos + Constants.LENGTH + r);
                particle.addNeighbour(getWallParticle(position, r));
            }
        }

        // LEFT
        if (Double.compare(y, yPos) >= 0) {
            if (col == 0) {
                if (Double.compare(x, particle.getRadius()) <= 0) {
                    DoublePair position = new DoublePair(-r, y);
                    particle.addNeighbour(getWallParticle(position, r));
                }
            }

            // RIGHT
            if (col == gridN - 1) {
                if (Double.compare(x + particle.getRadius(), Constants.WIDTH) >= 0) {
                    DoublePair position = new DoublePair(Constants.WIDTH + r, y);
                    particle.addNeighbour(getWallParticle(position, r));
                }
            }
        }
    }

    private Particle getWallParticle(DoublePair position, double radius) {
        Particle wall = new Particle(radius, position);
        wall.setNextR(R.POS, position);
        wall.setNextR(R.VEL, new DoublePair(0, Space.ySpeed));
        return wall;
    }

    private int getRow(DoublePair position) {
        int toRet = (int) ((position.getSecond() - yPos) / rowSize);
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
    }

    public List<Particle> getParticleList() {
        return particleList;
    }
}
