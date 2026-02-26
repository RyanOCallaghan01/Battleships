import java.util.Observable;
import java.util.Random;

public class BModel extends Observable {

    static final int boardSize = 10;
    private char[][] playerBoard;
    private char[][] opponentBoard;

    private final Ship[] playerShips;
    private final Ship[] opponentShips;

    public BModel() {
        playerBoard = new char[boardSize][boardSize];
        opponentBoard = new char[boardSize][boardSize];

        playerShips = new Ship[] {
                new Ship("Carrier", 5),
                new Ship("Battleship", 4),
                new Ship("Cruiser", 3),
                new Ship("Submarine", 2), // changed from length 3 to 2 to fit specification
                new Ship("Destroyer", 2),
        };

        opponentShips = new Ship[playerShips.length];
        for (int i = 0; i < playerShips.length; i++) {
            opponentShips[i] = new Ship(playerShips[i].getName(), playerShips[i].getLength());
        }
    }

    public void initialiseBoard() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                playerBoard[i][j] = '-';
                opponentBoard[i][j] = '-';
            }
        }
    }

    public void placeShips() {
        // Place opponent ships randomly, to ensure there is no way of knowing where they are.
        Random random = new Random();
        for (Ship ship : opponentShips) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(boardSize);
                int col = random.nextInt(boardSize);
                char orientation = random.nextBoolean() ? 'H' : 'V';
                placed = placeShip(opponentBoard, ship, row, col, orientation);
            }
        }
    }

    public boolean placeShip(char[][] board, Ship ship, int startRow, int startCol, char orientation) {
        int length = ship.getLength();
        int[][] positions = new int[length][2];

        if (orientation == 'H') { // Horizontal
            // Out-of-bounds check
            if (startCol < 0 || startCol + length > boardSize || startRow < 0 || startRow >= boardSize) {
                return false;
            }
            for (int i = 0; i < length; i++) { // Overlap check
                if (board[startRow][startCol + i] != '-') {
                    return false;
                }
            }

            for (int i = 0; i < length; i++) {
                board[startRow][startCol + i] = 'S';
                positions[i][0] = startRow;
                positions[i][1] = startCol + i;
            }

        } else if (orientation == 'V') { // Vertical
            // Out-of-bounds check
            if (startRow < 0 || startRow + length > boardSize || startCol < 0 || startCol >= boardSize) {
                return false;
            }
            for (int i = 0; i < length; i++) { // Overlap check
                if (board[startRow + i][startCol] != '-') {
                    return false;
                }
            }

            for (int i = 0; i < length; i++) {
                board[startRow + i][startCol] = 'S';
                positions[i][0] = startRow + i;
                positions[i][1] = startCol;
            }
        }
        ship.setPositions(positions);
        return true;
    }

    public char[][] getPlayerBoard() {
        return playerBoard;
    }

    public char[][] getOpponentBoard() {
        return opponentBoard;
    }

    public Ship[] getPlayerShips() {
        return playerShips;
    }

    public Ship[] getOpponentShips() {
        return opponentShips;
    }

    public char[][] getOpponentBoardHidden() {
        char[][] hidden = new char[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (opponentBoard[i][j] == 'S') { // Hide location of opponent ships
                    hidden[i][j] = '-';
                } else {
                    hidden[i][j] = opponentBoard[i][j];
                }
            }
        }
        return hidden;
    }

}
