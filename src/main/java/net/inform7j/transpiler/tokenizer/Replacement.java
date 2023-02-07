package net.inform7j.transpiler.tokenizer;

import java.util.function.Function;
import java.util.stream.Stream;

public record Replacement(String name, boolean cc) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        throw new IllegalStateException("Trying to evaluate unreplaced replacement pattern: " + name);
    }
    
    @Override
    public TokenPattern clearCapture() {
        if(cc) return this;
        return new net.inform7j.transpiler.tokenizer.Replacement(name, true);
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        TokenPattern pat = replacer.apply(name);
        if(pat == null) {
            pat = this;
        } else if(cc) pat = pat.clearCapture();
        return pat;
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        return this;
    }
}
