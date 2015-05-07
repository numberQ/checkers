package com.company;

public class Move {

	private int[] source;
	private int[] dest;
	private Board board;
	private SqState player;
	private boolean jumping;

	public Move(int[] source, int[] dest, Board board, SqState player) {
		this.source = source;
		this.dest = dest;
		this.board = board;
		this.player = player;
		this.jumping = false;
	}

	public void printMove() {
		System.out.println(player + " moved: (" +
				(char)(source[0] + 'a') + "," + (char)(source[1] + '1') + ") to (" +
				(char)(dest[0] + 'a') + "," + (char)(dest[1] + '1') + ")");
	}

	public boolean isJumping() {
		return jumping;
	}

	public boolean isLegal(boolean suppressText) {
		// Make sure source is on the board
		if (!board.isValidSquare(source[0], source[1])) {
			if (!suppressText) {
				System.out.println("Source not valid!");
				System.out.println();
			}
			return false;
		}

		// Make sure source is player's piece
		if (!board.getSquare(source[0], source[1]).isSame(player)) {
			if (!suppressText) {
				System.out.println("That's not your piece!");
				System.out.println();
			}
			return false;
		}

		// Make sure dest is on the board
		if (!board.isValidSquare(dest[0], dest[1])) {
			if (!suppressText) {
				System.out.println("Destination not valid!");
				System.out.println();
			}
			return false;
		}

		// Make sure a non-king isn't moving backwards
		if (!isValidKingMovement()) {
			if (!suppressText) {
				System.out.println("Only kings can move backwards!");
				System.out.println();
			}
			return false;
		}

		// Make sure dest is empty
		if (!board.getSquare(dest[0], dest[1]).isEmpty()) {
			if (!suppressText) {
				System.out.println("Destination must be empty!");
				System.out.println();
			}
			return false;
		}

		// Make sure move is within range
		if (!isSlide() && !isJump()) {
			if (!suppressText) {
				System.out.println("Move not in range!");
				System.out.println();
			}
			return false;
		}

		// Make sure a jump is take if possible
		if (mustJump() && !isJump()) {
			if (!suppressText) {
				System.out.println("You must make a jump if possible!");
				System.out.println();
			}
			return false;
		}

		return true;
	}

	public void execute(Board b) {
		// Update source as empty and destination as whatever source was
		SqState state = b.getSquare(source[0], source[1]);
		b.setSquare(source[0], source[1], SqState.EMPTY);
		b.setSquare(dest[0], dest[1], state);

		// Remove jumped piece
		if (jumping) {
			b.setSquare(
					source[0] + (dest[0] - source[0]) / 2,
					source[1] + (dest[1] - source[1]) / 2,
					SqState.EMPTY
			);
		}

		// King me
		switch (player) {
			case WHITE:
				if (dest[0] == 0) {
					b.setSquare(dest[0], dest[1], SqState.WHITE_K);
				}
				break;
			case BLACK:
				if (dest[0] == b.getRows() - 1) {
					b.setSquare(dest[0], dest[1], SqState.BLACK_K);
				}
				break;
		}
	}

	private boolean isValidKingMovement() {
		// Delta row and king checking
		int d_row = dest[0] - source[0];
		boolean kinged = board.getSquare(source[0], source[1]).isKinged();

		switch (player) {
			case BLACK:
				if (d_row < 0 && !kinged) {
					return false;
				}
				break;
			case WHITE:
				if (d_row > 0 && !kinged) {
					return false;
				}
				break;
		}

		return true;
	}

	private boolean isSlide() {
		// Delta row and delta col
		int d_row = dest[0] - source[0];
		int d_col = dest[1] - source[1];

		// Make sure piece is moving 1 up or 1 down
		if (Math.abs(d_row) != 1) {
			return false;
		}
		// Make sure piece is moving 1 left or 1 right
		if (Math.abs(d_col) != 1) {
			return false;
		}

		return true;
	}

	private boolean isJump() {
		// Delta row and delta col
		int d_row = dest[0] - source[0];
		int d_col = dest[1] - source[1];

		jumping = false;

		// Make sure piece is moving 2 up or 2 down
		if (Math.abs(d_row) != 2) {
			return false;
		}
		// Make sure piece is moving 2 left or 2 right
		if (Math.abs(d_col) != 2) {
			return false;
		}

		// Check middle square
		SqState jumped = board.getSquare(
				source[0] + d_row / 2,
				source[1] + d_col / 2
		);
		if (!jumped.isOpposite(player)) {
			return false;
		}

		// Mark that this is a jumping move
		jumping = true;

		return true;
	}

	private boolean mustJump() {
		Move move;
		int[] source = new int[2], dest = new int[2];

		// Iterate over the board
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {

				// We only care about pieces belonging to this player
				if (board.getSquare(i, j).isSame(player)) {

					// Set source for potential move
					source[0] = i;
					source[1] = j;

					// Iterate over surrounding squares
					for (int k = -1; k <= 1; k++) {
						for (int l = -1; l <= 1; l++) {

							// If an opponent's piece is adjacent to the player's
							if (board.getSquare(i + k, j + l).isOpposite(player)) {

								// If the square after that is empty, there may be a jump
								if (board.getSquare(i + k * 2, j + l * 2) == SqState.EMPTY &&
										board.isValidSquare(i + k * 2, j + l * 2)) {

									// Set dest for potential move
									dest[0] = i + k * 2;
									dest[1] = j + l * 2;

									// Set up and test potential move
									move = new Move(source, dest, board, player);
									if (move.isValidKingMovement()) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}

		return false;
	}
}
