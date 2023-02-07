package net.inform7j.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.tokenizer.pattern.Concat;
import net.inform7j.transpiler.tokenizer.pattern.Lookahead;
import net.inform7j.transpiler.tokenizer.pattern.Single;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TokenPatternTest {
    private static final String[] TESTSTRINGS = {"a word", "another word", "nothing"};
    private static List<TokenString> INPUTS = null;
    @BeforeAll
    static void setup() {
        INPUTS = Arrays.stream(TESTSTRINGS)
            .map(Token.Generator::parseLiteral)
            .map(TokenString::new).toList();
    }
    @AfterAll
    static void teardown() {
        INPUTS = null;
    }
    @Test
    void testSingle() {
        Single pat = new Single(new TokenPredicate("nothing"));
        for(int i = 0; i < TESTSTRINGS.length; i++) {
            switch(i) {
            case 2:
                assertIterableEquals(List.of(new Result(1)), pat.matches(INPUTS.get(i)).toList());
                break;
            default:
                assertIterableEquals(Collections::emptyIterator, pat.matches(INPUTS.get(i)).toList());
                break;
            }
        }
    }
    
    @Test
    void testConcat() {
        for(int i = 0; i < TESTSTRINGS.length; i++) {
            Concat pat = new Concat(INPUTS.get(i)
                .stream()
                .<Predicate<Token>>map(t -> t::equals)
                .map(Single::new)
                .toList());
            for(int j = 0; j < TESTSTRINGS.length; j++) {
				if(i == j) {
					assertIterableEquals(
						List.of(new Result(INPUTS.get(i).length())),
						pat.matches(INPUTS.get(j)).toList()
					);
				} else {
					assertIterableEquals(Collections::emptyIterator, pat.matches(INPUTS.get(j)).toList());
				}
            }
        }
    }
    
    @Test
    void testLookahead() {
        Lookahead pat = new Lookahead(
            new Single(new TokenPredicate("another")),
            true
        );
        for(int i = 0; i < TESTSTRINGS.length; i++) {
            switch(i) {
            case 1:
                assertIterableEquals(Collections::emptyIterator, pat.matches(INPUTS.get(i)).toList());
                break;
            default:
                assertIterableEquals(List.of(Result.EMPTY), pat.matches(INPUTS.get(i)).toList());
                break;
            }
        }
    }
}
