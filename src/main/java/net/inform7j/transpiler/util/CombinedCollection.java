package net.inform7j.transpiler.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public record CombinedCollection<E>(Collection<? extends Collection<E>> backing) implements Collection<E> {
    @SafeVarargs
    public CombinedCollection(Collection<E>... collections) {
        this(Arrays.asList(collections));
    }
    
    @Override
    public boolean add(E arg0) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(Collection<? extends E> arg0) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean contains(Object arg0) {
        return backing.stream().anyMatch(c -> c.contains(arg0));
    }
    
    @Override
    public boolean containsAll(Collection<?> arg0) {
        return arg0.stream().allMatch(this::contains);
    }
    
    @Override
    public boolean isEmpty() {
        return backing.stream().allMatch(Collection::isEmpty);
    }
    
    @Override
    public Iterator<E> iterator() {
        return stream().iterator();
    }
    
    @Override
    public Stream<E> parallelStream() {
        return backing.parallelStream().flatMap(Collection::parallelStream);
    }
    
    @Override
    public Spliterator<E> spliterator() {
        return stream().spliterator();
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
        return backing.stream().mapToInt(Collection::size).sum();
    }
    
    @Override
    public Stream<E> stream() {
        return backing.stream().flatMap(Collection::stream);
    }
    
    @Override
    public Object[] toArray() {
        return stream().toArray();
    }
    
    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return stream().toArray(generator);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] arg0) {
        return toArray(i -> i >= arg0.length ? arg0 : (T[]) Array.newInstance(arg0.getClass().componentType(), i));
    }
    
}
