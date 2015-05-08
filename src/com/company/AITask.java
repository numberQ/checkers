package com.company;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.RecursiveAction;

public class AITask extends RecursiveAction {

	// Layers of moves the AI will evaluate
	private final int NUM_AI_ITERS;

	private final Board board;
	private final SqState player;
	private final int iters;
	private int score;
	private Move move;

	private final Random rand = new Random(System.currentTimeMillis());

	public AITask(Board board, SqState player, int iters, int maxIters) {
		this.board = board;
		this.player = player;
		this.iters = iters;
		this.NUM_AI_ITERS = maxIters;
	}

	@Override
	protected void compute() {
		Tuple<Move, Integer> bestMoveContainer = compAIWork();
		move = bestMoveContainer.getKey();
		score = bestMoveContainer.getVal();
	}

	protected Move getMove() {
		return move;
	}

	protected int getScore() {
		return score;
	}

	/**
	 * Recursively searches through the state space of possible board layouts.
	 * Weaknesses:
	 *      - Assumes player never moves.
	 *      - Does not account for going again after a jump.
	 *
	 * @return - A Tuple with the best scoring move to return finally, and that move's score for calculation purposes.
	 */
	private Tuple<Move, Integer> compAIWork() {
		int playerScore = 0;
		int opponentScore = board.getRows() * board.getCols();
		int netScore = playerScore - opponentScore;
		int currentScore;
		Board testBoard;
		ArrayList<Move> allMoves = board.getLegalMoves(player);
		ArrayList<Move> bestMoves = new ArrayList<>();
		Tuple<Move, Integer> bestMoveContainer;
		Move move;

		// Dead board - try not to get here
		if (allMoves.isEmpty()) {
			return new Tuple<>(null, netScore);
		}

		// Build list of best moves
		for (int i = 0; i < allMoves.size(); i++) {

			// Set vars
			move = allMoves.get(i);
			testBoard = board.copyAndMove(move);
			playerScore = testBoard.getScore(player);
			opponentScore = testBoard.getScore(player.getOpposite());
			currentScore = playerScore - opponentScore;

			// Recur until we reach the end of a tree
			if (iters < NUM_AI_ITERS) {

				// Fork/join task
				AITask task = new AITask(testBoard, player, iters + 1, NUM_AI_ITERS);
				task.fork();
                task.join();

				// Add parent's score to this one, to search for highest pointed path
				currentScore += task.getScore() - iters;

				// Build list of highest scoring branches
				if (currentScore > netScore) {
					netScore = currentScore;
					bestMoves.clear();
					bestMoves.add(move);
				} else if (currentScore == netScore) {
					bestMoves.add(move);
				}
			} else {

				// Build the list of highest scoring moves
				if (currentScore > netScore) {
					netScore = currentScore;
					bestMoves.clear();
					bestMoves.add(move);
				} else if (currentScore == netScore) {
					bestMoves.add(move);
				}
			}
		}

		int r;

		// No best moves means every move leads to a losing state
		if (bestMoves.isEmpty()) {

			// Select a random legal move
//			r = rand.nextInt(allMoves.size());
//			move = allMoves.get(r);
            move = allMoves.get(0);
		} else {

			// Select a random best move
//			r = rand.nextInt(bestMoves.size());
//			move = bestMoves.get(r);
            move = bestMoves.get(0);
		}

		// Return that best move along with its score
		bestMoveContainer = new Tuple<>(move, netScore);
		return bestMoveContainer;
	}
}
