# Result library

Library for functional error handling with Either and Result Types.

```java
var result = someBusinessPort.get()                                         // get a Result<SomeValueType, SomeErrorType>
        .map(ApiResponse::getData)                                          // in case of success, get the data
        .mapError(errorHandler::handle)                                     // in case of error, handle the error
        .filter(ok -> ok.getStatusCode() != HttpStatus.ACCEPTED.value(),    // in case of success, filter the result. Supply an error in case the predicate returns false
				() -> getProcessingUnfinishedError());
```

To exit the result type, use a switch statement.

```java
var someThing = switch(result) {
    case Ok ok -> ok;
    case Error error -> createErrorResponse(error);
};
```

Alternatively, there are some convenience methods like consumeError and recover.

There are also some testFixtures for AssertJ, which make testing with result objects a breeze!

```java
ResultAssert.assertThat(underTest.get(stuff))
        .isOkayAnd()
        .isEqualTo(expected);
```