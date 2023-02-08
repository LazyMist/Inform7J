package net.inform7j.transpiler.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.pattern.End;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Patterns {
    public static final String SPECIAL_CHARS = ".,(){}";
    public static final TokenPattern AN = new Single(new TokenPredicate(Pattern.compile(
        "an?",
        Pattern.CASE_INSENSITIVE
    )));
    public static final TokenPattern HAS = new Single(new TokenPredicate(Pattern.compile(
        "has|have",
        Pattern.CASE_INSENSITIVE
    )));
    public static final TokenPattern IS = new Single(new TokenPredicate(Pattern.compile(
        "is|are",
        Pattern.CASE_INSENSITIVE
    )));
    public static final TokenPattern PLURAL_WORD = new Single(new TokenPredicate(Pattern.compile(
        ".+s",
        Pattern.CASE_INSENSITIVE
    )));
    public static final TokenPattern ENDMARKER = new Single(TokenPredicate.END_MARKER).or(End.PATTERN);
    public static final TokenPattern ENDLINE = End.PATTERN.or("\n");
    public static final TokenPattern WORD_LOOP = Single.WORD.loop(true);
    public static final TokenPattern WORD_LOOP_GREEDY = Single.WORD.loop();
    public static final TokenPattern IDENTIFIER_TOKEN = Single.WORD.or(new Single(new TokenPredicate(
        Token.Type.PUNCTUATION,
        s -> !SPECIAL_CHARS.contains(s)
    )));
    public static final TokenPattern IDENTIFIER_LOOP = IDENTIFIER_TOKEN.loop(true);
    public static final TokenPattern IDENTIFIER_LOOP_GREEDY = IDENTIFIER_TOKEN.loop();
    public static final TokenPattern NOT_ENDMARKER = new Single(TokenPredicate.END_MARKER.negate());
    public static final TokenPattern NOT_ENDMARKER_LOOP = NOT_ENDMARKER.loop(true);
    public static final TokenPattern NOT_ENDMARKER_LOOP_GREEDY = NOT_ENDMARKER.loop();
    public static final TokenPattern BRACE_OPEN = new Single(TokenPredicate.CURLY_OPEN);
    public static final TokenPattern BRACE_CLOSE = new Single(TokenPredicate.CURLY_CLOSE);
    private static TokenPattern brace(TokenPattern pattern) {
        return BRACE_OPEN.concat(pattern).concat(BRACE_CLOSE);
    }
    private static final Map<TokenPattern, TokenPattern> BRACED = new HashMap<>();
    public static TokenPattern braceExpression(TokenPattern pattern) {
        return BRACED.computeIfAbsent(pattern, Patterns::brace);
    }
}
