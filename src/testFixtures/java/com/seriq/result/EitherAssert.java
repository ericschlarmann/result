package com.seriq.result;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

/**
 * Asserts for {@link Either} instances.
 */
public class EitherAssert<L, R> extends AbstractAssert<EitherAssert<L, R>, Either<L, R>> {
	public static <L, R> EitherAssert<L, R> assertThat(Either<L, R> actual) {
		return new EitherAssert<L, R>(actual);
	}

	public EitherAssert(Either<L, R> actual) {
		super(actual, EitherAssert.class);
	}

	public EitherAssert<L, R> isLeft() {
		isNotNull();
		if (actual instanceof Either.Right) {
			failWithMessage("Expected either to be left, but was right");
		}
		return this;
	}

	public EitherAssert<L, R> isRight() {
		isNotNull();
		if (actual instanceof Either.Left) {
			failWithMessage("Expected either to be right, but was left");
		}
		return this;
	}

	public ObjectAssert<L> isLeftAnd() {
		isNotNull();
		return switch (actual) {
			case Either.Left(L left) -> Assertions.assertThat(left);
			case Either.Right(_) -> throw failure("Expected either to be left, but was right");
		};
	}

	public ObjectAssert<R> isRightAnd() {
		isNotNull();
		return switch (actual) {
			case Either.Left(_) -> throw failure("Expected either to be right, but was left");
			case Either.Right(R right) -> Assertions.assertThat(right);
		};
	}
}
