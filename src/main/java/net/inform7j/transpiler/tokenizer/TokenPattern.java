package net.inform7j.transpiler.tokenizer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.inform7j.transpiler.tokenizer.Token.SourcedToken;
import net.inform7j.transpiler.tokenizer.pattern.*;

public interface TokenPattern {
    
    Stream<Result> matches(TokenString string);
    
    default Stream<Result> matchesWrapped(List<SourcedToken> src) {
        return matches(new TokenString(src.stream().map(SourcedToken::tok)));
    }
    
    default TokenPattern loop(Comparator<Result> order) {
        return new Loop(this, order);
    }
    
    default TokenPattern loop(boolean min) {
        return loop(min ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }
    
    default TokenPattern loop() {
        return loop(false);
    }
    
    default TokenPattern omittable(Comparator<Result> order) {
        return new Omittable(this, order);
    }
    
    default TokenPattern omittable(boolean min) {
        return omittable(min ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }
    
    default TokenPattern omittable() {
        return omittable(false);
    }
    
    default TokenPattern concat(TokenPattern p) {
        return new Concat(List.of(this, p));
    }
    
    default TokenPattern concatReplacement(String key) {
        return concat(new Replacement(key, false));
    }
    
    default TokenPattern concatNoCapReplacement(String key) {
        return concat(new Replacement(key, true));
    }
    
    default TokenPattern concat(String s) {
        return concat(quote(s));
    }
    default TokenPattern concatIgnoreCase(String s) {
        return concat(quoteIgnoreCase(s));
    }
    
    default TokenPattern concatOptional(String s) {
        return concat(quote(s).omittable());
    }
    default TokenPattern concatOptionalIgnoreCase(String s) {
        return concat(quoteIgnoreCase(s).omittable());
    }
    
    default TokenPattern concatConjunction(Collection<? extends TokenPattern> patterns) {
        return concat(new Conjunction(patterns));
    }
    
    default TokenPattern or(TokenPattern p) {
        return new Conjunction(List.of(this, p));
    }
    
    default TokenPattern or(String s) {
        return or(quote(s));
    }
    
    default TokenPattern orIgnoreCase(String s) {
        return or(quoteIgnoreCase(s));
    }
    
    default TokenPattern capture(Collection<String> name) {
        return new Capture(this, Set.copyOf(name));
    }
    
    default TokenPattern capture(String... name) {
        return capture(Arrays.asList(name));
    }
    
    default TokenPattern lookahead(boolean invert) {
        return new Lookahead(this, invert);
    }
    
    static TokenPattern quote(Stream<Token> tokens, BiPredicate<Token, Token> eq) {
        return new Concat(tokens.map(t -> (Predicate<Token>) (x -> eq.test(t, x))).map(Single::new).toList());
    }
    
    static TokenPattern quote(Stream<Token> tokens) {
        return quote(tokens, Token::equals);
    }
    
    static TokenPattern quoteIgnoreCase(Stream<Token> tokens) {
        return quote(tokens, Token::equalsIgnoreCase);
    }
    
    static TokenPattern quote(TokenString tokens, BiPredicate<Token, Token> eq) {
        return new Concat(tokens.stream().map(t -> new Single(x -> eq.test(t, x))).toList());
    }
    
    static TokenPattern quote(TokenString tokens) {
        return quote(tokens, Token::equals);
    }
    
    static TokenPattern quoteIgnoreCase(TokenString tokens) {
        return quote(tokens, Token::equalsIgnoreCase);
    }
    
    static TokenPattern quote(String tokens) {
        return quote(Token.Generator.parseLiteral(tokens));
    }
    
    static TokenPattern quoteIgnoreCase(String tokens) {
        return quoteIgnoreCase(Token.Generator.parseLiteral(tokens));
    }
    
    TokenPattern clearCapture();
    
    TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer);
    
    TokenPattern replaceCaptures(Result previous);
    
    default Stream<TokenString> splitAsStream(TokenString src) {
        Stream.Builder<TokenString> out = Stream.builder();
        L:
        while(!src.isEmpty()) {
            for(int i = 0; i < src.length(); i++) {
                TokenString sub = src.substring(i);
                Optional<Result> res = matches(sub).findFirst();
                if(res.isPresent()) {
                    if(i > 0) out.accept(src.substring(0, i));
                    src = sub.substring(res.get().matchLength(), sub.length());
                    continue L;
                }
            }
            out.accept(src);
            break;
        }
        return out.build();
    }
}
