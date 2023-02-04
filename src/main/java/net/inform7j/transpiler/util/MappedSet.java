package net.inform7j.transpiler.util;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record MappedSet<K, V> (Set<? extends K> backing, Function<? super K,? extends V> mapping) implements Set<V> {
	public <E> MappedSet<K,E> map(Function<? super V,? extends E> map2) {
		return new MappedSet<>(backing, mapping.andThen(map2));
	}
	@Override
	public boolean add(V arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends V> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		backing.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return backing.stream().map(mapping).anyMatch(arg0 == null ? Objects::isNull : arg0::equals);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return arg0.stream().allMatch(this::contains);
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public Iterator<V> iterator() {
		return new MappedIterator<>(backing.iterator(), mapping);
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public Object[] toArray() {
		return backing.stream().map(mapping).toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] arg0) {
		return toArray(i -> arg0.length>=i ? arg0 : (T[])Array.newInstance(arg0.getClass().componentType(), i));
	}

	@Override
	public Stream<V> parallelStream() {
		return backing.parallelStream().map(mapping);
	}

	@Override
	public boolean removeIf(Predicate<? super V> filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Spliterator<V> spliterator() {
		return new MappedSpliterator<>(backing.spliterator(), mapping);
	}

	@Override
	public Stream<V> stream() {
		return backing.stream().map(mapping);
	}

	@Override
	public <T> T[] toArray(IntFunction<T[]> generator) {
		return backing.stream().map(mapping).toArray(generator);
	}
}
