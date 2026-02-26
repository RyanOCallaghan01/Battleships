import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Battleships {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showMainMenu());
    }

    private static void showMainMenu() {
        JFrame menuFrame = new JFrame("Battleships - Main Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(800, 500);
        menuFrame.setLayout(new GridLayout(3, 1));

        JButton newGameButton = new JButton("New Game");
        JButton loadGameButton = new JButton("Load From File");
        JButton exitButton = new JButton("Exit");

        newGameButton.addActionListener(e -> {
            menuFrame.dispose();
            launchNewGame(null);
        });

        loadGameButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(menuFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                    String[] lines = reader.lines().toArray(String[]::new);
                    menuFrame.dispose();
                    launchNewGame(lines);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(menuFrame, "Error loading file: " + ex.getMessage());
                }
            }
        });

        exitButton.addActionListener((ActionEvent e) -> System.exit(0));

        menuFrame.add(newGameButton);
        menuFrame.add(loadGameButton);
        menuFrame.add(exitButton);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

    private static void launchNewGame(String[] shipLines) {
        BModel model = new BModel();
        BView view = new BView();
        BController controller = new BController(model, view);

        if (shipLines != null) {
            model.initialiseBoard();
            Ship[] playerShips = model.getPlayerShips();
            Ship[] opponentShips = model.getOpponentShips();
            boolean loadingError = false;

            for (String line : shipLines) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length != 5) { // Expecting an extra identifier (PLAYER/OPPONENT)
                    JOptionPane.showMessageDialog(view, "Invalid format in file: " + line + "\nExpected: PLAYER/OPPONENT ShipName row col orientation", "Loading Error", JOptionPane.ERROR_MESSAGE);
                    loadingError = true;
                    break;
                }

                String boardOwner = parts[0].toUpperCase();
                String name = parts[1];
                try {
                    int row = Integer.parseInt(parts[2]);
                    int col = Integer.parseInt(parts[3]);
                    char orientation = parts[4].toUpperCase().charAt(0);

                    boolean shipPlaced = false;
                    Ship[] targetShips = null;
                    char[][] targetBoard = null;

                    if (boardOwner.equals("PLAYER")) {
                        targetShips = playerShips;
                        targetBoard = model.getPlayerBoard();
                    } else if (boardOwner.equals("OPPONENT")) {
                        targetShips = opponentShips;
                        targetBoard = model.getOpponentBoard();
                    } else {
                        JOptionPane.showMessageDialog(view, "Invalid board owner in file: " + boardOwner + ". Expected PLAYER or OPPONENT.", "Loading Error", JOptionPane.ERROR_MESSAGE);
                        loadingError = true;
                        break;
                    }

                    if (!loadingError && targetShips != null && targetBoard != null) {
                        for (Ship ship : targetShips) {
                            if (ship.getName().equalsIgnoreCase(name)) {
                                if (model.placeShip(targetBoard, ship, row, col, orientation)) {
                                    shipPlaced = true;
                                    break;
                                } else {
                                    JOptionPane.showMessageDialog(view, "Error placing " + boardOwner.toLowerCase() + " ship '" + name + "' from file at " + row + "," + col + " (" + orientation + "). Check for overlaps or out-of-bounds placement.", "Loading Error", JOptionPane.ERROR_MESSAGE);
                                    loadingError = true;
                                    break;
                                }
                            }
                        }
                        if (!shipPlaced && !loadingError) {
                            JOptionPane.showMessageDialog(view, "Unknown ship name in file for " + boardOwner + ": " + name, "Loading Error", JOptionPane.ERROR_MESSAGE);
                            loadingError = true;
                            break;
                        }
                    }

                    if (loadingError) {
                        break;
                    }

                } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                    JOptionPane.showMessageDialog(view, "Invalid format in file: " + line + "\nRow and column must be numbers, orientation a single character.", "Loading Error", JOptionPane.ERROR_MESSAGE);
                    loadingError = true;
                    break;
                }
            }

            if (!loadingError) {
                view.updateBoard(model.getPlayerBoard(), view.getPlayerButtons());
                view.setPlayerButtonsEnabled(false);
                view.getToggleOrientationButton().setVisible(false);

                view.updateBoard(model.getOpponentBoardHidden(), view.getOpponentButtons());

                controller.startLoadedGame();
            } else {
                view.dispose();
                showMainMenu();
            }
        } else {
            controller.startGame();
        }
    }
}

