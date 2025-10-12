import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class App {
    // records are value based classes. 
    public record Position(int row, int col) {}

    static String[][] goBoard = new String[9][9];

    static boolean[][] lives = new boolean[9][9];
    static boolean[][] territory = new boolean[9][9];
    static boolean[][] beenChecked = new boolean[9][9];

    //populated board for testing
    static String[][] board = 
    {
        {null, null,"●", "●", "○", null, null, "○", null},
        {null, "●", null, "●", "○", "●", "○", null, "○"},
        {null, "●", null, "●", "○", "●", null, "○", null},
        {"●", "●","●", "○", "○", "●", null, null, null},
        {"●", "○", "○", "○", "●", "●", "●", null, null},
        {"○", "○","○", "●", "●", "○", "●", "●", "●"},
        {"○", null, null, "○", "○", "○", "○", "○", "●"},
        {"○", null,"○", "○", null, "○", "○", null, "○"},
        {null, "○", null, null, null, null, null, null, null},

    };
    
    // Method to access the board from GUI
    public String[][] getBoard() {
        return board;
    }

    private static boolean inBounds(int r, int c) {
        return r >= 0 && r < board.length && c >= 0 && c < board[0].length;
    }

    private static String colorAt(Position pos) {
        System.out.println();
        System.out.println("colorAt called");
        System.out.println();
        return board[pos.row][pos.col];
    }

    private static List<Position> getNeighbors(Position piece) {
        System.out.println("getNeighbors called on position: " + piece.row + ", " + piece.col);
        List<Position> neighbors = new ArrayList<>(4);

        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] direction : directions) {
            int r = piece.row + direction[0];
            int c = piece.col + direction[1];
            if (inBounds(r, c)) {
                neighbors.add(new Position(r, c));
            }
        }

        System.out.println("neighbors list:");
        for (Position n : neighbors) {
            System.out.println(n.row + ", " + n.col);
        }

        return neighbors;     
    }

    // overloaded isAlive to accept color as argument to check unoccupied space
    static boolean isAlive(Position start, String color) {
        System.out.println("isAlive called on position: " + start.row + ", " + start.col + ", for color " + color);

        // initialize empty set of positions for has been checked
        Set<Position> visited = new HashSet<Position>();
        boolean alive = hasLiberty(start, color, visited);
        System.out.println("in isAlive, returning: " + alive);
        return alive;
    }

    // isAlive for checking occupied space
    static boolean isAlive(Position start) {
        System.out.println("isAlive called on position: " + start.row + ", " + start.col);
        // if space is empty, throw return false
        if (colorAt(start) == null) {
            System.out.println("returning false");
            return false;
        }

        // initialize empty set of positions for has been checked
        Set<Position> visited = new HashSet<Position>();
        return hasLiberty(start, colorAt(start), visited);
    }

    static boolean hasLiberty(Position piece, String color, Set<Position> visited) {
        System.out.println("hasLiberty called");
        if (visited.contains(piece)) {
            System.out.println("base case hit");
            return false;
        }

        visited.add(piece);

        List<Position> neighbors = getNeighbors(piece);

        for (Position neighbor : neighbors) {
            String c = colorAt(neighbor);
            if (c == null) {
                return true; // found a liberty
            } else if (c == color) {
                if (hasLiberty(neighbor, color, visited) == true) {
                    return true;
                }
            }
        }
        return false;
    }


    static void printBoard(String[][] board) {
        // print board coordinates for any size board
        for (int i = 0; i < board.length; i++) {
            System.out.print(" " + i);
        }
        System.out.println();


        for (int i = 0; i < board.length; i++){
            System.out.print(i);
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null) {
                    System.out.print(" +");
                }
                else {
                    System.out.print(" " + board[i][j]);
                }
            }
            System.out.println();
        }
    }

    static void placePiece(int x, int y, Boolean player1) {
        if (player1 == true) {
            board[x][y] = "○";
        } else {
            board[x][y] = "●";
        }
    }

    public static void main(String[] args) throws Exception {

        boolean player1 = true;
        String color = player1 ? "○" : "●";
        

        printBoard(board);

        // ---- TEST ----
        // Position pos = new Position(1, 7);
        // System.out.println("pos.row: " + pos.row);
        // System.out.println(isAlive(pos, color));
        // --------------

        while (true) {
            Scanner scn = new Scanner(System.in);
            int x, y;

            System.out.println((player1 ? "Player 1's" : "Player 2's") + " turn");

            System.out.println("Please enter X coord:");
            x = scn.nextInt();
            System.out.println("Please enter Y coord:");
            y = scn.nextInt();

            Position selectedPostion = new Position(x, y);

            System.out.println("ISALIVEEEEE");
            System.out.println(isAlive(selectedPostion, color));


            if ((board[x][y] == null) && isAlive(selectedPostion, color)) {
                placePiece(x, y, player1);
                player1 = !player1;
            } else if (board[x][y] != null) {
                System.out.println("This space is occupied. Try again");
            }

            printBoard(board);
            System.out.println();
        }

        






        // Scanner scn = new Scanner(System.in);
        // boolean player1 =  true;
        // int x, y;

        // printBoard(board);

        // while (true) {
        //     System.out.println((player1 ? "Player 1's" : "Player 2's") + " turn");

        //     System.out.println("Please enter X coord:");
        //     x = scn.nextInt();
        //     System.out.println("Please enter Y coord:");
        //     y = scn.nextInt();

        //     if (board[x][y] == null) {
        //         placePiece(x, y, player1);
        //         player1 = !player1;
        //     } else {
        //         System.out.println("This space is occupied. Try again");
        //         continue;
        //     }

        //     printBoard(board);
        //     System.out.println();
        // }
    }
}
