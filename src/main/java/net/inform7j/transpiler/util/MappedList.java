package net.inform7j.transpiler.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record MappedList<K, V> (List<? extends K> backing, Function<? super K,? extends V> mapping) implements List<V> {
	public <E> MappedList<K,E> map(Function<? super V,? extends E> map2) {
		return new MappedList<>(backing, mapping.andThen(map2));
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
		return backing.stream().map(mapping).anyMatch(arg0 == null ? s->s==null : arg0::equals);
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

	@Override
	public void add(int arg0, V arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends V> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(int arg0) {
		return mapping.apply(backing.get(arg0));
	}

	@Override
	public int indexOf(Object arg0) {
		return (int)stream().takeWhile(arg0 == null ? s -> s!=null : s -> !arg0.equals(s)).count();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<V> listIterator() {
		return new MappedListIterator<>(backing.listIterator(), mapping);
	}

	@Override
	public ListIterator<V> listIterator(int arg0) {
		return new MappedListIterator<>(backing.listIterator(arg0), mapping);
	}

	@Override
	public V remove(int arg0) {
		return mapping.apply(backing.remove(arg0));
	}

	@Override
	public V set(int arg0, V arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<V> subList(int arg0, int arg1) {
		return new MappedList<>(backing.subList(arg0, arg1), mapping);
	}
}
