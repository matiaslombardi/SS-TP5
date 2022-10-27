package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.Main;
import main.java.ar.edu.itba.ss.utils.Constants;
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

    private final double angularW;


    public Space(List<Particle> particles, double angularW) {
        this.angularW = angularW;
        this.particleList = particles;
//        this.positionParticles();

        double maxRadius = particles.stream().mapToDouble(Particle::getRadius).max().orElseThrow(RuntimeException::new); //TODO check exception
        double l = Constants.LENGTH + Constants.RE_ENTRANCE_THRESHOLD; // TODO check
        double w = Constants.WIDTH;
        this.gridM = (int) Math.floor(l / (2 * maxRadius));
        this.gridN = (int) Math.floor(w / (2 * maxRadius));

        this.rowSize = l / gridM;
        this.colSize = w / gridN;
        this.cells = new Cell[gridM][gridN];
    }

    public void update(double t) {
        this.positionParticles();
        this.calculateNeighbours();


        particleList.forEach(Particle::calculateForces);
        //particleList.forEach(p -> p.update(spaceSize));



        yPos = Constants.A * Math.sin(angularW * t);
    }

    /**
     * Si overlap es menor a 0. No hay choque
     * Si overlap es mayor a 0. Hay choque
     */

    private double normalForce(double overlap) {
        return -Constants.KN * overlap;
    }

    private double tangentialForce(Particle p1, Particle p2, double overlap) {
        double relativeVx = p1.getVx() - p2.getVx();
        double relativeVy = p1.getVy() - p2.getVy();
        Pair<Double> normalVersor = p1.getCollisionVerser(p2);

        double relativeVt = -relativeVx * normalVersor.getSecond() + relativeVy * normalVersor.getFirst();

        return -Constants.KT * overlap * relativeVt;
    }

    private void positionParticles() {
        for (int i = 0; i < gridM; i++) {
            for (int j = 0; j < gridN; j++) {
                this.cells[i][j] = null;
            }
        }

        for (Particle particle : this.particleList) {
            Point position = particle.getPosition();
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

            Point position = particle.getPosition();
            int row = getRow(position);
            int col = getCol(position);

            checkWallCollision(particle, row, col);

            for (int[] dir : DIRECTIONS) {
                int currRow = row + dir[0];
                int currCol = col + dir[1];

                if (currRow < 0 || currRow >= gridM || currCol < 0
                        || currCol >= gridN || cells[currRow][currCol] == null)
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
            double y = particle.getPosition().getY() - particle.getRadius();
            if (Double.compare(y, yPos) <= 0) {
                particle.addWall(Walls.BOTTOM); // TODO: slit
            }
            // TODO: Que pasa si choco desde abajo del slit hacia arriba
        }

        if (row == gridM - 1) {
            double y = particle.getPosition().getY() + particle.getRadius();
            if (Double.compare(y, yPos + Constants.LENGTH) >= 0) {
                particle.addWall(Walls.TOP);
            }
        }

        double y = particle.getPosition().getY();
        if (Double.compare(y, yPos) >= 0) {
            if (col == 0) {
                if (Double.compare(particle.getPosition().getX(), particle.getRadius()) <= 0) {
                    particle.addWall(Walls.LEFT);
                }
            }

            if (row == gridN - 1) {
                if (Double.compare(particle.getPosition().getX() + particle.getRadius(), Constants.WIDTH) >= 0) {
                    particle.addWall(Walls.RIGHT);
                }
            }
        }
    }

    private int getRow(Point position) {
        return (int) ((position.getY() - yPos) / rowSize);
    }

    private int getCol(Point position) {
        return (int) (position.getX() / colSize);
    }

    public List<Particle> getParticleList() {
        return particleList;
    }
}
