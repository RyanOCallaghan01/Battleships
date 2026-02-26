import javax.swing.JButton;
import java.awt.*;

public class BController {

    private final BModel model;
    private final BView view;

    private final boolean[][] alreadyGuessed;

    private int[][] hitQueue = new int[100][2];
    private int hitQueueSize = 0;

    private int playerTries = 0;
    private int opponentTries = 0;

    private enum GamePhase {
        SHIP_PLACEMENT, PLAYER_TURN, OPPONENT_TURN, GAME_OVER
    }

    private GamePhase phase = GamePhase.SHIP_PLACEMENT;
    private int currentShipIndex = 0;
    private char currentOrientation = 'H';

    public BController(BModel model, BView view) {
        this.model = model;
        this.view = view;
        alreadyGuessed = new boolean[BModel.boardSize][BModel.boardSize];
        initialiseActionListeners();
        startGame();

        view.getToggleOrientationButton().addActionListener(e -> {
            if (currentOrientation == 'H') {
                currentOrientation = 'V';
                view.getToggleOrientationButton().setText("Orientation: Vertical");
            } else {
                currentOrientation = 'H';
                view.getToggleOrientationButton().setText("Orientation: Horizontal");
            }
        });
    }

    public void startGame() {
        model.initialiseBoard();
        model.placeShips();
        view.updateBoard(model.getPlayerBoard(), view.getPlayerButtons());
        view.updateBoard(model.getOpponentBoardHidden(), view.getOpponentButtons());

        view.setPlayerButtonsEnabled(true);
        view.setOpponentButtonsEnabled(false);

        view.updateGamePhase("Ship Placement");
    }

    public void startLoadedGame() {
        phase = GamePhase.PLAYER_TURN;
        view.updateGamePhase("Player Turn");
        view.setPlayerButtonsEnabled(false); // Ensure player board interaction is off
        view.setOpponentButtonsEnabled(true); // Enable interaction with the opponent's board
        view.displayMessage("Game loaded. Your turn!");
    }

    private void initialiseActionListeners() {
        JButton[][] playerButtons = view.getPlayerButtons();
        JButton[][] opponentButtons = view.getOpponentButtons();

        for (int i = 0; i < BModel.boardSize; i++) {
            for (int j = 0; j < BModel.boardSize; j++) {
                final int row = i;
                final int col = j;

                view.getPlayerButtons()[i][j].addActionListener(e -> {
                    if (phase == GamePhase.SHIP_PLACEMENT) {
                        placeNextPlayerShip(row, col);
                    }
                });

                view.getPlayerButtons()[i][j].addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        if (phase == GamePhase.SHIP_PLACEMENT) {
                            showShipPreview(row, col);
                        }
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        if (phase == GamePhase.SHIP_PLACEMENT) {
                            clearPreview();
                        }
                    }
                });

                opponentButtons[i][j].addActionListener(e -> {
                    if (phase == GamePhase.PLAYER_TURN) {
                        playerTurn(row, col);
                    }
                });
            }
        }

    }

    private void placeNextPlayerShip(int row, int col) {
        Ship[] playerShips = model.getPlayerShips();
        if (currentShipIndex >= playerShips.length) {
            return;
        }

        Ship ship = playerShips[currentShipIndex];
        char orientation = currentOrientation;
        boolean placed = model.placeShip(model.getPlayerBoard(), ship, row, col, orientation);

        if (placed) {
            currentShipIndex++;
            view.updateBoard(model.getPlayerBoard(), view.getPlayerButtons());
            view.displayMessage("Placed " + ship.getName());

            if (currentShipIndex == playerShips.length) {
                view.displayMessage("All ships placed! Game begins!");
                phase = GamePhase.PLAYER_TURN;
                view.updateGamePhase("Player Turn");
                view.getToggleOrientationButton().setVisible(false);

                view.setPlayerButtonsEnabled(false);
                view.setOpponentButtonsEnabled(true);

                view.updateBoard(model.getOpponentBoardHidden(), view.getOpponentButtons());
            } else {
                view.displayMessage("Place ship: " + playerShips[currentShipIndex].getName());
            }
        } else {
            view.displayMessage("Invalid placement!");
        }

    }

    private void showShipPreview(int row, int col) {
        Ship ship = model.getPlayerShips()[currentShipIndex];
        int length = ship.getLength();
        JButton[][] buttons = view.getPlayerButtons();
        char[][] board = model.getPlayerBoard();

        boolean valid = isValidPlacement(board, row, col, length, currentOrientation);

        for (int i = 0; i < length; i++) {
            int r = (currentOrientation == 'V') ? row + i : row;
            int c = (currentOrientation == 'H') ? col + i : col;

            if (r >= BModel.boardSize || c >= BModel.boardSize) {
                continue;
            }

            buttons[r][c].setBackground(valid ? Color.GREEN : Color.RED);
        }
    }

    private boolean isValidPlacement(char[][] board, int row, int col, int length, char orientation) {
        for (int i = 0; i < length; i++) {
            int r = (orientation == 'V') ? row + i : row;
            int c = (orientation == 'H') ? col + i : col;

            if (r >= BModel.boardSize || c >= BModel.boardSize) {
                return false;
            }

            if (board[r][c] != '-') {
                return false;
            }
        }
        return true;
    }

    private void clearPreview() {
        view.updateBoard(model.getPlayerBoard(), view.getPlayerButtons());
    }

    private void playerTurn(int row, int col) {
        view.updateGamePhase("Player Turn");
        playerTries++;

        if (alreadyGuessed[row][col]) {
            view.displayMessage("Spot already guessed!");
            return;
        }

        alreadyGuessed[row][col] = true; // Add current guess to list of guesses
        char[][] opponentBoard = model.getOpponentBoard();
        boolean hit = false;
        boolean sunk = false;
        String sunkName = "";

        if (opponentBoard[row][col] == 'S') {
            opponentBoard[row][col] = 'X';
            hit = true;
            for (Ship ship : model.getOpponentShips()) {
                if (ship.occupies(row, col)) {
                    ship.registerHit();
                    if (ship.isSunk()) {
                        sunk = true;
                        sunkName = ship.getName();
                        break;
                    }
                }
            }
        } else if (opponentBoard[row][col] == '-') {
            opponentBoard[row][col] = 'O';
        }

        view.updateBoard(model.getOpponentBoardHidden(), view.getOpponentButtons());
        view.displayResult(row, col, hit);

        if (sunk) { // Band-Aid fix to display ship sunk message after hit message
            view.displayMessage("You sunk the opponent's " + sunkName + "!");
        }

        if (checkWin(opponentBoard)) {
            view.displayGameOver("Player");
            view.displayMessage("You won the game in " + playerTries + " turns!");
            disableOpponentButtons();
        } else {
            opponentTurn();
        }
    }

    private void opponentTurn() {
        opponentTries++;

        char[][] playerBoard = model.getPlayerBoard();
        int row = -1, col = -1;
        boolean foundTarget = false;
        boolean hit = false;
        boolean sunk = false;
        String sunkName = "";

        if (!checkWin(playerBoard)) { // Prevent Opponent's turn from lingering past victory
            phase = GamePhase.OPPONENT_TURN;
            view.updateGamePhase("Opponent Turn");
        }

        for (int i = 0; i < hitQueueSize && !foundTarget; i++) {
            int hitRow = hitQueue[i][0];
            int hitCol = hitQueue[i][1];

            int[][] neighbors = {
                    {hitRow - 1, hitCol},
                    {hitRow + 1, hitCol},
                    {hitRow, hitCol - 1},
                    {hitRow, hitCol + 1}
            };

            for (int[] n : neighbors) {
                int r = n[0], c = n[1];
                if (r >= 0 && r < BModel.boardSize && c >= 0 && c < BModel.boardSize) {
                    if (playerBoard[r][c] != 'X' && playerBoard[r][c] != 'O') {
                        row = r;
                        col = c;
                        foundTarget = true;
                        break;
                    }
                }
            }
        }

        if (!foundTarget) {
            do {
                row = (int) (Math.random() * BModel.boardSize);
                col = (int) (Math.random() * BModel.boardSize);
            } while (playerBoard[row][col] == 'X' || playerBoard[row][col] == 'O');
        }

        if (playerBoard[row][col] == 'S') {
            playerBoard[row][col] = 'X';
            hit = true;

            for (Ship ship : model.getPlayerShips()) {
                if (ship.occupies(row, col)) {
                    ship.registerHit();
                    if (ship.isSunk()) {
                        sunk = true;
                        sunkName = ship.getName();
                        break;
                    }
                }
            }

            if (hitQueueSize < hitQueue.length) {
                hitQueue[hitQueueSize][0] = row;
                hitQueue[hitQueueSize][1] = col;
                hitQueueSize++;
            }
        } else {
            playerBoard[row][col] = 'O';
        }

        view.updateBoard(playerBoard, view.getPlayerButtons());
        view.displayOpponentResult(row, col, hit);

        if (sunk) { // Band-Aid fix to display ship sunk message after hit message
            view.displayMessage("The opponent sunk your " + sunkName + "!");
        }

        if (checkWin(playerBoard)) {
            view.displayGameOver("Opponent");
            view.displayMessage("The opponent won the game in " + opponentTries + " turns!");
            disableOpponentButtons();
        } else {
            phase = GamePhase.PLAYER_TURN;
            view.updateGamePhase("Player Turn");
        }
    }


    private boolean checkWin(char[][] board) {
        for (int i = 0; i < BModel.boardSize; i++) {
            for (int j = 0; j < BModel.boardSize; j++) {
                if (board[i][j] == 'S') {
                    return false;
                }
            }
        }
        phase = GamePhase.GAME_OVER;
        view.updateGamePhase("Game Over!");
        return true;
    }

    private void disableOpponentButtons() {
        JButton[][] opponentButtons = view.getOpponentButtons();
        for (int i = 0; i < BModel.boardSize; i++) {
            for (int j = 0; j < BModel.boardSize; j++) {
                opponentButtons[i][j].setEnabled(false);
            }
        }
    }

}
