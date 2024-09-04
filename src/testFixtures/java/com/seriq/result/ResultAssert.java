package com.seriq.result;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

/**
 * Asserts for {@link Result} instances.
 */
public class ResultAssert<A, E> extends AbstractAssert<ResultAssert<A, E>, Result<A, E>> {
	public static <B, F> ResultAssert<B, F> assertThat(Result<B, F> actual) {
		return new ResultAssert<>(actual);
	}

	public ResultAssert(Result<A, E> actual) {
		super(actual, ResultAssert.class);
	}

	public ResultAssert<A, E> isOk() {
		isNotNull();
		if (actual.isError()) {
			failWithMessage("Expected result to be ok, but was error");
		}
		return this;
	}

	public ResultAssert<A, E> isError() {
		isNotNull();
		if (actual.isOk()) {
			failWithMessage("Expected result to be error, but was ok");
		}
		return this;
	}

	public ObjectAssert<A> isOkayAnd() {
		isNotNull();
		return switch (actual) {
			case Result.Ok(A ok) -> Assertions.assertThat(ok);
			case Result.Error(E error) -> throw failure("Expected result to be ok, but was error");
		};
	}

	public ObjectAssert<E> isErrorAnd() {
		isNotNull();
		return switch (actual) {
			case Result.Ok(A ok) -> throw failure("Expected result to be error, but was ok");
			case Result.Error(E error) -> Assertions.assertThat(error);
		};
	}

	public <L, R> EitherAssert<L, R> isEitherErrorAnd() {
		isNotNull();
		return switch (actual) {
			case Result.Ok(A ok) -> throw failure("Expected result to be error, but was ok");
			case Result.Error(Either either) -> EitherAssert.assertThat(either);
			case Result.Error(_) -> throw failure("Expected result to be error, but was ok");
		};
	}
}
