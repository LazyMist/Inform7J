package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public record Capture(TokenPattern pattern, Set<String> name) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        return pattern.matches(src).map(r -> r.mergeCapture(name, src.substring(0, r.matchLength())));
    }
    
    @Override
    public TokenPattern clearCapture() {
        return pattern.clearCapture();
    }
    
    @Override
    public TokenPattern capture(Collection<String> name) {
        Set<String> n = new HashSet<>(this.name);
        n.addAll(name);
        return new net.inform7j.transpiler.tokenizer.pattern.Capture(pattern, Collections.unmodifiableSet(n));
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        TokenPattern n = pattern.replace(replacer);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Capture(n, name);
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        TokenPattern n = pattern.replaceCaptures(previous);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Capture(n, name);
    }
}
