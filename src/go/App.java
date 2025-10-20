package go;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class App {
    static String[][] board = new String[9][9];

    //populated board for testing
    static String[][] testBoard = 
    {
        {"○", "○", "○", null, null, null, null, null, null},
        {"○", "●", "●", null, null, null, null, null, null},
        {"○", "●", "●", "○", null , "○", "○", "○", "○"},
        {"○", "●", "●", "○", null, null, null, null, null},
        {"○", "○", "○", "○", null, null, null, null, null},
        {null, null, null, null, null, null, "○", "●", null},
        {"●", "●", "●", "●", null, "○", "●", null, "●"},
        {null, null, null, "●", null, null, "○", "●", null},
        {null, null, null, "●", null, null, null, null, null},

    };
    
    public static void main(String[] args) throws Exception {
        boolean player1 = true;
        boolean playing = true;

        // Initialize prevBoard states
        String[][] prevBoard = new String[board.length][board[0].length];
        String[][] prevPrevBoard = new String[board.length][board[0].length];

        // Initialize map to track captured pieces 
        Map<String, Integer> captured = new HashMap<>();
        captured.put("●", 0); 
        captured.put("○", 0);
        

        // Initialize map to track territory
        Map<String, Integer> territory = new HashMap<>();
        territory.put("●", 0);
        territory.put("○", 0);

        // Initialize boolean array to track visited spaces
        boolean[][] beenVisited = new boolean[9][9];

        int numPasses = 0;

        Scanner scn = new Scanner(System.in);


        while (playing) {
            GameLogic.printBoard(board);

            prevPrevBoard = GameLogic.copyBoard(prevBoard);
            prevBoard = GameLogic.copyBoard(board);
            // in go, player 1 is black
            String color = player1 ? "●" : "○"; 
            System.out.println((player1 ? "Player 1's" : "Player 2's") + " turn");

            Position selectedPosition = GameLogic.promptUser(scn, player1);

            // Handle player passing
            if (selectedPosition.row() == -1){
                numPasses += 1;
                if (numPasses >= 2) {
                    playing = false;
                    continue;
                }
                player1 = !player1;
                continue;
            }


            if (board[selectedPosition.row()][selectedPosition.col()] != null) {
                System.out.println("This space is occupied. Try again");
                continue;
            }

            boolean legalMove = GameLogic.playMove(board, prevBoard, prevPrevBoard, player1, captured, selectedPosition, color);

            if (legalMove) {
                GameLogic.searchAndCapture(board, captured, selectedPosition, color);
                GameLogic.placePiece(board, selectedPosition, player1);
                numPasses = 0;
                player1 = !player1;
            } else {
                // if move was not legal, try again. Ensure board state remains unchanged.
                board = GameLogic.copyBoard(prevBoard);
                continue;

            }
            
        }

        System.out.println("Game over, time to score!");

        // Optional: manually remove dead pieces
        GameLogic.removeDeadPieces(board, captured, scn);

        // Count territory
        for (int r =0; r < board.length; r++) {
            for (int c=0; c < board[0].length; c++) {
                GameLogic.scoreEmptyRegion(board, new Position(r, c), territory, beenVisited);
            }
        }

        // SUBTRACT CAPTURED!!!!!
        int capturedBlack = captured.get("●");
        int territoryBlack = territory.get("●");
        int totalBlack = territoryBlack - capturedBlack;
        System.out.println("Black's territory: " + territoryBlack);
        System.out.println("Captured black pieces: " + capturedBlack);
        System.out.println("Total territory: " + totalBlack);

        System.out.println();

        int capturedWhite = captured.get("○");
        int territoryWhite = territory.get("○");
        int totalWhite = territoryWhite - capturedWhite;
        System.out.println("Whites's territory: " + territoryWhite);
        System.out.println("Captured white pieces: " + capturedWhite);
        System.out.println("Total territory: " + totalWhite);

        System.out.println();

        if (totalBlack > totalWhite) {
            System.out.println("Black wins!");
        } else if (totalWhite > totalBlack) {
            System.out.println("White wins!");
        } else {
            System.out.println("It's a tie!");
        }

    }
}
