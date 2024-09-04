package com.seriq.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Functional programming style result type. Allows for more complex method and processing chaining and gets rid of
 * horrid data flows via exception throwing
 *
 * @param <A> the potential value type
 * @param <E> the potential error case type
 */

public sealed interface Result<A, E> {

	/**
	 * All Ok implementation of a result.
	 */
	record Ok<A, E>(A value) implements Result<A, E>, Serializable {
	}

	/**
	 * Error implementation of a result.
	 */
	record Error<A, E>(E error) implements Result<A, E>, Serializable {
	}

	/**
	 * Create a new {@link Result} instance with a value.
	 */
	static <A, E> Result<A, E> ok(final A value) {
		return new Ok<>(value);
	}

	/**
	 * Create a new {@link Result} from an optional.
	 * The result will be in error state with the error from the supplier in case the optional is empty.
	 */
	static <A, E> Result<A, E> fromOptional(
			final Optional<A> optional,
			final Supplier<E> errorSupplier) {
		Objects.requireNonNull(optional);
		Objects.requireNonNull(errorSupplier);

		return optional
				.map(Result::<A, E>ok)
				.orElseGet(() -> Result.error(errorSupplier.get()));
	}

	/**
	 * Create a new {@link Result} instance with an error.
	 */
	static <A, E> Result<A, E> error(final E error) {
		return new Error<>(error);
	}

	/**
	 * Map the value to a new value with a function that also returns a result.
	 * This allows for chaining of operations that can fail, but only if the expected error is of the same type.
	 */
	default <U> Result<U, E> flatMap(final Function<? super A, Result<? extends U, ? extends E>> mapper) {
		Objects.requireNonNull(mapper);

		return switch (this) {
			case Ok(A ok) -> switch (mapper.apply(ok)) {
				case Ok(U stillOk) -> Result.ok(stillOk);
				case Error(E error) -> Result.error(error);
			};
			case Error(E error) -> Result.error(error);
		};
	}

	/**
	 * Map the value to a new value with a function that also returns a result.
	 * This allows for chaining of operations that can fail if the expected error is of a different type.
	 */
	default <U, F> Result<U, Either<E, F>> flatMapEither(
			final Function<? super A, Result<? extends U, ? extends F>> mapper) {
		Objects.requireNonNull(mapper);

		return switch (this) {
			case Ok(A ok) -> switch (mapper.apply(ok)) {
				case Ok(U stillOk) -> Result.ok(stillOk);
				case Error(F error) -> Result.error(Either.right(error));
			};
			case Error(E error) -> Result.error(Either.left(error));
		};
	}

	/**
	 * Map the value to a new value.
	 */
	default <U> Result<U, E> map(final Function<? super A, ? extends U> mapper) {
		Objects.requireNonNull(mapper);

		return switch (this) {
			case Ok(A ok) -> Result.ok(mapper.apply(ok));
			case Error(E error) -> Result.error(error);
		};
	}

	/**
	 * Run the consumer on success.
	 */
	default Result<A, E> run(final Consumer<? super A> consumer) {
		Objects.requireNonNull(consumer);

		if (this instanceof Ok(A ok)) {
			consumer.accept(ok);
		}
		return this;
	}

	/**
	 * Run the consumer on error.
	 */
	default Result<A, E> runError(final Consumer<? super E> consumer) {
		Objects.requireNonNull(consumer);

		if (this instanceof Error(E error)) {
			consumer.accept(error);
		}
		return this;
	}

	/**
	 * Consume the error, returning the value if it is an {@link Ok}. Returns an empty {@link Optional}
	 * if the result is an {@link Error}.
	 */
	default Optional<A> consumeError() {
		return switch (this) {
			case Ok(A ok) -> Optional.of(ok);
			case Error(_) -> Optional.empty();
		};
	}

	/**
	 * Consume the error, returning the value if it is an {@link Ok}. Returns an empty {@link Optional}
	 * if the result is an {@link Error}.
	 */
	default Optional<A> consumeError(final Consumer<? super E> consumer) {
		return switch (this) {
			case Ok(A ok) -> Optional.of(ok);
			case Error(E error) -> {
				consumer.accept(error);
				yield Optional.empty();
			}
		};
	}

	/**
	 * Map the error to a new value.
	 */
	default <U> Result<A, U> mapError(final Function<? super E, ? extends U> mapper) {
		Objects.requireNonNull(mapper);

		return switch (this) {
			case Ok(A ok) -> Result.ok(ok);
			case Error(E error) -> Result.error(mapper.apply(error));
		};
	}

	/**
	 * Resolve the result to a value.
	 */
	default <U> U resolve(final Function<Result<A, E>, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		return mapper.apply(this);
	}

	/**
	 * Resolve the result to a value, depending on the result type.
	 */
	default <U> U resolve(
			final Function<? super A, ? extends U> mapper,
			final Function<? super E, ? extends U> errorMapper
	) {
		Objects.requireNonNull(mapper);
		Objects.requireNonNull(errorMapper);

		return switch (this) {
			case Ok(A ok) -> mapper.apply(ok);
			case Error(E error) -> errorMapper.apply(error);
		};
	}

	/**
	 * Filter the value, returning an error if the predicate does not match.
	 */
	default Result<A, E> filter(
			final Predicate<? super A> predicate,
			final Supplier<? extends E> errorSupplier
	) {
		Objects.requireNonNull(predicate);
		Objects.requireNonNull(errorSupplier);

		return switch (this) {
			case Ok(A ok) -> predicate.test(ok) ? Result.ok(ok) : Result.error(errorSupplier.get());
			case Error(E error) -> Result.error(error);
		};
	}

	/**
	 * Filter the value, returning an error if the predicate does not match. The error is generated from the value
	 * via the given okToErrorMapper.
	 */
	default Result<A, E> filter(
			final Predicate<? super A> predicate,
			final Function<? super A, ? extends E> okToErrorMapper
	) {
		Objects.requireNonNull(predicate);
		Objects.requireNonNull(okToErrorMapper);

		return switch (this) {
			case Ok(A ok) -> predicate.test(ok) ? Result.ok(ok) : Result.error(okToErrorMapper.apply(ok));
			case Error(E error) -> Result.error(error);
		};
	}

	/**
	 * Recover an error into a new value.
	 */
	default A recover(final Function<? super E, ? extends A> recoveryFunction) {
		Objects.requireNonNull(recoveryFunction);

		return switch (this) {
			case Ok(A ok) -> ok;
			case Error(E error) -> recoveryFunction.apply(error);
		};
	}

	/**
	 * Flatten a collection of results (of the same type) into a single result by collecting the values and errors.
	 * If at least one error is present, the result will be in error state with the List of all errors.
	 */
	static <A, E> Result<List<A>, List<E>> flatten(final Collection<Result<A, E>> results) {
		Objects.requireNonNull(results);

		final Collection<A> values = new ArrayList<>();
		final Collection<E> errors = new ArrayList<>();

		for (final var result : results) {
			result.consumeError(errors::add).ifPresent(values::add);
		}

		return errors.isEmpty() ? Result.ok(List.copyOf(values)) : Result.error(List.copyOf(errors));
	}

	/**
	 * Check if the result is an error.
	 */
	default boolean isError() {
		return this instanceof Error;
	}

	/**
	 * Check if the result is ok.
	 */
	default boolean isOk() {
		return this instanceof Ok;
	}
}
