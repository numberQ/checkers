package com.company;

import java.io.File;
import java.util.ArrayList;

public class Board {

	private int rows, cols;
	private SqState[][] board;

    /**
     * Constructor.
     *
     * @param rows - The height of the board.
     * @param cols - The width of the baord.
     */
	public Board(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.board = new SqState[rows][cols];

		initBoard();
	}

    /**
     * Creates a board. Technically should be able to handle variable sized board,
     * but it's only been tested on 8x8.
     *
     */
	private void initBoard() {
		SqState state;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {

                // Set default to empty
				state = SqState.EMPTY;

				// Set top rows to black
				if (i < (rows / 2) - 1 && isValidSquare(i, j)) {
					state = SqState.BLACK;
				}
				// Set bottom rows to white
				if (i > rows / 2 && isValidSquare(i, j)) {
					state = SqState.WHITE;
				}

				board[i][j] = state;
			}
		}
	}

    /**
     * Creates a board from the current one with the specified move executed.
     * This is used by the AI for best move evaluation.
     *
     * @param move - The move to take.
     * @return - The board with the move taken.
     */
	public Board copyAndMove(Move move) {

		// Copy the current board over
		Board testBoard = new Board(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				testBoard.setSquare(
						i,
						j,
						board[i][j]
				);
			}
		}

		// Do the potential move
		move.execute(testBoard);

		return testBoard;
	}

    /**
     * Getter for 'rows' field.
     *
     * @return - How many rows the board has.
     */
	public int getRows() {
		return rows;
	}

    /**
     * Getter for 'cols' field.
     *
     * @return - How many cols the board has.
     */
	public int getCols() {
		return cols;
	}

    /**
     * Finds out what piece is on the given square.
     *
     * @param row - The row of the square.
     * @param col - The col of the square.
     * @return - What piece is on that square.
     */
	public SqState getSquare(int row, int col) {
		if (!isValidSquare(row, col)) {
			return SqState.EMPTY;
		}

		return board[row][col];
	}

    /**
     * Replaces the given squares piece with the given state.
     *
     * @param row - The row of the square.
     * @param col - The col of the square.
     * @param state - What state to put on it.
     */
	public void setSquare(int row, int col, SqState state) {
		board[row][col] = state;
	}

    /**
     * Checks if a given square is valid.
     * Valid does not mean legal!
     * This only checks if it's on the board and on the right set of diagonals.
     *
     * @param row - Which row the square is on.
     * @param col - Which col the square is on.
     * @return - True if it's valid, false if not.
     */
    public boolean isValidSquare(int row, int col) {
        // Out of bounds
        if (row < 0 || row >= rows) {
            return false;
        }
        if (col < 0 || col >= cols) {
            return false;
        }

        // Stay on diagonals
        if (row % 2 == 0 && col % 2 == 0) {
            return false;
        }
        if (row % 2 == 1 && col % 2 == 1) {
            return false;
        }

        return true;
    }

    /**
     * Prints this board.
     * Puts a row of col numbers at the top
     * and a col of row letters on the side.
     *
     */
	public void printBoard() {
		System.out.print("   ");
		for (int k = 0; k < cols; k++) {
			System.out.print(k + 1 + " ");
		}
		System.out.println();

		for (int i = 0; i < rows; i++) {
			System.out.print((char)('a' + i) + " ");
			for (int j = 0; j < cols; j++) {
				System.out.print("|");
				System.out.print(board[i][j].getToken());
			}
			System.out.println("|");
		}
	}

    /**
     * Tests every possible move the given player can make for legality.
     *
     * @param player - The player being tested.
     * @return - A list of legal moves.
     */
	public ArrayList<Move> getLegalMoves(SqState player) {
		ArrayList<Move> moves = new ArrayList<>();
		Move move;
		int[] source = new int[2], dest = new int[2];

		// Check all squares on the board
		for (int i = 0; i < getRows(); i++) {
			for (int j = 0; j < getCols(); j++) {

				// We only care about squares with this player's pieces
				if (getSquare(i, j).isSame(player)) {

					// Set source
					source[0] = i;
					source[1] = j;

					// Check all squares around source
					for (int k = -2; k <= 2; k++) {
						for (int l = -2; l <= 2; l++) {

							// Set dest
							dest[0] = i + k;
							dest[1] = j + l;

							// Add move to list if it's legal
							move = new Move(source.clone(), dest.clone(), this, player);
							if (move.isLegal(true)) {
								moves.add(move);
							}
						}
					}
				}
			}
		}

		return moves;
	}

    /**
     * Finds the score the given player has on this board.
     * Each square on the board contributes 1 of 5 values to the player's score:
     * 4 points - safe kings
     * 3 points - safe normals
     * 2 points - unsafe kings
     * 1 point  - unsafe normals
     * 0 points - opponent pieces and empty squares
     * Safeness means that the piece is not in immediate danger of being jumped.
     *
     * This can be vastly improved, but it's a workable approach
     * that can result in some pretty smart moves.
     *
     * @param player - The player whose score we're finding.
     * @return - The score.
     */
	public int getScore(SqState player) {
		int score = 0;

		// Loop through the board, tallying up points for each piece owned by the player
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (board[i][j].isSame(player)) {

					// Safe kings worth 4, safe normals worth 3, unsafe kings worth 2, unsafe normals worth 1
					if (isSafe(i,j)) {
						if (board[i][j].isKinged()) {
							score += 4;
						} else {
							score += 3;
						}
					} else {
						if (board[i][j].isKinged()) {
							score += 2;
						} else {
							score += 1;
						}
					}
				}
			}
		}

		return score;
	}

    /**
     * Checks if a piece is safe.
     * No player is necessary, since it gets the player from the color of the piece.
     * A piece is only unsafe under two conditions:
     *      - there is an opponent piece adjacent to it
     *      - the opponent can legally make the jump
     *
     * @param row - The row of the piece.
     * @param col - The col of the piece.
     * @return - True if the piece is safe, false if not.
     */
	private boolean isSafe(int row, int col) {
		SqState player = board[row][col];
		int[] source = new int[2], dest = new int[2];
		Move move;

		// Check adjacent squares (skipping 0 because only diagonals matter)
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				// Source
				source[0] = row + i;
				source[1] = col + j;

				// Dest is opposite the source
				dest[0] = row - i;
				dest[1] = col - j;

				// Create test jump and test it
				move = new Move(source, dest, this, player.getOpposite());
				if (move.isLegal(true)) {
					return false;
				}
			}
		}

		return true;
	}
}
