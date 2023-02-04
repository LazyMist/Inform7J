package net.inform7j.transpiler.util;

import java.util.ListIterator;
import java.util.function.Function;

public record MappedListIterator<K, V>(ListIterator<? extends K> base, Function<? super K,? extends V> mapping) implements ListIterator<V> {
	@Override
	public void add(V arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		return base.hasNext();
	}

	@Override
	public boolean hasPrevious() {
		return base.hasPrevious();
	}

	@Override
	public V next() {
		return mapping.apply(base.next());
	}

	@Override
	public int nextIndex() {
		return base.nextIndex();
	}

	@Override
	public V previous() {
		return mapping.apply(base.previous());
	}

	@Override
	public int previousIndex() {
		return base.previousIndex();
	}

	@Override
	public void remove() {
		base.remove();
	}

	@Override
	public void set(V arg0) {
		throw new UnsupportedOperationException();
	}

}
