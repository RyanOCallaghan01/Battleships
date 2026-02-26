import java.util.Observer;

public interface iBModel {

    int boardSize = 10;
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(Object arg);
    char[][] getPlayerBoard();
    char[][] getOpponentBoard();
    Ship[] getPlayerShips();
    char[][] getOpponentBoardHidden();
    void initialiseBoard();
    void placeShips();
    boolean placeShip(char[][] board, Ship ship, int startRow, int startCol, char orientation);

}
