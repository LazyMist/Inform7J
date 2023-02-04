package net.inform7j.transpiler.tokenizer;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.inform7j.Logging;
import net.inform7j.Logging.Severity;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.Statistics;

public record Token(Type type, String content) {
	public static String toString(Stream<Token> tokens) {
		return tokens.map(Token::fmtContent).collect(Collectors.joining(" "));
	}
	
	public static String toPattern(TokenString tokens) {
		return toPattern(tokens.stream());
	}
	
	public static String toPattern(Stream<Token> tokens) {
		return tokens.map(Token::content).map(Pattern::quote).collect(Collectors.joining(" *"));
	}
	
	public String fmtContent() {
		switch(type) {
		case INDENT:
			if("\t".equals(content())) return "[[]]";
			return content();
		case TAB:
			return "[[]]";
		default:
			return content();
		}
	}
	
	public boolean equalsIgnoreCase(Token other) {
		return type.equals(other.type) && content.equalsIgnoreCase(other.content);
	}
	
	public Token toLowerCase() {
		String lc = content.toLowerCase();
		if(lc.equals(content)) return this;
		return new Token(type, lc);
	}
	
	public Token toLowerCase(Locale l) {
		String lc = content.toLowerCase(l);
		if(lc.equals(content)) return this;
		return new Token(type, lc);
	}
	
	public Token normalize() {
		if(type.CASE_SENSITIVE) return this;
		return toLowerCase();
	}
	
	public static record SourcedToken(Token tok, long line, Path src, Source source) {
		public static String toString(List<SourcedToken> tok) {
			return Token.toString(tok.stream().map(SourcedToken::tok));
		}
	}
	
	public static enum Type {
		WORD(false),
		TAB(false),
		NEWLINE(false),
		INDENT(false),
		STRING(true),
		PUNCTUATION(true),
		COMMENT(true);
		public final boolean CASE_SENSITIVE;
		private Type(boolean caseSensitive) {
			this.CASE_SENSITIVE = caseSensitive;
		}
	}
	
 	public static class Generator implements IntConsumer, Runnable {
 		public static Stream<Token> parseLiteral(IntStream cp) {
 			Stream.Builder<SourcedToken> con = Stream.builder();
 			new Generator(null, null, con).absorbStream(cp);
 			return con.build().map(SourcedToken::tok);
 		}
 		
 		public static Stream<Token> parseLiteral(String s) {
 			return parseLiteral(s.codePoints());
 		}
 		
		private final Consumer<? super SourcedToken> output;
		private final Runnable oflush;
		private StringBuilder buff = new StringBuilder();
		private Type type = Type.NEWLINE;
		private Type old = type;
		private int commentDepth = 0;
		private long line = 1;
		private final Path src;
		private final Source source;
		public Generator(Path src, Source source, Consumer<? super SourcedToken> output, Runnable outputflush) {
			this.output = output;
			this.oflush = outputflush;
			this.src = src;
			this.source = source;
		}
		public Generator(Path src, Source source, Consumer<? super SourcedToken> output) {
			this(src, source, output, output instanceof Runnable r ? r : () -> {});
		}
		
		@Override
		public void run() {
			switch(type) {
			case COMMENT:
				Logging.log(Statistics.INCOMPLETE_EOF_LINE, "Unclosed comment");
				for(int i=0; i<commentDepth; i++) buff.append("]");
				output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
				buff = new StringBuilder();
				break;
			case INDENT:
			case NEWLINE:
			case PUNCTUATION:
			case TAB:
				break;
			case STRING:
				Logging.log(Statistics.INCOMPLETE_EOF_LINE, "Unclosed String");
				for(int i=0; i<commentDepth; i++) buff.append("]");
				buff.append('"');
				//$FALL-THROUGH$
			case WORD:
				output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
				buff = new StringBuilder();
				break;
			}
			type = Type.NEWLINE;
			oflush.run();
		}
		
		public void absorbStream(IntStream stream) {
			stream.forEachOrdered(this);
			run();
		}
		
		@Override
		public void accept(int codePoint) {
			Type oldOld = old;
			Type oldType = type;
			if(codePoint == '\n') {
				switch(type) {
				case INDENT:
				case NEWLINE:
				case PUNCTUATION:
				case TAB:
					type = Type.NEWLINE;
					output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
					break;
				case COMMENT:
				case STRING:
					buff.appendCodePoint(codePoint);
					break;
				case WORD:
					output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
					buff = new StringBuilder();
					type = Type.NEWLINE;
					output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
					break;
				}
				line++;
			} else if(codePoint == '\t') {
				switch(type) {
				case INDENT:
				case NEWLINE:
					type = Type.INDENT;
					output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
					break;
				case PUNCTUATION:
				case TAB:
					type = Type.TAB;
					output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
					break;
				case COMMENT:
				case STRING:
					buff.appendCodePoint(codePoint);
					break;
				case WORD:
					output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
					buff = new StringBuilder();
					type = Type.TAB;
					output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
					break;
				}
			} else if(Character.isWhitespace(codePoint)) {
				switch(type) {
				case INDENT:
				case NEWLINE:
					type = Type.INDENT;
					output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
					break;
				case COMMENT:
				case STRING:
					buff.appendCodePoint(codePoint);
					break;
				case PUNCTUATION:
				case TAB:
					break;
				case WORD:
					output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
					buff = new StringBuilder();
					type = Type.TAB;
					break;
				}
			} else if(Character.isLetterOrDigit(codePoint)) {
				switch(type) {
				case COMMENT:
				case STRING:
					buff.appendCodePoint(codePoint);
					break;
				case INDENT:
				case NEWLINE:
				case PUNCTUATION:
				case TAB:
					type = Type.WORD;
					//$FALL-THROUGH$
				case WORD:
					buff.appendCodePoint(codePoint);
					break;
				}
			} else {
				switch(type) {
				case COMMENT:
					buff.appendCodePoint(codePoint);
					switch(codePoint) {
					case '[':
						commentDepth++;
						break;
					case ']':
						commentDepth--;
						if(commentDepth == 0) {
							output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
							buff = new StringBuilder();
							type = old;
							if(type == Type.WORD) type = Type.TAB;
							return;
						}
					}
					break;
				case INDENT:
				case NEWLINE:
				case PUNCTUATION:
				case TAB:
					switch(codePoint) {
					case '[':
						old = type;
						type = Type.COMMENT;
						buff.appendCodePoint(codePoint);
						commentDepth++;
						break;
					case '"':
						type = Type.STRING;
						buff.appendCodePoint(codePoint);
						break;
					default:
						type = Type.PUNCTUATION;
						output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
						break;
					}
					break;
				case STRING:
					buff.appendCodePoint(codePoint);
					switch(codePoint) {
					case '[':
						commentDepth++;
						break;
					case ']':
						commentDepth--;
						break;
					case '"':
						if(commentDepth == 0) {
							output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
							buff = new StringBuilder();
							type = Type.PUNCTUATION;
						}
						break;
					}
					break;
				case WORD:
					output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
					buff = new StringBuilder();
					switch(codePoint) {
					case '[':
						type = Type.COMMENT;
						old = Type.TAB;
						commentDepth++;
						buff.appendCodePoint(codePoint);
						break;
					case '"':
						type = Type.STRING;
						buff.appendCodePoint(codePoint);
						break;
					default:
						type = Type.PUNCTUATION;
						output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
						break;
					}
					break;
				}
			}
			if(commentDepth<0) {
				Logging.log(Severity.FATAL, "Comment depth is negative on char %s.[type = %s; old = %s]\n%s", Character.toString(codePoint), oldType, oldOld, buff);
				System.exit(2);
			}
		}
	}

 	public static class CommentRemover implements Consumer<SourcedToken>, Runnable {
 		private final Consumer<? super SourcedToken> output;
		private final Runnable oflush;
		private final List<SourcedToken> buff = new LinkedList<>();
		private boolean hasComment = false, hasNonWhitespace = false;
		
		public CommentRemover(Consumer<? super SourcedToken> output, Runnable oflush) {
			this.output = output;
			this.oflush = oflush;
		}
		public CommentRemover(Consumer<? super SourcedToken> output) {
			this(output, output instanceof Runnable r ? r : () -> {});
		}

		@Override
		public void run() {
			if(!hasComment) {
				buff.forEach(output);
				buff.clear();
			}
			oflush.run();
		}
		
		@Override
		public void accept(SourcedToken tok) {
			switch(tok.tok().type) {
			case COMMENT:
				hasComment = true;
				return;
			case INDENT:
				buff.add(tok);
				break;
			case NEWLINE:
				if(!hasComment || hasNonWhitespace) {
					buff.forEach(output);
					buff.clear();
					output.accept(tok);
				} else buff.clear();
				hasComment = false;
				hasNonWhitespace = false;
				break;
			default:
				buff.forEach(output);
				buff.clear();
				output.accept(tok);
				hasNonWhitespace = true;
				break;
			}
		}
 	}
}
