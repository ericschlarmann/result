package com.seriq.result;

import static com.seriq.result.ResultAssert.assertThat;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ResultTest {

	@Test
	void fromOptional_ok() {
		var result = Result.fromOptional(Optional.of("value"), () -> "error");

		assertThat(result)
				.isOkayAnd()
				.isEqualTo("value");
	}

	@Test
	void fromOptional_error() {
		var result = Result.fromOptional(Optional.empty(), () -> "error");

		assertThat(result)
				.isErrorAnd()
				.isEqualTo("error");
	}

	@Test
	void flatMapEither_error_left() {
		var result = Result.ok("value")
				.flatMapEither(_ -> Result.error("error"));

		assertThat(result)
				.isEitherErrorAnd()
				.isRightAnd()
				.isEqualTo("error");
	}

	@Test
	void flatMapEither_error_right() {
		var result = Result.error("error")
				.flatMapEither(_ -> Result.ok("ok"));

		assertThat(result)
				.isEitherErrorAnd()
				.isLeftAnd()
				.isEqualTo("error");
	}

	@Test
	void flatMapEither_ok() {
		var result = Result.ok("value")
				.flatMapEither(_ -> Result.ok("ok"));

		assertThat(result)
				.isOkayAnd()
				.isEqualTo("ok");
	}

	@Test
	void filter_ok() {
		var result = Result.ok("value")
				.filter(value -> value.equals("value"), () -> "error");

		assertThat(result)
				.isOkayAnd()
				.isEqualTo("value");
	}

	@Test
	void flter_error() {
		var result = Result.ok("value")
				.filter(value -> value.equals("other"), () -> "error");

		assertThat(result)
				.isErrorAnd()
				.isEqualTo("error");
	}

	@Test
	void consumeError_ok() {
		var result = Result.ok("value")
				.consumeError();

		Assertions.assertThat(result)
				.contains("value");
	}

	@Test
	void consumeError_error() {
		var result = Result.error("error")
				.consumeError();

		Assertions.assertThat(result)
				.isEmpty();
	}

	@Test
	void recover_ok() {
		var result = Result.ok("value")
				.recover(_ -> "recovered");

		Assertions.assertThat(result)
				.isEqualTo("value");
	}

	@Test
	void recover_error() {
		var result = Result.error("error")
				.recover(error -> error);

		Assertions.assertThat(result)
				.isEqualTo("error");
	}
}