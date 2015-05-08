package com.company;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

public class Game {

	// For outputting data logs
	File file;
    private final int NUM_REPEATS = 100;

	// Threads to create
	private int NUM_THREADS;
	private ForkJoinPool pool;

	// How deep to search
	private int NUM_AI_ITERS;

	private SqState currentPlayer;
	private Board board;
	private Scanner scanner = new Scanner(System.in);
	private Random rand = new Random(System.currentTimeMillis());

	public Game() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		this.file = new File(timeStamp + ".txt");

		int rows = 8, cols = 8;
		this.currentPlayer = SqState.BLACK;
		this.board = new Board(rows, cols, file);
	}

	public void initGame() {

		// Set number of threads
		int numProc = Runtime.getRuntime().availableProcessors();
		System.out.println("How many threads to use? (" + numProc + " available) ");
		NUM_THREADS = scanner.nextInt();
		scanner.nextLine();

		// Create thread pool
		pool = new ForkJoinPool(NUM_THREADS);

		// Set search depth
		System.out.println("How deep should the AI search? ");
		NUM_AI_ITERS = scanner.nextInt();
		scanner.nextLine();

		// Set up file for data recording
		Main.write(file, "Thread count: " + NUM_THREADS);
		Main.write(file, "Search depth: " + NUM_AI_ITERS);
		Main.write(file, "");

		// Benchmark start
		long start, end, result;
		start = System.currentTimeMillis();

		gameLoop();

		// Benchmark end
		end = System.currentTimeMillis();
		result = end - start;
		System.out.println("Time taken with " + NUM_THREADS + " thread(s): " + result);
		Main.write(file, "");
		Main.write(file, "Total time taken with " + NUM_THREADS + " thread(s): " + result);

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
			Main.write(file, currentPlayer + "'s turn");

			/*// Select a piece to move
			if (currentPlayer == SqState.BLACK) {
				// The player is black
				move = userSelectMove();
			} else {
				// The computer is white
				move = compAI(board);
				// Wait for user input
				System.out.println("Press enter when ready.");
				scanner.nextLine();
			}*/

			// AI competing against itself
			move = compAI();

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
			Main.write(file, "");
			board.printBoard();
		}
	}

	/**
	 * Finds the best move for the current board based on a few heuristics.
	 * Heuristics are detailed in the Board function 'getScore'.
	 *
	 * @return - The move that leads to the highest point total.
	 */
	private Move compAI() {
		Move bestMove = null;

        int average = 0;

        for (int i = 0; i < NUM_REPEATS; i++) {
            // Benchmark start
            long start, end, result;
            start = System.currentTimeMillis();

            // Start parallelism
            AITask root = new AITask(board, currentPlayer, 0, NUM_AI_ITERS);
            pool.invoke(root);

            // Benchmark end
            end = System.currentTimeMillis();
            result = end - start;
            average += result;

            // Select the best move
            bestMove = root.getMove();
        }

        average /= NUM_REPEATS;

        System.out.println("Average time taken with " + NUM_THREADS + " thread(s) and " + NUM_REPEATS + " repeat(s): " + average);
        Main.write(file, "Average time taken with " + NUM_THREADS + " thread(s) and " + NUM_REPEATS + " repeat(s): " + average);

		// It shouldn't return null, but set default behavior just in case
		if (bestMove == null) {
			ArrayList<Move> allMoves = board.getLegalMoves(currentPlayer);
//			int r = rand.nextInt(allMoves.size());
//			bestMove = allMoves.get(r);
            bestMove = allMoves.get(0);
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
