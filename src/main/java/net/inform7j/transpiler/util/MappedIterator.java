package net.inform7j.transpiler.util;

import java.util.Iterator;
import java.util.function.Function;

public record MappedIterator<K, V>(Iterator<? extends K> base, Function<? super K, ? extends V> mapping)
    implements Iterator<V> {
    @Override
    public boolean hasNext() {
        return base.hasNext();
    }
    
    @Override
    public V next() {
        return mapping.apply(base.next());
    }
    
}
