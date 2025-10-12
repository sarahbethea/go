import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class GoBoardSwing extends JFrame {
    private static final int BOARD_SIZE = 9;
    private boolean player1Turn = true; // true = white (○), false = black (●)
    private JLabel currentPlayerLabel;
    private JLabel statusLabel;
    private GoBoardPanel boardPanel;
    private JTextArea debugConsole;
    private PrintStream originalOut;
    
    // Reference to the game logic from App.java
    private App gameLogic;
    
    public GoBoardSwing() {
        // Initialize game logic
        gameLogic = new App();
        
        // Set up the frame
        setTitle("Go Board Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 240, 240)); // Light gray background for the window
        
        // Create game info panel
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.setBackground(new Color(240, 240, 240)); // Match window background
        currentPlayerLabel = new JLabel("Current Player: White (○)");
        statusLabel = new JLabel("Click a position to place a stone");
        
        // Create player chooser buttons
        JButton whiteButton = new JButton("White (○)");
        JButton blackButton = new JButton("Black (●)");
        whiteButton.setBackground(Color.WHITE);
        whiteButton.setForeground(Color.BLACK);
        blackButton.setBackground(Color.BLACK);
        blackButton.setForeground(Color.WHITE);
        
        // Add action listeners for player chooser
        whiteButton.addActionListener(e -> {
            player1Turn = true;
            updatePlayerDisplay();
        });
        
        blackButton.addActionListener(e -> {
            player1Turn = false;
            updatePlayerDisplay();
        });
        
        infoPanel.add(currentPlayerLabel);
        infoPanel.add(new JLabel(" | "));
        infoPanel.add(statusLabel);
        infoPanel.add(new JLabel(" | "));
        infoPanel.add(new JLabel("Force Player: "));
        infoPanel.add(whiteButton);
        infoPanel.add(blackButton);
        
        // Create the board
        boardPanel = createBoard();
        
        // Create debug console
        debugConsole = createDebugConsole();
        
        // Create scroll pane for console
        JScrollPane consoleScrollPane = new JScrollPane(debugConsole);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Debug Console"));
        
        // Create command input
        JTextField commandInput = createCommandInput();
        
        // Create console panel with output and input
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(consoleScrollPane, BorderLayout.CENTER);
        consolePanel.add(commandInput, BorderLayout.SOUTH);
        consolePanel.setBorder(BorderFactory.createTitledBorder("Interactive Debug Console"));
        
        // Create split pane for board and console (horizontal split)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(boardPanel);
        splitPane.setRightComponent(consolePanel);
        splitPane.setDividerLocation(600); // Set initial divider position
        splitPane.setEnabled(true);
        
        // Add everything to frame
        add(infoPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        
        // Set size and show - wider to accommodate side-by-side layout
        setSize(1200, 700);
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
        
        // Setup console output redirection
        setupConsoleOutput();
    }
    
    private GoBoardPanel createBoard() {
        return new GoBoardPanel();
    }
    
    private JTextArea createDebugConsole() {
        JTextArea console = new JTextArea(20, 40); // More rows, good width
        console.setEditable(false);
        console.setBackground(new Color(30, 30, 30)); // Dark background
        console.setForeground(new Color(0, 255, 0)); // Green text
        console.setFont(new Font("Courier New", Font.PLAIN, 11));
        console.setLineWrap(true);
        console.setWrapStyleWord(true);
        
        // Ensure scrolling works properly
        console.setAutoscrolls(true);
        
        return console;
    }
    
    private JTextField createCommandInput() {
        JTextField input = new JTextField();
        input.setBackground(new Color(50, 50, 50)); // Slightly lighter than console
        input.setForeground(new Color(255, 255, 0)); // Yellow text for input
        input.setFont(new Font("Courier New", Font.PLAIN, 12));
        input.setCaretColor(new Color(255, 255, 0)); // Yellow cursor
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Command Input (Press Enter to execute)"),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Add placeholder text
        input.setToolTipText("Type commands here and press Enter. Try 'help' or 'methods'");
        
        // Add action listener for Enter key
        input.addActionListener(e -> {
            String command = input.getText().trim();
            if (!command.isEmpty()) {
                executeCommand(command);
                input.setText("");
            }
        });
        
        // Focus the input field when window opens
        input.setFocusable(true);
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
        
        return input;
    }
    
    private void setupConsoleOutput() {
        // Store original System.out
        originalOut = System.out;
        
        // Create a custom PrintStream that writes to both console and GUI
        PrintStream customOut = new PrintStream(new ByteArrayOutputStream()) {
            @Override
            public void println(String x) {
                // Write to original console
                originalOut.println(x);
                // Write to GUI console
                SwingUtilities.invokeLater(() -> {
                    debugConsole.append(x + "\n");
                    // Auto-scroll to bottom
                    debugConsole.setCaretPosition(debugConsole.getDocument().getLength());
                    // Ensure the console scrolls to show new content
                    debugConsole.scrollRectToVisible(debugConsole.getVisibleRect());
                });
            }
            
            @Override
            public void print(String x) {
                originalOut.print(x);
                SwingUtilities.invokeLater(() -> {
                    debugConsole.append(x);
                    // Auto-scroll to bottom
                    debugConsole.setCaretPosition(debugConsole.getDocument().getLength());
                    // Ensure the console scrolls to show new content
                    debugConsole.scrollRectToVisible(debugConsole.getVisibleRect());
                });
            }
        };
        
        // Redirect System.out to our custom stream
        System.setOut(customOut);
        
        // Add welcome message
        System.out.println("=== Interactive Go Board Console ===");
        System.out.println("Type commands in the input field below and press Enter");
        System.out.println("Type 'help' for available commands");
        System.out.println("Type 'methods' to see all testable methods");
        System.out.println("=====================================");
    }
    
    private void executeCommand(String command) {
        System.out.println("> " + command); // Show the command that was typed
        
        try {
            if (command.equals("help")) {
                showHelp();
            } else if (command.equals("methods")) {
                showMethods();
            } else if (command.equals("clear")) {
                debugConsole.setText("");
                System.out.println("Console cleared.");
            } else if (command.startsWith("isAlive(") && command.endsWith(")")) {
                // Parse: isAlive(row, col) or isAlive(row, col, color)
                String params = command.substring(8, command.length() - 1);
                String[] parts = params.split(",");
                if (parts.length == 2) {
                    // Original isAlive(row, col) - for occupied spaces
                    int row = Integer.parseInt(parts[0].trim());
                    int col = Integer.parseInt(parts[1].trim());
                    App.Position pos = new App.Position(row, col);
                    boolean result = App.isAlive(pos);
                    System.out.println("Result: " + result);
                } else if (parts.length == 3) {
                    // Overloaded isAlive(row, col, color) - for unoccupied spaces
                    int row = Integer.parseInt(parts[0].trim());
                    int col = Integer.parseInt(parts[1].trim());
                    String colorInput = parts[2].trim().replaceAll("\"", "").toLowerCase(); // Remove quotes and convert to lowercase
                    
                    // Convert "white"/"black" to App.java symbols
                    String color;
                    if (colorInput.equals("white") || colorInput.equals("w")) {
                        color = "○"; // White in App.java
                    } else if (colorInput.equals("black") || colorInput.equals("b")) {
                        color = "●"; // Black in App.java
                    } else {
                        System.out.println("Error: Color must be 'white', 'black', 'w', or 'b'");
                        return;
                    }
                    
                    App.Position pos = new App.Position(row, col);
                    boolean result = App.isAlive(pos, color);
                    System.out.println("Result: " + result);
                } else {
                    System.out.println("Error: isAlive requires 2 parameters (row, col) or 3 parameters (row, col, color)");
                }
            } else if (command.startsWith("placePiece(") && command.endsWith(")")) {
                // Parse: placePiece(row, col, isWhite)
                String params = command.substring(11, command.length() - 1);
                String[] parts = params.split(",");
                if (parts.length == 3) {
                    int row = Integer.parseInt(parts[0].trim());
                    int col = Integer.parseInt(parts[1].trim());
                    boolean isWhite = Boolean.parseBoolean(parts[2].trim());
                    // Convert GUI isWhite to App.java logic: isWhite=true → player1=false, isWhite=false → player1=true
                    boolean appPlayer1 = !isWhite;
                    gameLogic.placePiece(row, col, appPlayer1);
                    boardPanel.repaint();
                    System.out.println("Stone placed at (" + row + ", " + col + ") - " + (isWhite ? "White (○)" : "Black (●)"));
                } else {
                    System.out.println("Error: placePiece requires 3 parameters (row, col, isWhite)");
                }
            } else if (command.equals("printBoard")) {
                System.out.println("Current board state:");
                App.printBoard(gameLogic.getBoard());
            } else if (command.equals("reset")) {
                // Reset board to initial state
                App.resetBoard();
                boardPanel.repaint();
                System.out.println("Board reset to initial state.");
            } else if (command.equals("setWhite") || command.equals("white")) {
                player1Turn = true;
                updatePlayerDisplay();
                System.out.println("Player set to White (○)");
            } else if (command.equals("setBlack") || command.equals("black")) {
                player1Turn = false;
                updatePlayerDisplay();
                System.out.println("Player set to Black (●)");
            } else {
                System.out.println("Unknown command: " + command);
                System.out.println("Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        System.out.println("\n=== Available Commands ===");
        System.out.println("help                    - Show this help message");
        System.out.println("methods                 - List all testable game methods");
        System.out.println("clear                   - Clear the console");
        System.out.println("reset                   - Reset board to initial state");
        System.out.println("setWhite / white        - Set player to White (○)");
        System.out.println("setBlack / black        - Set player to Black (●)");
        System.out.println("\nGame Method Commands:");
        System.out.println("  isAlive(row, col)              - Test if existing stone is alive");
        System.out.println("  isAlive(row, col, color)       - Test if placing stone would be alive");
        System.out.println("  placePiece(row, col, isWhite)  - Place a stone");
        System.out.println("  printBoard()                   - Display board state");
        System.out.println("\nExamples:");
        System.out.println("  isAlive(4, 2)                  - Check existing stone");
        System.out.println("  isAlive(4, 4, \"white\")        - Check if white (○) stone would live");
        System.out.println("  isAlive(3, 3, \"black\")        - Check if black (●) stone would live");
        System.out.println("  placePiece(3, 5, true)         - Place white stone");
        System.out.println("  printBoard()                   - Show board");
        System.out.println("========================\n");
    }
    
    private void showMethods() {
        System.out.println("\n=== All Testable Game Methods ===");
        System.out.println();
        
        System.out.println(" BOARD STATE METHODS:");
        System.out.println("  printBoard()                   - Display current board state");
        System.out.println("  reset                          - Reset board to initial state");
        System.out.println();
        
        System.out.println(" STONE PLACEMENT:");
        System.out.println("  placePiece(row, col, isWhite)  - Place stone at position");
        System.out.println("    Parameters:");
        System.out.println("      row (0-8)     - Row coordinate");
        System.out.println("      col (0-8)     - Column coordinate");
        System.out.println("      isWhite       - true for white (○), false for black (●)");
        System.out.println("    Example: placePiece(4, 4, true)");
        System.out.println();
        
        System.out.println(" GAME LOGIC METHODS:");
        System.out.println("  isAlive(row, col)              - Check if existing stone has liberties");
        System.out.println("    Parameters:");
        System.out.println("      row (0-8)     - Row coordinate");
        System.out.println("      col (0-8)     - Column coordinate");
        System.out.println("    Returns: true if stone is alive, false if captured");
        System.out.println("    Example: isAlive(4, 2)");
        System.out.println();
        System.out.println("  isAlive(row, col, color)       - Check if placing stone would be alive");
        System.out.println("    Parameters:");
        System.out.println("      row (0-8)     - Row coordinate");
        System.out.println("      col (0-8)     - Column coordinate");
        System.out.println("      color         - \"white\" (○), \"black\" (●), \"w\", or \"b\"");
        System.out.println("    Returns: true if stone would be alive, false if suicide");
        System.out.println("    Example: isAlive(4, 4, \"white\")");
        System.out.println();
        
        System.out.println(" UTILITY COMMANDS:");
        System.out.println("  help                            - Show command help");
        System.out.println("  methods                         - Show this method list");
        System.out.println("  clear                           - Clear console output");
        System.out.println("  reset                           - Reset board to initial state");
        System.out.println();
        
        System.out.println(" PLAYER CONTROLS:");
        System.out.println("  setWhite / white                - Set current player to White (○)");
        System.out.println("  setBlack / black                - Set current player to Black (●)");
        System.out.println("    Note: Use these to place multiple stones of same color");
        System.out.println("    Example: setWhite then placePiece(4, 4, true)");
        System.out.println();
        
        System.out.println(" TIPS:");
        System.out.println("  - All coordinates are 0-based (0-8)");
        System.out.println("  - true/false for boolean parameters");
        System.out.println("  - Commands are case-sensitive");
        System.out.println("  - Use parentheses for method calls");
        System.out.println("  - Board updates visually after placePiece()");
        System.out.println();
        System.out.println("=====================================\n");
    }
    
    // Custom panel that draws the Go board with stones on intersections
    private class GoBoardPanel extends JPanel {
        private static final int CELL_SIZE = 60; // Increased from 40 to 60
        private static final int BOARD_MARGIN = 40; // Increased margin
        
        public GoBoardPanel() {
            // Let the panel fill the available space
            setBackground(new Color(222, 184, 135)); // Tan background for the board
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(), // Outer raised border
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Inner padding
            ));
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleBoardClick(e.getX(), e.getY());
                }
            });
        }
        
        private void handleBoardClick(int mouseX, int mouseY) {
            // Calculate actual board dimensions based on current panel size
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int actualCellSize = Math.min((panelWidth - 2 * BOARD_MARGIN) / (BOARD_SIZE - 1),
                                        (panelHeight - 2 * BOARD_MARGIN) / (BOARD_SIZE - 1));
            int actualMarginX = (panelWidth - actualCellSize * (BOARD_SIZE - 1)) / 2;
            int actualMarginY = (panelHeight - actualCellSize * (BOARD_SIZE - 1)) / 2;
            
            // Convert mouse coordinates to board position
            int col = Math.round((float)(mouseX - actualMarginX) / actualCellSize);
            int row = Math.round((float)(mouseY - actualMarginY) / actualCellSize);
            
            // Check if click is within board bounds
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                handleMove(row, col);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Calculate actual board dimensions based on current panel size
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int actualCellSize = Math.min((panelWidth - 2 * BOARD_MARGIN) / (BOARD_SIZE - 1),
                                        (panelHeight - 2 * BOARD_MARGIN) / (BOARD_SIZE - 1));
            int actualMarginX = (panelWidth - actualCellSize * (BOARD_SIZE - 1)) / 2;
            int actualMarginY = (panelHeight - actualCellSize * (BOARD_SIZE - 1)) / 2;
            
            // Draw grid lines with darker color
            g2d.setColor(new Color(80, 40, 20)); // Darker brown color for better visibility
            g2d.setStroke(new BasicStroke(2)); // Thicker lines
            
            // Draw horizontal lines
            for (int row = 0; row < BOARD_SIZE; row++) {
                int y = actualMarginY + row * actualCellSize;
                g2d.drawLine(actualMarginX, y, actualMarginX + (BOARD_SIZE - 1) * actualCellSize, y);
            }
            
            // Draw vertical lines
            for (int col = 0; col < BOARD_SIZE; col++) {
                int x = actualMarginX + col * actualCellSize;
                g2d.drawLine(x, actualMarginY, x, actualMarginY + (BOARD_SIZE - 1) * actualCellSize);
            }
            
            // Draw row and column indexes
            g2d.setColor(new Color(50, 50, 50)); // Dark gray for indexes
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            
            // Draw row indexes (left side) - moved farther away
            for (int row = 0; row < BOARD_SIZE; row++) {
                int y = actualMarginY + row * actualCellSize + 5; // Slight offset to center
                g2d.drawString(String.valueOf(row), actualMarginX - 40, y); // Moved farther left
            }
            
            // Draw column indexes (top) - moved farther away
            for (int col = 0; col < BOARD_SIZE; col++) {
                int x = actualMarginX + col * actualCellSize - 5; // Slight offset to center
                g2d.drawString(String.valueOf(col), x, actualMarginY - 25); // Moved farther up
            }
            
            // Draw stones on intersections
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    String piece = gameLogic.getBoard()[row][col];
                    if (piece != null) {
                        int x = actualMarginX + col * actualCellSize;
                        int y = actualMarginY + row * actualCellSize;
                        
                        // Draw stone centered on intersection - make stones bigger
                        int stoneSize = Math.max(28, actualCellSize * 6 / 10); // Scale with board size, make bigger
                        int stoneX = x - stoneSize / 2;
                        int stoneY = y - stoneSize / 2;
                        
                        if (piece.equals("○")) {
                            // White stone
                            g2d.setColor(Color.WHITE);
                            g2d.fillOval(stoneX, stoneY, stoneSize, stoneSize);
                            g2d.setColor(Color.BLACK);
                            g2d.setStroke(new BasicStroke(1));
                            g2d.drawOval(stoneX, stoneY, stoneSize, stoneSize);
                        } else if (piece.equals("●")) {
                            // Black stone
                            g2d.setColor(Color.BLACK);
                            g2d.fillOval(stoneX, stoneY, stoneSize, stoneSize);
                        }
                    }
                }
            }
        }
    }
    
    private void handleMove(int row, int col) {
        // Convert GUI player1Turn to App.java logic
        // In GUI: player1Turn=true means White, player1Turn=false means Black
        // In App: player1=true means Black (●), player1=false means White (○)
        // So we need to invert: GUI player1Turn → App !player1Turn
        boolean appPlayer1 = !player1Turn;
        String playerColor = player1Turn ? "White (○)" : "Black (●)";
        System.out.println("Attempting to place " + playerColor + " stone at (" + row + ", " + col + ")");
        
        // Check if position is empty
        if (gameLogic.getBoard()[row][col] != null) {
            String currentPiece = gameLogic.getBoard()[row][col];
            System.out.println("ILLEGAL MOVE: Position (" + row + ", " + col + ") is occupied by " + currentPiece);
            statusLabel.setText("Position occupied! Try another spot.");
            return;
        }
        
        // Check if the move would be legal (not suicide)
        App.Position pos = new App.Position(row, col);
        String colorSymbol = appPlayer1 ? "●" : "○"; // App.java symbols
        boolean wouldBeAlive = App.isAlive(pos, colorSymbol);
        
        if (!wouldBeAlive) {
            System.out.println("ILLEGAL MOVE: Placing " + playerColor + " at (" + row + ", " + col + ") would be suicide (no liberties)");
            statusLabel.setText("Illegal move! That would be suicide.");
            return;
        }
        
        System.out.println("LEGAL MOVE: Placing " + playerColor + " stone at (" + row + ", " + col + ")");
        
        // Place the piece using existing game logic (with inverted boolean)
        gameLogic.placePiece(row, col, appPlayer1);
        
        System.out.println("Stone successfully placed at (" + row + ", " + col + ")");
        
        // Refresh the board display
        boardPanel.repaint();
        
        // Update status (don't auto-switch turns for testing)
        statusLabel.setText("Stone placed! Click player buttons to change color.");
        
        // Check for captures (you can expand this later)
        checkForCaptures(row, col);
    }
    
    private void updatePlayerDisplay() {
        currentPlayerLabel.setText("Current Player: " + (player1Turn ? "White (○)" : "Black (●)"));
        statusLabel.setText("Player set to " + (player1Turn ? "White" : "Black") + ". Click board to place stones.");
    }
    
    
    private void checkForCaptures(int row, int col) {
        // This is where you can integrate your capture logic later
        // For now, just a placeholder
        statusLabel.setText("Move placed! (Capture detection coming soon)");
    }
    
    public static void main(String[] args) {
        // Run on Event Dispatch Thread for Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GoBoardSwing();
            }
        });
    }
}
