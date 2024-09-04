package com.seriq.result;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Represents a value of one of two possible types (a disjoint union).
 *
 * @see Result
 */
public sealed interface Either<A, B> {

	record Left<A, B>(A left) implements Either<A, B>, Serializable {
	}

	record Right<A, B>(B right) implements Either<A, B>, Serializable {
	}

	static <A, B> Either<A, B> left(A left) {
		return new Left<>(left);
	}

	static <A, B> Either<A, B> right(B right) {
		return new Right<>(right);
	}

	/**
	 * Map the left value to a new value.
	 */
	default <U> Either<U,B> mapLeft(Function<? super A, ? extends U> mapper) {
		return switch (this) {
			case Left(A left)-> left(mapper.apply(left));
			case Right(B right)-> right(right);
		};
	}

	/**
	 * Map the right value to a new value.
	 */
	default <U> Either<A,U> mapRight(Function<? super B, ? extends U> mapper) {
		return switch (this) {
			case Left(A left)-> left(left);
			case Right(B right)-> right(mapper.apply(right));
		};
	}
}
