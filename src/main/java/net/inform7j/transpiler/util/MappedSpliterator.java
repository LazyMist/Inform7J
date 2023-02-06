package net.inform7j.transpiler.util;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

public record MappedSpliterator<K, V>(Spliterator<? extends K> base, Function<? super K, ? extends V> mapping)
    implements Spliterator<V> {
    @Override
    public int characteristics() {
        return base.characteristics();
    }
    
    @Override
    public long estimateSize() {
        return base.estimateSize();
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super V> con) {
        return base.tryAdvance(c -> con.accept(mapping.apply(c)));
    }
    
    @Override
    public Spliterator<V> trySplit() {
        return new MappedSpliterator<>(base.trySplit(), mapping);
    }
    
}
