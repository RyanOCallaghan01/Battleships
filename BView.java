import javax.swing.*;
import java.awt.*;

public class BView extends JFrame {

    private static final int boardSize = BModel.boardSize;
    private final JButton[][] playerButtons;
    private final JButton[][] opponentButtons;
    private final JButton toggleOrientationButton;
    private final JTextArea messageArea;
    private final JLabel gamePhaseLabel;

    public BView() {
        setTitle("Battleships");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(1, 2));
        JPanel playerPanel = new JPanel(new GridLayout(boardSize, boardSize));
        JPanel opponentPanel = new JPanel(new GridLayout(boardSize, boardSize));

        playerPanel.setBorder(BorderFactory.createTitledBorder("Your Board"));
        opponentPanel.setBorder(BorderFactory.createTitledBorder("Opponent's Board"));
        playerButtons = new JButton[boardSize][boardSize];
        opponentButtons = new JButton[boardSize][boardSize];

        toggleOrientationButton = new JButton("Orientation: Horizontal");

        gamePhaseLabel = new JLabel("Phase: Ship Placement");
        gamePhaseLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        gamePhaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(gamePhaseLabel, BorderLayout.NORTH);

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                playerButtons[i][j] = new JButton("-");
                playerPanel.add(playerButtons[i][j]);
            }
        }
        boardPanel.add(playerPanel);

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                opponentButtons[i][j] = new JButton("-");
                opponentPanel.add(opponentButtons[i][j]);
            }
        }
        boardPanel.add(opponentPanel);
        add(boardPanel, BorderLayout.CENTER);

        messageArea = new JTextArea(10, 40);
        messageArea.setEditable(false);

        Font fancyFont = new Font("Dialog", Font.PLAIN, 16);
        add(new JScrollPane(messageArea), BorderLayout.SOUTH);
        messageArea.setFont(fancyFont);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(toggleOrientationButton, BorderLayout.WEST);
        controlPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1280, 800));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public JButton[][] getPlayerButtons() {
        return playerButtons;
    }

    public JButton[][] getOpponentButtons() {
        return opponentButtons;
    }

    public JButton getToggleOrientationButton() {
        return toggleOrientationButton;
    }


    public void updateBoard(char[][] board, JButton[][] buttons) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                char state = board[i][j];
                switch (state) {
                    case 'S':
                        buttons[i][j].setBackground(Color.LIGHT_GRAY); // Show ship for player board
                        buttons[i][j].setText("");
                        break;
                    case 'X':
                        buttons[i][j].setBackground(Color.RED); // Hit
                        buttons[i][j].setText("X");
                        break;
                    case 'O':
                        buttons[i][j].setBackground(Color.WHITE); // Miss
                        buttons[i][j].setText("O");
                        break;
                    default:
                        buttons[i][j].setBackground(null);
                        buttons[i][j].setText("-");
                        break;
                }
            }
        }
    }

    public void setPlayerButtonsEnabled(boolean enabled) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                playerButtons[i][j].setEnabled(enabled);
            }
        }
    }

    public void setOpponentButtonsEnabled(boolean enabled) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                opponentButtons[i][j].setEnabled(enabled);
            }
        }
    }

    public void updateGamePhase(String phaseText) {
        gamePhaseLabel.setText("Phase: " + phaseText);
    }

    public void displayMessage(String message) {
        messageArea.append(message + "\n");
    }

    public void displayResult(int row, int col, boolean hit) {
        if (hit) {
            displayMessage("You hit an opposing ship at " + row + "," + col + "!");
        } else {
            displayMessage("You missed at " + row + "," + col + "!");
        }
    }

    public void displayOpponentResult(int row, int col, boolean hit) {
        if (hit) {
            displayMessage("The opponent hit your ship at " + row + "," + col + "!");
        } else {
            displayMessage("The opponent missed at " + row + "," + col + "!");
        }
    }

    public void displayGameOver(String winner) {
        displayMessage("Game Over! The winner is: " + winner);
    }

}