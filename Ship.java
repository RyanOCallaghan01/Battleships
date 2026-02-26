public class Ship {
    private final String name;
    private final int length;
    private int hits;
    private int[][] positions;
    private int positionCount = 0;

    public Ship(String name, int length) {
        this.name = name;
        this.length = length;
        this.hits = 0;
    }

    public void setPositions(int[][] positions) {
        this.positions = positions;
        this.positionCount = positions.length;
    }

    public boolean occupies(int row, int col) {
        for (int i = 0; i < positionCount; i++) {
            if (positions[i][0] == row && positions[i][1] == col) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public void registerHit() {
        hits++;
    }

    public boolean isSunk() {
        return hits >= length;
    }
}
