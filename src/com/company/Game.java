package com.company;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

public class Game {

	// Threads to create
	private int NUM_THREADS;

	// How deep to search
	private int NUM_AI_ITERS;

	private SqState currentPlayer;
	private Board board;
	private Scanner scanner = new Scanner(System.in);
	private Random rand = new Random(System.currentTimeMillis());

	public Game() {
		int rows = 8, cols = 8;
		this.currentPlayer = SqState.BLACK;
		this.board = new Board(rows, cols);
	}

	public void initGame() {

		// Set number of threads
		System.out.println("How many threads to use? ");
		NUM_THREADS = scanner.nextInt();
		scanner.nextLine();

		// Set search depth
		System.out.println("How deep should the AI search? ");
		NUM_AI_ITERS = scanner.nextInt();
		scanner.nextLine();

		gameLoop();

		System.out.println("Game over!");
		System.out.println(currentPlayer.getOpposite() + " won!");
	}

	private void gameLoop() {
		boolean gameOver = false;
		ArrayList<Move> moves;
		Move move;

		// Print board
		board.printBoard();

		while (!gameOver) {
			System.out.println(currentPlayer + "'s turn");

			// Select a piece to move
			if (currentPlayer == SqState.BLACK) {

				// The player is black
				move = userSelectMove();
			} else {

				// The computer is white
				move = compAI(board);

				// Wait for user input
				System.out.println("Press enter when ready.");
				scanner.nextLine();
			}

			// Make the move
			move.execute(board);

			// Switch turns
			if (currentPlayer == SqState.BLACK) {
				currentPlayer = SqState.WHITE;
			} else {
				currentPlayer = SqState.BLACK;
			}

			// Find all legal moves
			moves = board.getLegalMoves(currentPlayer);

			// Check win condition
			if (moves.isEmpty()) {
				gameOver = true;
			} else if (move.isJumping()) {

				// If we're in the middle of a jump chain, switch back
				if (currentPlayer == SqState.BLACK) {
					currentPlayer = SqState.WHITE;
				} else {
					currentPlayer = SqState.BLACK;
				}
			}

			// Print board
			move.printMove();
			board.printBoard();
		}
	}

	/**
	 * Finds the best move for the current board based on a few heuristics.
	 * Heuristics are detailed in the Board function 'getScore'.
	 *
	 * @param b - The current board.
	 * @return - The move that leads to the highest point total.
	 */
	private Move compAI(Board b) {
		Move bestMove;

		// Benchmark start
		long start, end, result;
		start = System.currentTimeMillis();

		// Start parallelism
		ForkJoinPool pool = new ForkJoinPool(NUM_THREADS);
		AITask root = new AITask(b, currentPlayer, 0, NUM_AI_ITERS);
		pool.invoke(root);

		// Benchmark end
		end = System.currentTimeMillis();
		result = end - start;
		System.out.println("Time taken with " + NUM_THREADS + " thread(s): " + result);

		// Select the best move
		bestMove = root.getMove();

		// It shouldn't return null, but set default behavior just in case
		if (bestMove == null) {
			ArrayList<Move> allMoves = b.getLegalMoves(currentPlayer);
			int r = rand.nextInt(allMoves.size());
			bestMove = allMoves.get(r);
		}

		return bestMove;
	}

	private Move userSelectMove() {
		int[] moveSource = new int[2], moveDest = new int[2];
		String source, dest;
		Move move;
		boolean tryAgain;

		// Get user input, repeat if invalid
		do {
			tryAgain = false;

			// Get source
			do {
				System.out.println("Enter row and col of source: ");
				source = scanner.nextLine();
			} while (source.length() != 2);

			// Get destination
			do {
				System.out.println("Enter row and col of destination: ");
				dest = scanner.nextLine();
			} while (dest.length() != 2);

			// Assign to arrays
			moveSource[0] = source.charAt(0) - 'a';
			moveSource[1] = source.charAt(1) - '1';
			moveDest[0] = dest.charAt(0) - 'a';
			moveDest[1] = dest.charAt(1) - '1';

			// Create and evaluate the move
			move = new Move(moveSource, moveDest, board, currentPlayer);

			if (!move.isLegal(false)) {
				board.printBoard();
				System.out.println(currentPlayer + "'s turn");
				tryAgain = true;
			}
		} while (tryAgain);

		return move;
	}
}
