package net.inform7j.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import net.inform7j.transpiler.tokenizer.TokenString;
import org.junit.jupiter.api.Test;

import net.inform7j.transpiler.language.impl.deferring.DeferringEnum;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPattern.Result;

class EnumPatternTest {
	static final String TEST1 = "A situation can be resolved or unresolved.", TEST2 = TEST1 + " A situation is usually unresolved.\n";

	@Test
	void test() {
		TokenPattern pat = DeferringEnum.PARSERS.get(1).pattern();
		TokenString src1 = new TokenString(Token.Generator.parseLiteral(TEST1)), src2 = new TokenString(Token.Generator.parseLiteral(TEST2));
		Optional<Result> r = pat.matches(src1).findFirst();
		assertTrue(r.isPresent());
		assertEquals(src1.length(), r.get().matchLength());
		r = pat.matches(src2).findFirst();
		assertTrue(r.isPresent());
		assertEquals(src1.length(), r.get().matchLength());
	}

}
