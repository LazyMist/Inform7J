package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record Single(Predicate<? super Token> pred) implements TokenPattern {
    public static final net.inform7j.transpiler.tokenizer.pattern.Single STRING = new net.inform7j.transpiler.tokenizer.pattern.Single(
        r -> r.type() == Token.Type.STRING),
        TAB = new net.inform7j.transpiler.tokenizer.pattern.Single(r -> r.type() == Token.Type.TAB),
        WORD = new net.inform7j.transpiler.tokenizer.pattern.Single(r -> r.type() == Token.Type.WORD),
        PUNCTUATION = new net.inform7j.transpiler.tokenizer.pattern.Single(r -> r.type() == Token.Type.PUNCTUATION);
    @Override
    public Stream<Result> matches(TokenString src) {
        if(src.isEmpty() || !pred.test(src.get(0))) return Stream.empty();
        return Stream.of(new Result(1));
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
