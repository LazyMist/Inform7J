package net.inform7j.transpiler.util;

import java.util.function.Function;
import java.util.function.Supplier;

@Deprecated
public class Lazy<T> implements Supplier<T> {
    private final Supplier<? extends T> sup;
    private T value;
    private boolean init;
    
    public Lazy(Supplier<? extends T> sup) {
        this.sup = sup;
        this.value = null;
        this.init = false;
    }
    
    public Lazy(T val) {
        this.sup = () -> val;
        this.value = val;
        this.init = true;
    }
    
    @Override
    public T get() {
        if(init) return value;
        synchronized(this) {
            if(init) return value;
            value = sup.get();
            init = true;
            return value;
        }
    }
    
    public <V> Lazy<V> map(Function<? super T, ? extends V> mapping) {
        return new Lazy<>(() -> mapping.apply(get()));
    }
}
