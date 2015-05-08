package com.company;

import java.io.File;
import java.util.ArrayList;

public class Board {

	private int rows, cols;
	private SqState[][] board;
	File file;

	public Board(int rows, int cols, File file) {
		this.rows = rows;
		this.cols = cols;
		this.board = new SqState[rows][cols];
		this.file = file;

		initBoard();
	}

	private void initBoard() {
		SqState state;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
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

	public Board copyAndMove(Move move) {
		// Copy the current board over
		Board tempBoard = new Board(rows, cols, file);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				tempBoard.setSquare(
						i,
						j,
						board[i][j]
				);
			}
		}

		// Do the potential move
		move.execute(tempBoard);

		return tempBoard;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

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

	public SqState getSquare(int row, int col) {
		if (!isValidSquare(row, col)) {
			return SqState.EMPTY;
		}

		return board[row][col];
	}

	public void setSquare(int row, int col, SqState state) {
		board[row][col] = state;
	}

	public void printBoard() {
		String output;

		System.out.print("   ");
		output = "   ";
		for (int k = 0; k < cols; k++) {
			System.out.print(k + 1 + " ");
			output += k + 1 + " ";
		}
		System.out.println();
		Main.write(file, output);

		for (int i = 0; i < rows; i++) {
			System.out.print((char)('a' + i) + " ");
			output = (char)('a' + i) + " ";
			for (int j = 0; j < cols; j++) {
				System.out.print("|");
				System.out.print(board[i][j].getToken());
				output += "|" + board[i][j].getToken();
			}
			System.out.println("|");
			output += "|";
			Main.write(file, output);
		}
	}

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

	public int getScore(SqState player) {
		int score = 0;

		// Loop through the board, tallying up points for each piece owned by the player
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (board[i][j].isSame(player)) {

					// Safe kings worth 4, safe normals worth 3, unsafe kings worth 2, unsafe normals worth 1
					if (board[i][j].isKinged()) {
						if (isSafe(i, j)) {
							score += 4;
						} else {
							score += 3;
						}
					} else {
						if (isSafe(i, j)) {
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

				// Dest
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
