package net.inform7j.transpiler.util.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ConsumerExt<T> extends Consumer<T> {
	public default <R> ConsumerExt<R> compose(Function<? super R, ? extends T> map) {
		return r -> accept(map.apply(r));
	}
	public default Runnable compose(Supplier<? extends T> sup) {
		return () -> accept(sup.get());
	}
}
