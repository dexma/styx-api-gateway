package com.dexmatech.styx.core.pipeline.stages;

import com.dexmatech.styx.core.http.HttpMessage;
import com.dexmatech.styx.core.http.HttpResponse;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class StageResult<T extends HttpMessage> {

	public static <T extends HttpMessage> CompletableFuture completeStageSuccessfullyWith(T httpMessage) {
		return CompletableFuture.completedFuture(new Success(httpMessage));
	}

	public static <T extends HttpMessage> CompletableFuture completeStageFailingWith(HttpResponse httpResponse, Throwable cause) {
		return CompletableFuture.completedFuture(new Fail(httpResponse, cause));
	}

	public static <T extends HttpMessage> StageResult<T> stageSuccessWith(T httpMessage) {
		return new Success(httpMessage);
	}

	public static <T extends HttpMessage> StageResult<T> stageFailWith(HttpResponse httpResponse, Throwable cause) {
		if (cause instanceof CompletionException && cause.getCause() != null) {
			cause = cause.getCause();
		}
		return new Fail(httpResponse, cause);
	}

	public abstract boolean isSuccess();

	public abstract boolean isFail();

	public abstract T getSuccess();

	public abstract HttpResponse getFail();

	public abstract Throwable getFailCause();

	//	public final Left<L, R> left() {
	//		return (Left<L, R>) this;
	//	}
	//
	//	public final Right<L, R> right() {
	//		return (Right<L, R>) this;
	//	}

	//	public final <X> Either<L, X> mapRight(Function<R, X> f) {
	//		if (isRight()) {
	//			return new Right<>(f.applyPipelineStages(getRight()));
	//		} else {
	//			return (Either<L, X>) this;
	//		}
	//	}
	//
	//	public final void ifRight(Consumer<R> f) {
	//		if (isRight()) {
	//			f.accept(getRight());
	//		}
	//	}
	//
	//	public final <X> Either<L, X> flatMapR(Function<R, Either<L, X>> f) {
	//		if (isRight()) {
	//			return f.applyPipelineStages(getRight());
	//		} else {
	//			return (Either<L, X>) this;
	//		}
	//	}
	//
	public final <R> R fold(Function<T, R> success, BiFunction<HttpResponse, Throwable, R> fail) {
		if (isSuccess())
			return success.apply(getSuccess());
		else {
			return fail.apply(getFail(), getFailCause());
		}

	}
	//
	//	public final <LL, RR> Either<LL, RR> flatMap(Function<L, Either<LL, RR>> left, Function<R, Either<LL, RR>> right) {
	//		if (isRight())
	//			return right.applyPipelineStages(getRight());
	//		else {
	//			return left.applyPipelineStages(getLeft());
	//		}
	//
	//	}

	@EqualsAndHashCode
	@ToString
	static class Success<T extends HttpMessage> extends StageResult<T> {
		private final T value;

		private Success(T success) {
			value = success;
		}

		@Override public boolean isSuccess() {
			return true;
		}

		@Override public boolean isFail() {
			return false;
		}

		@Override public T getSuccess() {
			return value;
		}

		@Override public HttpResponse getFail() {
			throw new NoSuchElementException("No value present");
		}

		@Override public Throwable getFailCause() {
			throw new NoSuchElementException("No value present");
		}

		//		public <X> Left<X, R> map(Function<L, X> map) {
		//			return (Left<X, R>) this;
		//		}
		//
		//		public L orElse(Supplier<L> supplier) {
		//			return (value != null) ? value : supplier.get();
		//		}

	}

	@EqualsAndHashCode
	@ToString
	static class Fail<T extends HttpMessage> extends StageResult<T> {
		private final HttpResponse value;
		private Throwable cause ;



		private Fail(HttpResponse fail, Throwable cause) {
			this.value = fail;
			this.cause = cause;
		}

		@Override public boolean isSuccess() {
			return false;
		}

		@Override public boolean isFail() {
			return true;
		}

		@Override public T getSuccess() {
			throw new NoSuchElementException("No value present");

		}

		@Override public HttpResponse getFail() {
			return value;
		}

		@Override public Throwable getFailCause() {
			return cause;
		}

		//		public <X> Left<X, R> map(Function<L, X> map) {
		//			return (Left<X, R>) this;
		//		}
		//
		//		public L orElse(Supplier<L> supplier) {
		//			return (value != null) ? value : supplier.get();
		//		}

	}

}
