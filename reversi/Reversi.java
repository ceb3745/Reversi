package reversi;

/**
 * Implementation of the game board representation and move making for
 * Reversi.
 *
 * @author Sean Strout @ RIT CS
 * @author Robert St Jacquest @ RIT SE
 */
public class Reversi {
    /** The default board size is 8x8 */
    public final static int DIM = 8;

    /**
     * Used to indicate a move that has been made on the board.
     */
    public enum Move {
        PLAYER_ONE('O'),
        PLAYER_TWO('X'),
        NONE('.');

        private char symbol;

        Move(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }
    }

    /** number of rows in board */
    private int rows;
    /** number of columns in board */
    private int cols;
    /** the board */
    private Move[][] board;
    /** which player's turn is it? */
    private boolean p1Turn;
    /** how many valid moves have been made? */
    private int numMoves;

    /**
     * Default construct an 8x8 board.
     */
    public Reversi() {
        this(DIM, DIM);
    }

    /**
     * Construct a board of a specified size.
     *
     * @param rows number of rows
     * @param cols number of columns
     *
     * @rit.pre the board dimensions cannot be smaller than 2x2
     */
    public Reversi(int rows, int cols) {
        // initialize all spots on the board to empty
        this.board = new Move[rows][cols];
        for (int row=0; row<rows; ++row) {
            for (int col=0; col<cols; ++col) {
                this.board[row][col] = Move.NONE;
            }
        }
        // populate the center of the board with pieces
        this.board[rows/2-1][cols/2-1] = Move.PLAYER_ONE;
        this.board[rows/2][rows/2] = Move.PLAYER_ONE;
        this.board[rows/2-1][cols/2] = Move.PLAYER_TWO;
        this.board[rows/2][cols/2-1] = Move.PLAYER_TWO;

        // finishing setting up all instance data
        this.rows = rows;
        this.cols = cols;
        this.p1Turn = true;
        this.numMoves = 4;
    }

    /**
     * Get the number of rows in the board.
     *
     * @return number of rows
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Get the number of columns in the board.
     *
     * @return number of columns
     */
    public int getCols() {
        return this.cols;
    }

    /**
     * Check that there is an occupied neighbor - we relax the official rules
     * here that say the neighbor must be the same color.
     *
     * @param row the row
     * @param col the column
     * @return whether there is an occupied neighbor or not
     */
    private boolean occupiedNeighbor(int row, int col) {
        // scan through all neighbors and see if there is at least one that is occupied
        for (int r=row-1; r<=row+1; ++r) {
            for (int c=col-1; c<=col+1; ++c) {
                if (!(r == row && c == col)) {
                    if (r >= 0 && r < this.rows && c >= 0 && c < this.cols &&
                            this.board[r][c] != Move.NONE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * When a new valid move is made, this helper routine flips all the pieces
     * on the board that are affected.
     *
     * @param row the row
     * @param col the column
     */
    private void flipPieces(int row, int col) {
        // figure out who made the move and who the other player is
        Move me = this.board[row][col];
        Move other = me == Move.PLAYER_ONE ? Move.PLAYER_TWO : Move.PLAYER_ONE;

        // generate row and column deltas [-1, 0, +1] to check the eight directions
        // on the board.  this code comes courtesy of a port of python code from
        // the great and mighty oracle himself, Jim. <3
        for (int rd=-1; rd<=1; ++rd) {
            for (int cd=-1; cd<=1; ++cd) {
                if (rd == 0 && cd == 0) {
                    continue;
                }

                int r = row + rd;
                int c = col + cd;

                // Continue in the current direction until we go off the end of the
                // board or we reach a square that does not contain an opponent's disc.
                while (r >= 0 && r < this.rows && c >= 0 && c < this.rows && this.board[r][c] == other) {
                    r += rd;
                    c += cd;
                }

                // If we did not go off the board and the square we stopped on
                // contains one of this player's discs, flips the ones in between.
                if (r >= 0 && r < this.rows && c >= 0 && c < this.rows && this.board[r][c] == me) {
                    // restart
                    r = row + rd;
                    c = col + cd;

                    while (r >= 0 && r < this.rows && c >= 0 && c < this.rows && this.board[r][c] == other) {
                        this.board[r][c] = me;
                        r += rd;
                        c += cd;
                    }
                }
            }
        }
    }

    /**
     * Called when a move is made in the game.  This routine verifies
     * the move is valid, and then updates the board state.
     *
     * @param row the row to place the new piece
     * @param col the column to place the new piece
     * @throws ReversiException if the move is invalid
     */
    public void makeMove(int row, int col) throws ReversiException {
        // check for exceptions
        if (row < 0 || row >= this.rows) {
            throw new ReversiException("Invalid row: " + row);
        } else if (col < 0 || col >= this.cols) {
            throw new ReversiException("Invalid column: " + col);
        } else if (this.board[row][col] != Move.NONE) {
            throw new ReversiException("Cell occupied: " + "(" + row + ", " + col + ")");
        } else {
            if (!occupiedNeighbor(row, col)) {
                throw new ReversiException("No neighbor: " + "(" + row + ", " + col + ")");
            }
        }

        // place piece on board
        ++this.numMoves;
        this.board[row][col] = this.p1Turn ? Move.PLAYER_ONE : Move.PLAYER_TWO;

        // flip opposite neighbors
        flipPieces(row, col);

        this.p1Turn = !this.p1Turn;
    }

    /**
     * Check to see if the game is over (board is filled)
     *
     * @return whether the game is over or not
     */
    public boolean gameOver() {
        return this.numMoves == this.rows * this.cols;
    }

    /**
     * Get the winner of the game.
     *
     * @rit.pre The game must be over.
     * @return The winner.
     */
    public Move getWinner() {
        // count the disks for each player
        int p1Disks = 0;
        int p2Disks = 0;
        for (int row=0; row<this.rows; ++row) {
            for (int col=0; col<this.cols; ++col) {
                if (this.board[row][col] == Move.PLAYER_ONE) {
                    ++p1Disks;
                } else {
                    ++p2Disks;
                }
            }
        }

        // determine winner
        if (p1Disks == p2Disks) {
            return Move.NONE;
        } else if (p1Disks > p2Disks) {
            return Move.PLAYER_ONE;
        } else {
            return Move.PLAYER_TWO;
        }
    }

    /**
     * Returns a string representation of the board, suitable for printing out.
     * The starting board for a 4x4 game would be:<br>
     * <br><tt>
     *    0  1  2  3<br>
     *  0[.][.][.][.]<br>
     *  1[.][O][X][.]<br>
     *  2[.][X][O][.]<br>
     *  3[.][.][.][.]<br>
     * </tt>
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // build the top row with column numbers
        builder.append(' ');
        for(int c=0; c<this.cols; ++c) {
            builder.append(" " + c + ' ');
        }
        builder.append('\n');

        // build remaining rows with row numbers and column values
        for(int r=0; r<this.rows; ++r) {
            builder.append(r);
            for(int c=0; c<this.cols; ++c) {
                builder.append('[');
                builder.append(this.board[r][c].getSymbol());
                builder.append(']');
            }
            builder.append('\n');
        }

        return builder.toString();
    }
}
