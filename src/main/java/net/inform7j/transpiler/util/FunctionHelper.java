package net.inform7j.transpiler.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FunctionHelper {
    public static <T> Runnable transfer(Supplier<? extends T> sup, Consumer<? super T> con) {
        return () -> con.accept(sup.get());
    }
    public static <T, R> Supplier<R> map(Supplier<? extends T> sup, Function<T, R> fun) {
        return () -> fun.apply(sup.get());
    }
    public static <T, R> Consumer<T> map(Function<T, R> fun, Consumer<? super R> con) {
        return t -> con.accept(fun.apply(t));
    }
    public static <T, U, R> Function<T, R> fill2(BiFunction<T, U, R> f, Supplier<? extends U> u) {
        return t -> f.apply(t, u.get());
    }
    
    public static <T, U, R> Function<T, R> fill2(BiFunction<T, U, R> f, U u) {
        return t -> f.apply(t, u);
    }
    
    public static <T, U, R> Function<U, R> fill(BiFunction<T, U, R> f, Supplier<? extends T> t) {
        return u -> f.apply(t.get(), u);
    }
    
    public static <T, U, R> Function<U, R> fill(BiFunction<T, U, R> f, T t) {
        return u -> f.apply(t, u);
    }
}
