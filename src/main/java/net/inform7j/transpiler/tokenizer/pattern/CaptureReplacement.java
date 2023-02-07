package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public record CaptureReplacement(
    String capture,
    Function<? super TokenString, ? extends TokenPattern> replace,
    boolean cc
)
    implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        throw new IllegalStateException("Trying to evaluate unreplaced capture replacement pattern: " + capture);
    }
    
    @Override
    public TokenPattern clearCapture() {
        if(cc) return this;
        return new net.inform7j.transpiler.tokenizer.pattern.CaptureReplacement(capture, replace, true);
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        return this;
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        Optional<TokenPattern> pat = previous.capOpt(capture).map(replace);
        if(cc) pat = pat.map(TokenPattern::clearCapture);
        return pat.orElse(this);
    }
}
