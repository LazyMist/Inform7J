package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record Loop(TokenPattern pattern, Comparator<Result> order) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        List<Result> ret = new ArrayList<>();
        List<Result> fmatch = pattern.matches(src).toList();
        List<Result> empty = fmatch.stream().filter(Result::isEmpty).toList();
        fmatch.stream().filter(Result::notEmpty).forEach(ret::add);
        for(int i = 0; i < ret.size(); i++) {
            final Result res = ret.get(i);
            pattern.matches(src.substring(res.matchLength(), src.length()))
                .filter(Result::notEmpty)
                .map(res::concat)
                .forEachOrdered(ret::add);
        }
        ret.addAll(0, empty);
        return ret.stream().sorted(order);
    }
    
    @Override
    public TokenPattern clearCapture() {
        TokenPattern n = pattern.clearCapture();
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Loop(n, order);
    }
    
    @Override
    public TokenPattern loop(Comparator<Result> order) {
        if(this.order == order) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Loop(pattern, this.order.thenComparing(order));
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        TokenPattern n = pattern.replace(replacer);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Loop(n, order);
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        TokenPattern n = pattern.replaceCaptures(previous);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Loop(n, order);
    }
}
