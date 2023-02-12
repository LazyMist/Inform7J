package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.function.Function;
import java.util.stream.Stream;

public record RepeatedCapture(String capture, boolean ignoreCase) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        throw new IllegalStateException("Trying to evaluate unreplaced capture repeat pattern: " + capture);
    }
    @Override
    public TokenPattern clearCapture() {
        return this;
    }
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        return this;
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        return new Conjunction(
            previous.capMulti(capture).stream()
                .map(ignoreCase ? TokenPattern::quoteIgnoreCase : TokenPattern::quote)
                .toList()
        );
    }
}
