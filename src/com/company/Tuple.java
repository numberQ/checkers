package com.company;

/**
 * A generic tuple object.
 *
 * @param <X> - The first object in the pair.
 * @param <Y> - The second object in the pair.
 */
public class Tuple<X, Y> {

	private X x;
	private Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public X getKey() {
		return x;
	}

	public Y getVal() {
		return y;
	}

	public void setKey(X x) {
		this.x = x;
	}

	public void setVal(Y y) {
		this.y = y;
	}
}
