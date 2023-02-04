package net.inform7j.transpiler.util;

import java.util.function.Function;

import net.inform7j.transpiler.util.function.SupplierExt;

@SuppressWarnings("deprecation")
public record LazyLookup<U, T>(U key, Lazy<T> lazy) implements SupplierExt<T> {
	public LazyLookup(U key, Function<? super U, ? extends T> lookup) {
		this(key, new Lazy<>(key).map(lookup));
	}
	
	public LazyLookup(U key, T result) {
		this(key, new Lazy<>(result));
	}

	@Override
	public T get() {
		return lazy.get();
	}

	@Override
	public <V> LazyLookup<U,V> map(Function<? super T, ? extends V> map) {
		return new LazyLookup<>(key, lazy.map(map));
	}
	
}
