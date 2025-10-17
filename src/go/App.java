package go;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class App {

    static String[][] goBoard = new String[9][9];

    static boolean[][] lives = new boolean[9][9];

    static boolean[][] territory = new boolean[9][9];


    //populated board for testing
    static String[][] board = 
    {
        {null, null, "●", null, "●", null, null, null, null},
        {null, null, "●", "○", "○", "●", null, null, null},
        {null, null, "●", "○", null, "○", null, null, null},
        {null, null, null, "●", "○", null, null, null, null},
        {null, null, null, null, null, null, null, null, null},
        {"○", "○","○", "●", "●", "○", "●", "●", "●"},
        {"○", null, null, "○", "○", "○", "○", "○", "●"},
        {"○", null,"○", "○", null, "○", "○", null, "○"},
        {null, "○", null, null, null, null, null, null, null},

    };

    

    
    public static void main(String[] args) throws Exception {

        boolean player1 = true;
        boolean playing = true;

        GameLogic.printBoard(board);

        // Initialize prevBoard states
        String[][] prevBoard = new String[board.length][board[0].length];
        String[][] prevPrevBoard = new String[board.length][board[0].length];

        // Initialize Map to track captured pieces 
        Map<String, Integer> captured = new HashMap<>();
        captured.put("○", 0);
        captured.put("●", 0); 

        // Initialize boolean array to track visited spaces
        boolean[][] beenVisited = new boolean[9][9];

        int numPasses = 0;

        Scanner scn = new Scanner(System.in);

        

        // // ---- TEST ----
        // Position pos = new Position(2, 4);
        // Set<Position> groupMembers = new HashSet<>();
        // // System.out.println("pos.row: " + pos.row);
        // // System.out.println(isAlive(pos, color));
        // // --------------
        // // "○" "●"
        // String color = "●";
        // boolean canCapture = searchAndCapture(pos, color);
        // System.out.println("CANCAPTURE: " + canCapture);

        // printBoard(board);

        // System.out.println("group members:");
        // for (Position member: groupMembers) {
        //     System.out.println(member);
        // }

        while (playing) {
            // Map<String, Integer> captured = new HashMap<>();
            // captured.put("●", 0);
            // captured.put("○", 0);

            prevPrevBoard = GameLogic.copyBoard(prevBoard);
            prevBoard = GameLogic.copyBoard(board);
            // in go, player 1 is black
            String color = player1 ? "●" : "○"; 
            int x, y;
            System.out.println((player1 ? "Player 1's" : "Player 2's") + " turn");

            // PROMPT USER INPUT
            // System.out.println("Please enter X coord:");
            // x = scn.nextInt();
            // System.out.println("Please enter Y coord:");
            // y = scn.nextInt();

            // Position selectedPostion = new Position(x, y);
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

            System.out.println(GameLogic.isAlive(board, selectedPosition, color));


            if (board[selectedPosition.row()][selectedPosition.col()] != null) {
                System.out.println("This space is occupied. Try again");
                continue;
            }

            boolean legalMove = GameLogic.playMove(board, prevPrevBoard, player1, captured, selectedPosition, color);

            if (legalMove) {
                numPasses = 0;
            } else {
                // if move was not legal, try again
                continue;

            }
            

            // System.out.println("captured pieces: " + captured.get("○"));
            GameLogic.printBoard(board);
            // System.out.println("prevBoard:");
            // GameLogic.printBoard(prevBoard);
            // System.out.println("prevPrevBoard");
            // GameLogic.printBoard(prevPrevBoard);
            // System.out.println();

        }

        // scoring

        System.out.println("Game over, time to score!");
        GameLogic.removeDeadPieces(board, captured, scn);

        
    }
}
