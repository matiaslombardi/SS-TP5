package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.Integration;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.util.List;

public class Space {
    private final static int[][] DIRECTIONS = new int[][]{new int[]{-1, 0}, new int[]{-1, 1},
            new int[]{0, 0}, new int[]{0, 1}, new int[]{1, 1}};

    public static double SLIT_SIZE = 0.5;

    private final Cell[][] cells;
    private final List<Particle> particleList;

    private final double colSize;
    private final double rowSize;

    private final int gridM;
    private final int gridN;

    public static double yPos = 0;
    public static double nextYPos = 0;
    public static double ySpeed = 0;

    private final double angularW;


    public Space(List<Particle> particles, double angularW) {
        this.angularW = angularW;
        this.particleList = particles;

        double maxRadius = particles.stream().mapToDouble(Particle::getRadius).max().orElseThrow(RuntimeException::new); //TODO check exception
        double l = Constants.LENGTH; // TODO check
        double w = Constants.WIDTH;
        this.gridM = (int) Math.floor(l / (2 * maxRadius));
        this.gridN = (int) Math.floor(w / (2 * maxRadius));

        this.rowSize = l / gridM;
        this.colSize = w / gridN;
        this.cells = new Cell[gridM][gridN];
        Space.yPos = 0;
        Space.nextYPos = 0;
//        Space.nextYPos = Space.yPos + Constants.A * Math.sin(angularW * Constants.STEP); //TODO: Descomentar
    }

    public void getNextRs(double elapsed) {
        // First set nextR[0] and predict nextR[1] for each particle
        particleList.forEach(p -> {
            DoublePair currR0 = p.getCurrent(R.POS);
            DoublePair currR1 = p.getCurrent(R.VEL);

            DoublePair currR2 = p.getCurrent(R.ACC);
            DoublePair prevR2 = p.getPrev(R.ACC);

            // Next Position for each particle
            double r0X = Integration.beemanR(currR0.getFirst(), currR1.getFirst(), Constants.STEP,
                    currR2.getFirst(), prevR2.getFirst());
            double r0Y = Integration.beemanR(currR0.getSecond(), currR1.getSecond(), Constants.STEP,
                    currR2.getSecond(), prevR2.getSecond());
            p.setNextR(0, new DoublePair(r0X, r0Y));

            // Predict Speed for each particle
            double r1X = Integration.beemanPredV(currR1.getFirst(), Constants.STEP, currR2.getFirst(),
                    prevR2.getFirst());
            double r1Y = Integration.beemanPredV(currR1.getSecond(), Constants.STEP, currR2.getSecond(),
                    prevR2.getSecond());

            p.setPredV(new DoublePair(r1X, r1Y));
        });

        // TODO: chequear si es con los actuales o con los siguientes
        calculateNeighbours(); // TODO: check que este con lo predicho

        // Correct R[1] for each particle
        particleList.forEach(p -> {
            DoublePair force = p.calculateForces();
            DoublePair currR1 = p.getCurrent(R.VEL);
            DoublePair currR2 = p.getCurrent(R.ACC);
            DoublePair prevR2 = p.getPrev(R.ACC);

            double r1X = Integration.beemanV(currR1.getFirst(), Constants.STEP, currR2.getFirst(),
                    prevR2.getFirst(), force.getFirst() / p.getMass());
            double r1Y = Integration.beemanV(currR1.getSecond(), Constants.STEP, currR2.getSecond(),
                    prevR2.getSecond(), force.getSecond() / p.getMass());

            p.setNextR(R.VEL, new DoublePair(r1X, r1Y)); // TODO: se usa de vuelta?
        });

        particleList.forEach(p -> p.setPredV(p.getNext(R.VEL)));

        particleList.forEach(p -> {
            DoublePair force = p.calculateForces();
            p.setNextR(2, new DoublePair(force.getFirst() / p.getMass(),
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

//            particleList.stream()
//                    .filter(particle::isColliding)
//                    .forEach(p -> {
//                        particle.addNeighbour(p);
//                        p.addNeighbour(particle);
//                    });
        });
    }

    public void reenterParticles() {
        particleList.forEach(p -> {
            if (p.getCurrent(R.POS).getSecond() <= -Constants.RE_ENTRANCE_THRESHOLD) {
                DoublePair newPos = ParticleGenerator.generateParticlePosition(particleList, p.getId(),
                        p.getRadius(), true);

                p.setCurr(R.POS, newPos);
            }
        });
    }

    private void checkWallCollision(Particle particle, int row, int col) {
        double x = particle.getNext(R.POS).getFirst();
        double y = particle.getNext(R.POS).getSecond();
        double r = particle.getRadius();

        //BOTTOM
        if (row == 0) {
            double dx = Math.abs(x - Constants.WIDTH / 2); // distancia al centro
            double dy = y - nextYPos;

            if (dy >= 0 && Double.compare(r, dy) >= 0) {
                    //  Double.compare(dx, r + Space.SLIT_SIZE / 2) >= 0) {
                if (((x <= Constants.WIDTH/2 - Space.SLIT_SIZE / 2) || // TODO: check menor estricto
                        (x >= Constants.WIDTH/2 + Space.SLIT_SIZE / 2))) {
                    if (y < nextYPos) {
                        System.out.println("Abajo del centro " + (particle.getNext(R.POS).getSecond()
                                - nextYPos));
                    }
//                    DoublePair position = new DoublePair(particle.getCurrent(R.POS).getFirst(), nextYPos);
                    DoublePair position = new DoublePair(particle.getNext(R.POS).getFirst(), nextYPos);
                    Particle wall = new Particle(0, position);
                    wall.setNextR(R.POS, position);
                    wall.setNextR(R.VEL, new DoublePair(0, Space.ySpeed)); // TODO: en y la oscilacion
                    particle.addNeighbour(wall);
                } else {

                    if ((x - r <= Constants.WIDTH/2 - Space.SLIT_SIZE / 2)) {
                        DoublePair position = new DoublePair(Constants.WIDTH/2 - Space.SLIT_SIZE / 2, nextYPos);

                        Particle wall = new Particle(0, position);
                        wall.setNextR(R.POS, position);
                        wall.setNextR(R.VEL, new DoublePair(0, Space.ySpeed)); // TODO: en y la oscilacion
                        particle.addNeighbour(wall);
                    } else if (x + r >= Constants.WIDTH/2 + Space.SLIT_SIZE / 2) {
                        DoublePair position = new DoublePair(Constants.WIDTH/2 + Space.SLIT_SIZE / 2, nextYPos);

                        Particle wall = new Particle(0, position);
                        wall.setNextR(R.POS, position);
                        wall.setNextR(R.VEL, new DoublePair(0, Space.ySpeed)); // TODO: en y la oscilacion
                        particle.addNeighbour(wall);
                    }
                }
            }
        }

        //TOP
        if (row == gridM - 1) {
            double topY = y + particle.getRadius();
            if (Double.compare(topY, nextYPos + Constants.LENGTH) >= 0) {
//                particle.addWall(Walls.TOP);
                DoublePair position = new DoublePair(particle.getNext(R.POS).getFirst(), nextYPos + Constants.LENGTH);
//                DoublePair position = new DoublePair(particle.getCurrent(R.POS).getFirst(), nextYPos + Constants.LENGTH);

                Particle wall = new Particle(0, position);
                wall.setNextR(R.POS, position);
                wall.setNextR(R.VEL, new DoublePair(0, Space.ySpeed)); // TODO: en y la oscilacion
                particle.addNeighbour(wall);
            }
        }

        //LEFT
        if (Double.compare(y, nextYPos) >= 0) {
            if (col == 0) {
                if (Double.compare(x, particle.getRadius()) <= 0) {
                    //particle.addWall(Walls.LEFT);
                    DoublePair position = new DoublePair(0, particle.getNext(R.POS).getSecond());
//                    DoublePair position = new DoublePair(0, particle.getCurrent(R.POS).getSecond());

                    Particle wall = new Particle(0, position);
                    wall.setNextR(R.POS, position);
                    wall.setNextR(R.VEL, new DoublePair(0, Space.ySpeed)); // TODO: en y la oscilacion
                    particle.addNeighbour(wall);
                }
            }

            //RIGHT
            if (col == gridN - 1) {
                if (Double.compare(x + particle.getRadius(), Constants.WIDTH) >= 0) {
                    DoublePair position = new DoublePair(Constants.WIDTH, particle.getNext(R.POS).getSecond());
//                    DoublePair position = new DoublePair(Constants.WIDTH, particle.getCurrent(R.POS).getSecond());
                    Particle wall = new Particle(0, position);
                    wall.setNextR(R.POS, position);
                    wall.setNextR(R.VEL, new DoublePair(0, Space.ySpeed)); // TODO: en y la oscilacion

                    particle.addNeighbour(wall);
                }
            }
        }
    }

    private int getRow(DoublePair position) {
        int toRet = (int) ((position.getSecond() - nextYPos) / rowSize); // TODO: check
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
