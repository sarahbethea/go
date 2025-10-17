package go;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GameLogic {
    // ------------ Board Manipulation -------------------

    // Method to access the board from GUI
    public static String[][] getBoard(String[][] board) {
        return board;
    }
    
    // Method to reset the board to initial state
    public static void resetBoard(String[][] board) {
        System.out.println("Resetting board to initial state...");
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = null;
            }
        }
        System.out.println("Board reset complete.");
    }

    private static boolean inBounds(String[][] board, int r, int c) {
        return r >= 0 && r < board.length && c >= 0 && c < App.board[0].length;
    }


    private static String colorAt(String[][] board, Position pos) {
        System.out.println();
        System.out.println("colorAt called");
        System.out.println();
        return board[pos.row()][pos.col()];
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

    static void placePiece(String[][] board, Position pos, boolean player1) {
        if (player1 == true) {
            board[pos.row()][pos.col()] = "●";
        } else {
            board[pos.row()][pos.col()] = "○";
        }
    }

    static String[][] copyBoard(String[][] board) {
        String[][] copy = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                copy[i][j] = board[i][j];
            }
        }
        return copy;
    }

    static boolean boardEquals(String[][] a, String[][] b) {
        return Arrays.deepEquals(a, b);
    }

    // ----------- Remove Pieces --------------

    static void removePiece(String[][] board, Position piece) {
        System.out.println("removePiece called");
        board[piece.row()][piece.col()] = null;
    }

    static void removeGroup(String[][] board, Set<Position> group) {
        System.out.println("removeGroup called");
        for (Position member: group) {
            System.out.println("member to remove: " + member.row() + ", " + member.col());
            removePiece(board, member);
        }
    }

   
    // ---------- Enforce Rules ----------


    static boolean passKoRule(String[][] board, Position pos, String color, String[][] prevPrevBoard) {
        // temporarily place piece
        board[pos.row()][pos.col()] = color;
        String[][] tempBoard = copyBoard(board);
        boolean isLegit = !boardEquals(tempBoard, prevPrevBoard);
        // restore board state
        board[pos.row()][pos.col()] = null;
        return isLegit;
    }

    static boolean hasLiberty(String[][] board, Position piece, String color, Set<Position> visited) {
        System.out.println("hasLiberty called on position " + piece.row() + ", " + piece.col());
        if (visited.contains(piece)) {
            System.out.println("base case hit");
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

    // overloaded isAlive to accept color as argument to check unoccupied space
    static boolean isAlive(String[][] board, Position start, String color) {
        System.out.println("isAlive called on position: " + start.row() + ", " + start.col() + ", for color " + color);

        String prev = board[start.row()][start.col()]; // before temporarily placing piece, should be null
        // temporarily place piece
        board[start.row()][start.col()] = color;
        try {
            Set<Position> visited = new HashSet<>();
            boolean alive = hasLiberty(board, start, color, visited);
            System.out.println("in isAlive, returning: " + alive);
            return alive;
        } finally {
            // replace piece back to null
            board[start.row()][start.col()] = prev;
        }
    }

    // isAlive for checking occupied space
    static boolean isAlive(String[][] board, Position start) {
        System.out.println("isAlive called on position: " + start.row() + ", " + start.col());
        // if space is empty, throw() return false
        if (board[start.row()][start.col()] == null) return false;

        // initialize empty set of positions for has been checked
        Set<Position> visited = new HashSet<Position>();
        return hasLiberty(board, start, colorAt(board, start), visited);
    }


    // ---------- Search and Capture ------------

    static void incrementCaptured(Map<String, Integer> capturedPieces, int n, String color) {
        int numCaptured = capturedPieces.get(color) + n;
        capturedPieces.put(color, numCaptured);

    }

    // if pieces to capture, remove them and return number of pieces captured, else return 0
    static boolean searchAndCapture(String[][] board, Map<String, Integer> capturedPieces, Position start, String startColor) {
        System.out.println("searchAndCapture start color: " + startColor);

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
                System.out.println("searchAndCapture calls hasLiberty on neighbor: " + neighbor.row() + ", " + neighbor.col());
                if (!hasLiberty(board, neighbor, neighborColor, group)) {
                    incrementCaptured(capturedPieces, group.size(), neighborColor);
                    System.out.println("group size in searchAndCapture: " + group.size());
                    removeGroup(board, group);
                    canCapture = true;
                }
            }

        }
        System.out.println("canCapture: " + canCapture);
        // if nothing to capture, reset start position to null, dont place piece
        if (!canCapture) board[start.row()][start.col()] = null;
        return canCapture;
    }

    // ------------ Score Empty Region -----------

    // make boolean array beenVisited and pass it in at scoring time
    // this will be called after dead pieces are removed

    static void scoreEmptyRegion(String[][] board, Position start, Map<String, Integer> territory, boolean[][] beenVisited) {
        // if position is not null or has been visited, return
        if (colorAt(board, start) != null || beenVisited[start.row()][start.col()] == true) return;

        // Initialize set for borderColor and list for region
        Set<String> borderColors = new HashSet<>();
        List<Position> region = new ArrayList<>();

        dfsEmpty(board, start, beenVisited, borderColors, region);

        if (borderColors.equals(Set.of("●"))) {
            int newTerritory = territory.get("●") + region.size();
            territory.put("●", newTerritory);
        } else if (borderColors.equals(Set.of("○"))) {
            int newTerritory = territory.get("○") + region.size();
            territory.put("○", newTerritory);
        }

    }

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


    // ------------ Get Neighbors -------

    private static List<Position> getNeighbors(String[][] board, Position piece) {
        System.out.println("getNeighbors called on position: " + piece.row() + ", " + piece.col());
        List<Position> neighbors = new ArrayList<>(4);

        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] direction : directions) {
            int r = piece.row() + direction[0];
            int c = piece.col() + direction[1];
            if (inBounds(board, r, c)) {
                neighbors.add(new Position(r, c));
            }
        }

        System.out.println("neighbors list:");
        for (Position n : neighbors) {
            System.out.println(n.row() + ", " + n.col());
        }

        return neighbors;     
    }


    
  






}
