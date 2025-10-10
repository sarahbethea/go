import java.util.Scanner;

public class App {
    static String[][] goBoard = new String[9][9];

    static void printBoard() {
        // print board coordinates for any size board
        for (int i = 0; i < goBoard.length; i++) {
            System.out.print(" " + i);
        }
        System.out.println();


        for (int i = 0; i < goBoard.length; i++){
            System.out.print(i);
            for (int j = 0; j < goBoard[i].length; j++) {
                if (goBoard[i][j] == null) {
                    System.out.print(" +");
                }
                else {
                    System.out.print(" " + goBoard[i][j]);
                }
            }
            System.out.println();
        }
    }

    static void placePiece(int x, int y, Boolean player1) {
        if (player1 == true) {
            goBoard[x][y] = "○";
        } else {
            goBoard[x][y] = "●";
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scn = new Scanner(System.in);
        boolean player1 =  true;
        int x, y;

        static String[][] goBoard = new String[9][9];
        static boolean[][] lives = new boolean[9][9]; 

        printBoard();

        while (true) {
            System.out.println((player1 ? "Player 1's" : "Player 2's") + " turn");

            System.out.println("Please enter X coord:");
            x = scn.nextInt();
            System.out.println("Please enter Y coord:");
            y = scn.nextInt();

            if (goBoard[x][y] == null) {
                placePiece(x, y, player1);
                player1 = !player1;
            } else {
                System.out.println("This space is occupied. Try again");
                continue;
            }

            printBoard();
            System.out.println();
        }
    }
}
