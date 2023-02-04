package net.inform7j.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.Token.SourcedToken;

class TokenGeneratorTest {
	private static final String TESTSTRING = """
			a word, another word, 2 "strings" "are tested".
			[this is a comment]	tabulator
				[this is an empty line]
				indent
			[this is another empty line]
			""";
	private static List<Token> TOKENSTRING1 = null;
	private static List<Token> TOKENSTRING2 = null;
	
	@BeforeAll
	static void setup() {
		 TOKENSTRING1 = List.of(
			new Token(Token.Type.WORD, "a"),
			new Token(Token.Type.WORD, "word"),
			new Token(Token.Type.PUNCTUATION, ","),
			new Token(Token.Type.WORD, "another"),
			new Token(Token.Type.WORD, "word"),
			new Token(Token.Type.PUNCTUATION, ","),
			new Token(Token.Type.WORD, "2"),
			new Token(Token.Type.STRING, "\"strings\""),
			new Token(Token.Type.STRING, "\"are tested\""),
			new Token(Token.Type.PUNCTUATION, "."),
			new Token(Token.Type.NEWLINE, "\n"),
			new Token(Token.Type.COMMENT, "[this is a comment]"),
			new Token(Token.Type.INDENT, "\t"),
			new Token(Token.Type.WORD, "tabulator"),
			new Token(Token.Type.NEWLINE, "\n"),
			new Token(Token.Type.INDENT, "\t"),
			new Token(Token.Type.COMMENT, "[this is an empty line]"),
			new Token(Token.Type.NEWLINE, "\n"),
			new Token(Token.Type.INDENT, "\t"),
			new Token(Token.Type.WORD, "indent"),
			new Token(Token.Type.NEWLINE, "\n"),
			new Token(Token.Type.COMMENT, "[this is another empty line]"),
			new Token(Token.Type.NEWLINE, "\n")
		);
		TOKENSTRING2 = List.of(
			new Token(Token.Type.WORD, "a"),
			new Token(Token.Type.WORD, "word"),
			new Token(Token.Type.PUNCTUATION, ","),
			new Token(Token.Type.WORD, "another"),
			new Token(Token.Type.WORD, "word"),
			new Token(Token.Type.PUNCTUATION, ","),
			new Token(Token.Type.WORD, "2"),
			new Token(Token.Type.STRING, "\"strings\""),
			new Token(Token.Type.STRING, "\"are tested\""),
			new Token(Token.Type.PUNCTUATION, "."),
			new Token(Token.Type.NEWLINE, "\n"),
			new Token(Token.Type.INDENT, "\t"),
			new Token(Token.Type.WORD, "tabulator"),
			new Token(Token.Type.NEWLINE, "\n"),
			new Token(Token.Type.INDENT, "\t"),
			new Token(Token.Type.WORD, "indent"),
			new Token(Token.Type.NEWLINE, "\n")
		);
	}
	
	@AfterAll
	static void teardown() {
		TOKENSTRING1 = null;
		TOKENSTRING2 = null;
	}

	@Test
	void testGen() {
		List<Token> t = Token.Generator.parseLiteral(TESTSTRING).toList();
		assertIterableEquals(TOKENSTRING1, t);
	}
	
	@Test
	void testCr() {
		List<Token> t2 = new ArrayList<>(TOKENSTRING1.size());
		Token.CommentRemover cr = new Token.CommentRemover(s -> t2.add(s.tok()));
		TOKENSTRING1.stream().map(to -> new SourcedToken(to, 0, null, null)).forEachOrdered(cr);
		cr.run();
		assertIterableEquals(TOKENSTRING2, t2);
	}
}
