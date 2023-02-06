package net.inform7j.transpiler.tokenizer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public record TokenPredicate(EnumSet<Token.Type> types, Predicate<? super String> words) implements Predicate<Token> {
    public static final Predicate<Token> IS_WHITESPACE = new TokenPredicate(EnumSet.of(
        Token.Type.INDENT,
        Token.Type.NEWLINE,
        Token.Type.TAB
    )),
        COMMENT = new TokenPredicate(Token.Type.COMMENT),
        NORMALLY_IGNORED = new TokenPredicate(EnumSet.of(Token.Type.COMMENT, Token.Type.TAB)),
        NEWLINE = new TokenPredicate(Token.Type.NEWLINE),
        END_MARKER = new TokenPredicate(Token.Type.PUNCTUATION, ".", ":", ";").or(TokenPredicate.NEWLINE),
        PAREN_OPEN = new TokenPredicate(Token.Type.PUNCTUATION, "("),
        PAREN_CLOSE = new TokenPredicate(Token.Type.PUNCTUATION, ")"),
        CURLY_OPEN = new TokenPredicate(Token.Type.PUNCTUATION, "{"),
        CURLY_CLOSE = new TokenPredicate(Token.Type.PUNCTUATION, "}"),
        COMMA = new TokenPredicate(Token.Type.PUNCTUATION, ","),
        MINUS = new TokenPredicate(Token.Type.PUNCTUATION, "-"),
        SLASH = new TokenPredicate(Token.Type.PUNCTUATION, "/"),
        COLON = new TokenPredicate(Token.Type.PUNCTUATION, ":");
    public static Predicate<Token.SourcedToken> wrap(Predicate<? super Token> pred) {
        return x -> pred.test(x.tok());
    }
    public TokenPredicate(EnumSet<Token.Type> types) {
        this(types, x -> true);
    }
    public TokenPredicate(EnumSet<Token.Type> types, String... words) {
        this(types, Arrays.asList(words)::contains);
    }
    public TokenPredicate(EnumSet<Token.Type> types, Pattern words) {
        this(types, words.asMatchPredicate());
    }
    public TokenPredicate(Token.Type types, Predicate<? super String> words) {
        this(EnumSet.of(types), words);
    }
    public TokenPredicate(Token.Type types) {
        this(EnumSet.of(types));
    }
    public TokenPredicate(Token.Type types, String... words) {
        this(EnumSet.of(types), words);
    }
    public TokenPredicate(Token.Type types, Pattern words) {
        this(EnumSet.of(types), words);
    }
    public TokenPredicate(Predicate<? super String> words) {
        this(Token.Type.WORD, words);
    }
    public TokenPredicate(String... words) {
        this(Token.Type.WORD, words);
    }
    public TokenPredicate(Pattern words) {
        this(Token.Type.WORD, words);
    }
    public TokenPredicate(Predicate<? super Token.Type> types, Predicate<? super String> words) {
        this(EnumSet.allOf(Token.Type.class), words);
        this.types.removeIf(types.negate());
    }
    public TokenPredicate(Predicate<? super Token.Type> types, String... words) {
        this(EnumSet.allOf(Token.Type.class), words);
        this.types.removeIf(types.negate());
    }
    public TokenPredicate(Predicate<? super Token.Type> types, Pattern words) {
        this(EnumSet.allOf(Token.Type.class), words);
        this.types.removeIf(types.negate());
    }
    @Override
    public boolean test(Token t) {
        return types.contains(t.type()) && words.test(t.content());
    }
}