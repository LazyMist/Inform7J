package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

public record Omittable(TokenPattern pattern, Comparator<Result> order) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        return Stream.concat(pattern.matches(src), Stream.of(Result.EMPTY)).sorted(order);
    }
    
    @Override
    public TokenPattern clearCapture() {
        TokenPattern n = pattern.clearCapture();
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Omittable(n, order);
    }
    
    @Override
    public TokenPattern omittable(Comparator<Result> order) {
        if(order == this.order) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Omittable(pattern, this.order.thenComparing(order));
    }
    
    @Override
    public TokenPattern loop(Comparator<Result> order) {
        return pattern.loop(order).omittable(this.order);
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        TokenPattern n = pattern.replace(replacer);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Omittable(n, order);
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        TokenPattern n = pattern.replaceCaptures(previous);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Omittable(n, order);
    }
}
