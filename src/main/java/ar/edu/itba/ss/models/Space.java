package main.java.ar.edu.itba.ss.models;

import main.java.ar.edu.itba.ss.utils.Constants;
import main.java.ar.edu.itba.ss.utils.ParticleGenerator;

import java.util.List;

public class Space {
    private final static int[][] DIRECTIONS = new int[][]{new int[]{-1, 0}, new int[]{-1, 1},
            new int[]{0, 0}, new int[]{0, 1}, new int[]{1, 1}};

    private final Cell[][] cells;
    private final List<Particle> particleList;

    private final double xSize;
    private final double ySize;

    private final int gridM;
    private final int gridN;

    public static double yPos = 0;

    public Space(List<Particle> particles) {
        this.particleList = particles;
//        this.positionParticles();

        double maxRadius = particles.stream().mapToDouble(Particle::getRadius).max().getAsDouble();
        double l = Constants.LENGTH + Constants.RE_ENTRANCE_THRESHOLD; // TODO check
        double w = Constants.WIDTH;
        this.gridM = (int) Math.floor(l / (2 * maxRadius));
        this.gridN = (int) Math.floor(w / (2 * maxRadius));

        this.xSize = l / gridM;
        this.ySize = w / gridN;
        this.cells = new Cell[gridM][gridN];
    }

    public void update() {
        this.positionParticles();
        this.calculateNeighbours();

        

        //particleList.forEach(Particle::calculateDirection);
        //particleList.forEach(p -> p.update(spaceSize));
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

            for (int[] dir : DIRECTIONS) {
                int currRow = row + dir[0];
                int currCol = col + dir[1];

                if (currRow < 0 || currRow >= gridM || currCol < 0
                        || currCol >= gridM || cells[currRow][currCol] == null)
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

    private int getRow(Point position) {
        return (int) (position.getY() / xSize);
    }

    private int getCol(Point position) {
        return (int) ((position.getX() - yPos) / ySize);
    }

    public List<Particle> getParticleList() {
        return particleList;
    }
}
