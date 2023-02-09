package net.inform7j.tests;

import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.EnumParserProvider;
import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumPatternTest {
    static final String TEST1 = "A situation can be resolved or unresolved.", TEST2 = TEST1 + "\nA situation is usually unresolved.";
    
    @Test
    void test() {
        TokenPattern pat = ServiceLoader.load(CombinedParser.Provider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(EnumParserProvider.class::isInstance)
            .map(EnumParserProvider.class::cast)
            .findFirst()
            .orElseThrow()
            .get()
            .skip(1)
            .findFirst()
            .orElseThrow()
            .pattern();
        TokenString src1 = new TokenString(Token.Generator.parseLiteral(TEST1)), src2 = new TokenString(Token.Generator.parseLiteral(
            TEST2));
        Optional<Result> r = pat.matches(src1).findFirst();
        assertTrue(r.isPresent());
        assertEquals(src1.length(), r.get().matchLength());
        r = pat.matches(src2).findFirst();
        assertTrue(r.isPresent());
        assertEquals(src1.length(), r.get().matchLength());
    }
    
}
