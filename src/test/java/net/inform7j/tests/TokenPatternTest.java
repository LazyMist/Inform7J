package net.inform7j.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import net.inform7j.transpiler.tokenizer.TokenString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;

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
		TokenPattern.Single pat = new TokenPattern.Single(new TokenPredicate("nothing"));
		for(int i=0; i<TESTSTRINGS.length; i++) {
			switch(i) {
			case 2:
				assertIterableEquals(List.of(new TokenPattern.Result(1)), pat.matches(INPUTS.get(i)).toList());
				break;
			default:
				assertIterableEquals(Collections::emptyIterator, pat.matches(INPUTS.get(i)).toList());
				break;
			}
		}
	}
	
	@Test
	void testConcat() {
		for(int i=0; i<TESTSTRINGS.length; i++) {
			TokenPattern.Concat pat = new TokenPattern.Concat(INPUTS.get(i).stream().<Predicate<Token>>map(t -> t::equals).map(TokenPattern.Single::new).toList());
			for(int j=0; j<TESTSTRINGS.length; j++) {
				if(i == j) assertIterableEquals(List.of(new TokenPattern.Result(INPUTS.get(i).length())), pat.matches(INPUTS.get(j)).toList());
				else assertIterableEquals(Collections::emptyIterator, pat.matches(INPUTS.get(j)).toList());
			}
		}
	}
	
	@Test
	void testLookahead() {
		TokenPattern.Lookahead pat = new TokenPattern.Lookahead(new TokenPattern.Single(new TokenPredicate("another")), true);
		for(int i=0; i<TESTSTRINGS.length; i++) {
			switch(i) {
			case 1:
				assertIterableEquals(Collections::emptyIterator, pat.matches(INPUTS.get(i)).toList());
				break;
			default:
				assertIterableEquals(List.of(TokenPattern.Result.EMPTY), pat.matches(INPUTS.get(i)).toList());
				break;
			}
		}
	}
}
