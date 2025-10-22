package go;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Core game mechanics for a simple Go implementation.
 * <p>
 * Board representation: {@code String[][]} with {@code "●"} (black), {@code "○"} (white), and {@code null} for empty.
 * Coordinates are treated as {@code (row, col)}.
 * <p>
 * This class contains only package-private static helpers—intended for use by the CLI/app layer.
 *
 * @implNote Ko is enforced by comparing the post-move board to {@code prevPrevBoard}.
 */
public class GameLogic {

    // ------------ Board Manipulation ------------
    
    static void resetBoard(String[][] board) {
        System.out.println("Resetting board to initial state...");
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = null;
            }
        }
        System.out.println("Board reset complete.");
    }

    // Return true if (r, c) is within board bounds
    private static boolean inBounds(String[][] board, int r, int c) {
        return r >= 0 && r < board.length && c >= 0 && c < App.board[0].length;
    }

    // Return color at position
    private static String colorAt(String[][] board, Position pos) {
        return board[pos.row()][pos.col()];
    }

    private static boolean boardEquals(String[][] a, String[][] b) {
        return Arrays.deepEquals(a, b);
    }

    private static List<Position> getNeighbors(String[][] board, Position piece) {
        List<Position> neighbors = new ArrayList<>(4);

        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] direction : directions) {
            int r = piece.row() + direction[0];
            int c = piece.col() + direction[1];
            if (inBounds(board, r, c)) {
                neighbors.add(new Position(r, c));
            }
        }

        return neighbors;     
    }

    /**
     * Prints the board to stdout with coordinate labels.
     *
     * @param board board state
     */
    static void printBoard(String[][] board) {
        // print board coordinates for any size board
        System.out.print(" ");
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

    /**
     * Deep-copies the board.
     *
     * @param board board state
     * @return new 2D array with identical contents
     */
    static String[][] copyBoard(String[][] board) {
        String[][] copy = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                copy[i][j] = board[i][j];
            }
        }
        return copy;
    }

    /**
     * Places a stone at {@code pos} for the current player.
     *
     * @param board   board state
     * @param pos     target position
     * @param player1 true for black ("●"), false for white ("○")
     */
    static void placePiece(String[][] board, Position pos, boolean player1) {
        if (player1 == true) {
            board[pos.row()][pos.col()] = "●";
        } else {
            board[pos.row()][pos.col()] = "○";
        }
    }

    // ------------ Prompt User Input ------------

    /**
     * Prompts for a move or pass from stdin.
     * <p>
     * Input formats:
     * <ul>
     *   <li>{@code "p"} to pass</li>
     *   <li>{@code "x y"} as integers (row col)</li>
     * </ul>
     * Returns {@code (-1, -1)} for pass.
     *
     * @param scn     scanner (stdin)
     * @param player1 whose turn message (black if true)
     * @return selected position or {@code (-1,-1)} for pass
     */
    static Position promptUser(Scanner scn, boolean player1) {
        System.out.println(player1 ? "Black's turn" : "White's turn");
        System.out.println("Enter X and Y coordinates separated by space, or 'p' for pass");
        String input = scn.nextLine().trim();

        while (input.isEmpty()) {
            input = scn.nextLine().trim();
        }

        String[] coords = input.toLowerCase().split("\\s+");
        if (coords.length == 1 && coords[0].equals("p")) {
            return new Position(-1,-1);
        }

        if (coords.length != 2) {
            System.out.println("Invalid input, try again");
            return promptUser(scn, player1);
        }

        try {
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            return new Position(x, y);
        } catch (NumberFormatException e) {
            System.out.println("Coordinates must be integers");
            return promptUser(scn, player1);
        }
    }

    // ------------ Remove Pieces ------------

    private static void removePiece(String[][] board, Position piece) {
        board[piece.row()][piece.col()] = null;
    }

    private static void removeGroup(String[][] board, Set<Position> group) {
        for (Position member: group) {
            removePiece(board, member);
        }
    }
   
    // ------------ Enforce Game Rules ------------

    // Ko check: verifies that placing color at position would not illegally recreate previous board state.
    private static boolean passKoRule(String[][] board, Position pos, String color, String[][] prevPrevBoard) {
        // temporarily place piece
        board[pos.row()][pos.col()] = color;
        String[][] tempBoard = copyBoard(board);
        boolean passes = !boardEquals(tempBoard, prevPrevBoard);
        // restore board state
        board[pos.row()][pos.col()] = null;
        return passes;
    }

    // Depth-first search to determine if connected group has at least one liberty.
    private static boolean hasLiberty(String[][] board, Position piece, String color, Set<Position> visited) {
        if (visited.contains(piece)) {
            return false;
        }

        visited.add(piece);

        List<Position> neighbors = getNeighbors(board, piece);
        boolean groupHasLiberty = false;
        for (Position neighbor : neighbors) {
            String c = colorAt(board, neighbor);
            if (c == null) {
                groupHasLiberty = true;
            } else if (c.equals(color)) {
                if (hasLiberty(board, neighbor, color, visited)) {
                    groupHasLiberty = true;
                }
            }
        }
        return groupHasLiberty;
    }

    // Check if piece would be alive if placed at Position start.
    private static boolean isAlive(String[][] board, Position start, String color) {
        String prev = board[start.row()][start.col()]; // before temporarily placing piece, should be null
        // temporarily place piece
        board[start.row()][start.col()] = color;
        try {
            Set<Position> visited = new HashSet<>();
            boolean alive = hasLiberty(board, start, color, visited);
            return alive;
        } finally {
            // replace piece back to null
            board[start.row()][start.col()] = prev;
        }
    }

    // ------------ Search and Capture ------------

    static void incrementCaptured(Map<String, Integer> capturedPieces, int n, String color) {
        int numCaptured = capturedPieces.get(color) + n;
        capturedPieces.put(color, numCaptured);

    }

    /**
     * Tests adjacent enemy groups for capture if {@code startColor} were placed at {@code start}.
     * <p>
     * Temporarily places the stone, searches neighbors for enemy groups with no liberties,
     * removes such groups, and updates capture counts.
     *
     * @param board          board state (temporarily mutated)
     * @param capturedPieces map from color to captured count
     * @param start          hypothetical placement
     * @param startColor     "●" or "○"
     * @return true if at least one enemy group would be captured
     */
    static boolean searchAndCapture(String[][] board, Map<String, Integer> capturedPieces, Position start, String startColor) {
        //temporarily place piece
        board[start.row()][start.col()] = startColor;
        boolean canCapture = false;
        // get neighbors of start piece
        List<Position> neighbors = getNeighbors(board, start);
        for (Position neighbor: neighbors) {
            String neighborColor = colorAt(board, neighbor);
            // String startColor = colorAt(start);

            if (neighborColor != null && !neighborColor.equals(startColor)) {
                Set<Position> group = new HashSet<>();
                if (!hasLiberty(board, neighbor, neighborColor, group)) {
                    incrementCaptured(capturedPieces, group.size(), neighborColor);
                    removeGroup(board, group);
                    canCapture = true;
                }
            }
        }
        board[start.row()][start.col()] = null;
        return canCapture;
    }

    // ------------ Score Empty Region ------------

    /**
     * Scores a contiguous empty region for territory using simple border-color logic:
     * if bordered only by black (and/or edges), credit black; if only by white, credit white.
     *
     * @param board       board state
     * @param start       starting empty position
     * @param territory   map of territory counts keyed by "●"/"○"
     * @param beenVisited visitation grid for empty-region DFS
     */
    static void scoreEmptyRegion(String[][] board, Position start, Map<String, Integer> territory, boolean[][] beenVisited) {
        // if position is not null or has been visited, return
        if (colorAt(board, start) != null || beenVisited[start.row()][start.col()] == true) return;

        // Initialize set for borderColor and list for region
        Set<String> borderColors = new HashSet<>();
        List<Position> region = new ArrayList<>();

        dfsEmpty(board, start, beenVisited, borderColors, region);

        // if borderColors is only black and null values, add region to black's territory
        if (!borderColors.contains("○")) {
            int newTerritory = territory.get("●") + region.size();
            territory.put("●", newTerritory);
        // if borderColors is only white and null values, add region to white's territory
        } else if (!borderColors.contains("●")) {
            int newTerritory = territory.get("○") + region.size();
            territory.put("○", newTerritory);
        }

    }

    // DFS over empty spaces, collects empty positions, and records bordering stone colors.
    static void dfsEmpty(String[][] board, Position pos, boolean[][] beenVisited, Set<String> borderColors, List<Position> region) {
        beenVisited[pos.row()][pos.col()] = true;
        region.add(pos);

        for (Position p : getNeighbors(board, pos)) {
            String color = colorAt(board, p);
            if (color == null && !beenVisited[p.row()][p.col()]) {
                dfsEmpty(board, p, beenVisited, borderColors, region);
            } else {
                borderColors.add(color);
            }
        }
    }

    // ------------  Play move ------------

    /**
     * Validates whether placing {@code color} at {@code selectedPosition} is legal under
     * simple suicide/ko checks and capture rules.
     * <p>
     * Logic:
     * <ol>
     *   <li>If the hypothetical stone/group is alive, require ko to pass.</li>
     *   <li>Else, if it captures neighbors, require ko to pass.</li>
     *   <li>Otherwise reject (suicide).</li>
     * </ol>
     *
     * @param board            current board
     * @param prevBoard        previous board (unused here but kept for context)
     * @param prevPrevBoard    board two turns ago (for ko)
     * @param player1          current player (unused here; provided by caller context)
     * @param captured         capture counters map (updated when captures occur)
     * @param selectedPosition target position
     * @param color            "●" or "○"
     * @return true if the move is allowed by these checks
     */
    static boolean playMove(String[][] board, String[][] prevBoard, String[][] prevPrevBoard, boolean player1, Map<String, Integer> captured, Position selectedPosition, String color) {
        if (isAlive(board, selectedPosition, color)) {
            if (passKoRule(board, selectedPosition, color, prevPrevBoard)) {
                return true;
            } else {
                System.out.println("Illegal move, violates koh rule. Try again.");
                return false;
            }
        
        } else {
            if (searchAndCapture(board, captured, selectedPosition, color)) {
                if (passKoRule(board, selectedPosition, color, prevPrevBoard)) {
                    // should remove group here?
                    return true;
                } else {
                    System.out.println("Illegal move, violates koh rule. Try again.");
                    return false;
                }
            } else {
                System.out.println("Illegal move, immediate capture. Try again.");
                return false;
            }
        } 
    }


    /**
     * Optional cleanup after play: lets users manually remove dead stones and updates capture counts.
     *
     * @param board    board state (mutated)
     * @param captured capture counters (mutated)
     * @param scn      scanner for user input
     */
    static void removeDeadPieces(String[][] board, Map<String, Integer> captured, Scanner scn) {
        boolean removing = true;
        while (removing) {
            System.out.println("Do you want to manually remove dead stones? (y/n)");
            String shouldRemove = scn.nextLine().trim().toLowerCase();
            if (shouldRemove.equals("y") || shouldRemove.equals("yes")) {
                System.out.println("Entered X and Y coordinate, separated by space, of piece to remove:");
                
                String input = scn.nextLine().trim();
                while (input.isEmpty()) {
                    input = scn.nextLine().trim();
                }
        
                String[] coords = input.toLowerCase().split("\\s+");
        
                if (coords.length != 2) {
                    System.out.println("Invalid input, try again");
                    removeDeadPieces(board, captured, scn);
                }

                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    Position selectedPosition = new Position(x, y);

                    incrementCaptured(captured, 1, colorAt(board, selectedPosition));
                    removePiece(board, selectedPosition);
                } catch (NumberFormatException e) {
                    System.out.println("Coordinates must be integers");
                    removeDeadPieces(board, captured, scn);
                }

            } else if (shouldRemove.equals("n") || shouldRemove.equals("no")) {
                removing = false;
            } else {
                System.out.println("Please enter 'y' or 'n'");
            }
            printBoard(board);
        }
    }
}
