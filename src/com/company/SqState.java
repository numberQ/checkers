package com.company;

public enum SqState {

	EMPTY   (" ", false),
	WHITE   ("w", false),
	BLACK   ("b", false),
	WHITE_K ("W", true),
	BLACK_K ("B", true);

	private String token;
	private boolean kinged;

	SqState (String token, boolean kinged) {
		this.token = token;
		this.kinged = kinged;
	}

	public String getToken() {
		return token;
	}

	public boolean isKinged() {
		return kinged;
	}

	public boolean isSame(SqState state) {
		switch (this) {
			case WHITE:
			case WHITE_K:
				if (state == WHITE || state == WHITE_K) {
					return true;
				}
				break;
			case BLACK:
			case BLACK_K:
				if (state == BLACK || state == BLACK_K) {
					return true;
				}
				break;
		}

		return false;
	}

	public boolean isOpposite(SqState state) {
		switch (this) {
			case WHITE:
			case WHITE_K:
				if (state == BLACK || state == BLACK_K) {
					return true;
				}
				break;
			case BLACK:
			case BLACK_K:
				if (state == WHITE || state == WHITE_K) {
					return true;
				}
				break;
		}

		return false;
	}

	public SqState getOpposite() {
		switch (this) {
			case WHITE:
			case WHITE_K:
				return BLACK;
			case BLACK:
			case BLACK_K:
				return WHITE;
			default:
				return EMPTY;
		}
	}

	public boolean isEmpty() {
		return (this == EMPTY);
	}
}