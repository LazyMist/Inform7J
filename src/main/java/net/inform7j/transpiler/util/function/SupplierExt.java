package net.inform7j.transpiler.util.function;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface SupplierExt<T> extends Supplier<T> {
	public default <V> SupplierExt<V> map(Function<? super T,? extends V> map) {
		return () -> map.apply(get());
	}
	
	public default <V,W> Function<V,W> mapFunc(BiFunction<? super T, ? super V, ? extends W> func) {
		return v -> func.apply(get(), v);
	}
	
	public default <V,W> Function<V,W> mapFunc2(BiFunction<? super V, ? super T, ? extends W> func) {
		return v -> func.apply(v, get());
	}
	
	public default Runnable transfer(Consumer<? super T> con) {
		return () -> con.accept(this.get());
	}
}
