package net.inform7j.transpiler.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BiFunctionExt<T, U, R> extends BiFunction<T, U, R> {
	public default Function<T, R> fill2(Supplier<? extends U> u) {
		return t -> apply(t, u.get());
	}
	
	public default Function<T, R> fill2(U u) {
		return fill2(() -> u);
	}
	
	public default Function<U, R> fill(Supplier<? extends T> t) {
		return u -> apply(t.get(), u);
	}
	
	public default Function<U, R> fill(T t) {
		return fill(() -> t);
	}
}
