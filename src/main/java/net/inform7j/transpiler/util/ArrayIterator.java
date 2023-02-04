package net.inform7j.transpiler.util;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T> {
	private final T[] array;
	private int idx = 0;
	public ArrayIterator(T[] array) {
		this.array = array;
	}

	@Override
	public boolean hasNext() {
		return idx<array.length;
	}

	@Override
	public T next() {
		return array[idx++];
	}

}
