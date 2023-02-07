package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.function.Function;
import java.util.stream.Stream;

public enum End implements TokenPattern {
    PATTERN;
    @Override
    public Stream<Result> matches(TokenString src) {
        if(src.isEmpty()) return java.util.stream.Stream.of(net.inform7j.transpiler.tokenizer.Result.EMPTY);
        return java.util.stream.Stream.empty();
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
        return this;
    }
}
