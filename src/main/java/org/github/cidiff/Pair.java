package org.github.cidiff;

public record Pair<T>(T left, T right) {

	public static <T> Pair<T> of(T left, T right) {
		return new Pair<>(left, right);
	}

	public record Free<L, R>(L left, R right) {
		public static <L, R> Pair.Free<L, R> of(L left, R right) {
			return new Pair.Free<>(left, right);
		}
	}

}
