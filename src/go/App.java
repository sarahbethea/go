package go;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class App {

    static String[][] goBoard = new String[9][9];

    static boolean[][] lives = new boolean[9][9];

    static boolean[][] territory = new boolean[9][9];


    static boolean[][] beenVisited = new boolean[9][9];

    //populated board for testing
    static String[][] board = 
    {
        {null, null, "●", "●", "●", null, null, null, null},
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

        GameLogic.printBoard(board);

        // Initialize prevBoard states
        String[][] prevBoard = new String[board.length][board[0].length];
        String[][] prevPrevBoard = new String[board.length][board[0].length];

        Map<String, Integer> captured = new HashMap<>();
        captured.put("○", 0);
        captured.put("●", 0); 
        

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

        while (true) {
            // Map<String, Integer> captured = new HashMap<>();
            // captured.put("●", 0);
            // captured.put("○", 0);

            prevPrevBoard = GameLogic.copyBoard(prevBoard);
            prevBoard = GameLogic.copyBoard(board);
            // in go, player 1 is black
            String color = player1 ? "●" : "○"; 

            Scanner scn = new Scanner(System.in);
            int x, y;
            System.out.println((player1 ? "Player 1's" : "Player 2's") + " turn");
            System.out.println("Please enter X coord:");
            x = scn.nextInt();
            System.out.println("Please enter Y coord:");
            y = scn.nextInt();

            Position selectedPostion = new Position(x, y);

            System.out.println(GameLogic.isAlive(board, selectedPostion, color));


            if (board[x][y] != null) {
                System.out.println("This space is occupied. Try again");
                continue;
            }

            if (GameLogic.isAlive(board, selectedPostion, color)) {
                System.out.println("isAlive for this position" + GameLogic.isAlive(board, selectedPostion, color));
                if (GameLogic.passKoRule(board, selectedPostion, color, prevPrevBoard)) {
                    GameLogic.placePiece(board, selectedPostion, player1);
                    player1 = !player1;
                }
            
            } else {
                if (GameLogic.searchAndCapture(board, captured, selectedPostion, color)) {
                    if (GameLogic.passKoRule(board, selectedPostion, color, prevPrevBoard)) {
                        System.out.println("searchAndCapture returns: " + GameLogic.searchAndCapture(board, captured, selectedPostion, color));
                        player1 = !player1;
                    }
                } 
            }

            

            System.out.println("captured pieces: " + captured.get("○"));
            GameLogic.printBoard(board);
            System.out.println("prevBoard:");
            GameLogic.printBoard(prevBoard);
            System.out.println("prevPrevBoard");
            GameLogic.printBoard(prevPrevBoard);
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
