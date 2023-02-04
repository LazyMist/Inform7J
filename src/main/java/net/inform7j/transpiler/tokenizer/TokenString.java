package net.inform7j.transpiler.tokenizer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.inform7j.transpiler.util.ArrayIterator;

public final class TokenString implements Iterable<Token> {
	public static final TokenString EMPTY = new TokenString();
	
	private final Token[] tokens;
	public TokenString(Token ...tokens) {
		this.tokens = Arrays.copyOf(tokens, tokens.length);
	}
	
	public TokenString(Token[] tokens, int from, int to) {
		this.tokens = Arrays.copyOfRange(tokens, from, to);
	}
	
	public TokenString(Stream<Token> tokens) {
		this.tokens = tokens.toArray(Token[]::new);
	}
	
	public TokenString(List<Token> tokens) {
		this(tokens.stream());
	}
	
	public TokenString(String literal) {
		this(Token.Generator.parseLiteral(literal));
	}
	
	public boolean isEmpty() {
		return tokens.length == 0;
	}
	
	public int length() {
		return tokens.length;
	}
	
	public Token get(int idx) {
		if(idx<0 || idx>=tokens.length) throw new IndexOutOfBoundsException("requested token "+idx+" out of "+tokens.length+" tokens");
		return tokens[idx];
	}
	public TokenString pluralize() {
		Token fin = tokens[tokens.length-1];
		if(fin.type() != Token.Type.WORD) return this;
		fin = new Token(Token.Type.WORD, fin.content()+"s");
		final Token[] arr = Arrays.copyOf(tokens, tokens.length);
		arr[tokens.length-1] = fin;
		return new TokenString(arr);
	}
	
	public TokenString substring(int from, int to) {
		return new TokenString(tokens, from, to);
	}
	
	public TokenString substring(int from) {
		return new TokenString(tokens, from, tokens.length);
	}
	
	public Stream<Token> stream() {
		return Arrays.stream(tokens);
	}
	
	public TokenString concat(TokenString ...others) {
		return new TokenString(
			Stream.concat(
			    Stream.of(this),
			    Arrays.stream(others)
		    ).flatMap(TokenString::stream)
		);
	}
	
	public boolean contains(Token token) {
		return stream().anyMatch(token::equals);
	}
	
	@Override
	public Spliterator<Token> spliterator() {
		return Arrays.spliterator(tokens);
	}

	@Override
	public Iterator<Token> iterator() {
		return new ArrayIterator<>(tokens);
	}
	
	@Override
	public String toString() {
		return stream().map(Token::fmtContent).collect(Collectors.joining(" "));
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o instanceof TokenString tok) {
			return Arrays.equals(tokens, tok.tokens);
		}
		return false;
	}
	
	public boolean equalsIgnoreCase(TokenString o) {
		if(o.tokens.length != tokens.length) return false;
		for(int i=0;i<tokens.length;i++) {
			if(!tokens[i].equalsIgnoreCase(o.tokens[i])) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(tokens);
	}
}
