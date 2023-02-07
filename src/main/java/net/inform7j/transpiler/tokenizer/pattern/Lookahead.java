package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.function.Function;
import java.util.stream.Stream;

public record Lookahead(TokenPattern pattern, boolean invert) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        if(pattern.matches(src).findAny().isPresent() != invert) return Stream.of(Result.EMPTY);
        return Stream.empty();
    }
    
    @Override
    public TokenPattern clearCapture() {
        TokenPattern n = pattern.clearCapture();
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Lookahead(n, invert);
    }
    
    @Override
    public TokenPattern lookahead(boolean invert) {
        if(invert) return new net.inform7j.transpiler.tokenizer.pattern.Lookahead(pattern, !this.invert);
        return this;
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        TokenPattern n = pattern.replace(replacer);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Lookahead(n, invert);
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        TokenPattern n = pattern.replaceCaptures(previous);
        if(n == pattern) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.Lookahead(n, invert);
    }
}
