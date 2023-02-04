package net.inform7j.transpiler.util.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface FunctionExt<T, R> extends Function<T, R> {
	public default SupplierExt<R> compose(Supplier<? extends T> sup) {
		SupplierExt<? extends T> ext = (sup instanceof SupplierExt<? extends T> s) ? s : sup::get;
		return ext.map(this);
	}
	public default ConsumerExt<T> andThen(Consumer<? super R> sup) {
		ConsumerExt<? super R> ext = (sup instanceof ConsumerExt<? super R> s) ? s : sup::accept;
		return ext.compose(this);
	}
}
