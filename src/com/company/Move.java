package com.company;

public class Move {

	private int[] source;
	private int[] dest;
	private Board board;
	private SqState player;
	private boolean jumping;

    /**
     * Constructor.
     *
     * @param source - Where the piece originates from.
     * @param dest - Where the piece is moving.
     * @param board - On what board the move is happening.
     * @param player - Who's making the move.
     */
	public Move(int[] source, int[] dest, Board board, SqState player) {
		this.source = source;
		this.dest = dest;
		this.board = board;
		this.player = player;
		this.jumping = false;
	}

    /**
     * Prints out what this move is.
     * States the player moving, the source, and the destination.
     *
     */
	public void printMove() {
		String output = player + " moved: (" +
				(char)(source[0] + 'a') + "," + (char)(source[1] + '1') + ") to (" +
				(char)(dest[0] + 'a') + "," + (char)(dest[1] + '1') + ")";
		System.out.println(output);
	}

    /**
     * Getter for 'jumping' field.
     * @return - True if this move is a jump, false if not.
     */
	public boolean isJumping() {
		return jumping;
	}

    /**
     * Tests if this move is legal.
     * A move can fail for 7 reasons:
     *      - the source isn't valid
     *      - the source isn't the player's piece
     *      - the destination isn't valid
     *      - a normal is trying to move backwards
     *      - the destination isn't empty
     *      - the destination is too far from the source
     *      - the move isn't a jump when one is possible
     *
     * @param suppressText - Whether or not to print out failure messages.
     * @return - True if the move is legal, false if not.
     */
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

		// Make sure a normal isn't moving backwards
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

		// Make sure a jump is taken if possible
		if (mustJump() && !isJump()) {
			if (!suppressText) {
				System.out.println("You must make a jump if possible!");
				System.out.println();
			}
			return false;
		}

		return true;
	}

    /**
     * Changes the proper squares on the board to make this move happen.
     * Make the source empty.
     * Make the destination filled.
     * Remove a piece if jumping.
     * King a piece if necessary.
     *
     * @param b - The board the move is happening on.
     */
	public void execute(Board b) {
		// Update source as empty and dest as whatever source was
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

    /**
     * Checks that, if this move is backwards, the piece is a king.
     *
     * @return - True if a backwards move is done by a king, false if not.
     */
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

    /**
     * Checks if this move is a slide.
     * A slide only moves one square in any direction.
     *
     * @return - True if it's a slide, false if not.
     */
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

    /**
     * Checks if this move is a jump.
     * A jump moves two squares in any direction,
     * and moves past an opponent's piece.
     *
     * @return - True if it's a jump, false if not.
     */
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

    /**
     * Checks to see if a jump is possible for the current player.
     * The rules of checkers state that a jump must be taken if possible.
     *
     * @return - True if there's a jump, false if not.
     */
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
