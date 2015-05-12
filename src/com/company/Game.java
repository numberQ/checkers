package com.company;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

public class Game {

	// Thread vars
	private int NUM_THREADS;
	private ForkJoinPool pool;

	// How deep to search
	private int NUM_AI_ITERS;

    // For benchmarking
    private  int NUM_REPEATS;

    // Game vars
    private int mode;
	private SqState currentPlayer;
	private Board board;
	private Scanner scanner = new Scanner(System.in);
	private Random rand = new Random(System.currentTimeMillis());

    /**
     * Constructor.
     * Sets up a default checkers board, 8x8 and black goes first.
     *
     */
	public Game() {
		int rows = 8, cols = 8;
		this.currentPlayer = SqState.BLACK;
		this.board = new Board(rows, cols);
	}

    /**
     * Initializes the game.
     * Also contains the main game loop, and ends the game.
     *
     */
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

        // Set benchmarking repetition
        System.out.println("How many times should the AI repeat its move search? (Set to 1 if not benchmarking). ");
        NUM_REPEATS = scanner.nextInt();
        scanner.nextLine();

        // Player vs computer or computer vs computer?
        System.out.println("Enter 1 for player vs computer, 2 for computer vs computer. ");
        mode = scanner.nextInt();
        scanner.nextLine();

        System.out.println();

		// Benchmark start
		long start, end, result;
		start = System.currentTimeMillis();

		gameLoop();

		// Benchmark end
		end = System.currentTimeMillis();
		result = end - start;
		System.out.println("Time taken with " + NUM_THREADS + " thread(s): " + result);
        System.out.println();

        // Game over!
		System.out.println("Game over!");
		System.out.println(currentPlayer.getOpposite() + " won!");
	}

    /**
     * The main game loop.
     * Handles all things related to turn order, AI, printing the game, and ending the game.
     */
	private void gameLoop() {
		boolean gameOver = false;
		ArrayList<Move> moves;
		Move move;

		// Print board
		board.printBoard();

		while (!gameOver) {
			System.out.println(currentPlayer + "'s turn");

            if (mode == 1) {
                // Player vs computer

                if (currentPlayer == SqState.BLACK) {

                    // The player's turn
                    move = userSelectMove();
                } else {

                    // The computer's turn
                    move = compAI();

                    // Slow down the output, so the player can see the new board state
                    System.out.println("Press enter when ready.");
                    scanner.nextLine();
                }
            } else {
                // Computer vs computer

                move = compAI();
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
	 * @return - The move that leads to the highest point total.
	 */
	private Move compAI() {
		Move bestMove = null;
        int average = 0;

        // Search for best move repeatedly, for benchmarking purposes
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

		// It shouldn't return null, but set default behavior just in case
		if (bestMove == null) {
			ArrayList<Move> allMoves = board.getLegalMoves(currentPlayer);
			int r = rand.nextInt(allMoves.size());
			bestMove = allMoves.get(r);
		}

		return bestMove;
	}

    /**
     * Prompts the user for a move and ensures it's a proper one.
     *
     * @return - The move selected by the user.
     */
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

            // Show the board if the move isn't legal
			if (!move.isLegal(false)) {
				board.printBoard();
				System.out.println(currentPlayer + "'s turn");
				tryAgain = true;
			}
		} while (tryAgain);

		return move;
	}
}
