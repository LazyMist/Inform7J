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

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.Statistics;

@Slf4j
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
        return type == other.type && content.equalsIgnoreCase(other.content);
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
        if(type.caseSensitive) return this;
        return toLowerCase();
    }
    
    public record SourcedToken(Token tok, long line, Path src, Source source) {
        public static String toString(List<SourcedToken> tok) {
            return Token.toString(tok.stream().map(SourcedToken::tok));
        }
    }
    
    public enum Type {
        WORD(false),
        TAB(false),
        NEWLINE(false),
        INDENT(false),
        STRING(true),
        PUNCTUATION(true),
        COMMENT(true);
        public final boolean caseSensitive;
        Type(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }
    }
    
    public static class Generator implements IntConsumer, Runnable {
        private IllegalStateException invalidTypeException() {
            return new IllegalStateException("Invalid Type " + type);
        }
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
                Statistics.INCOMPLETE_EOF_LINE.prepareLog(log).log("Unclosed comment");
                buff.append("]".repeat(commentDepth));
                output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
                buff = new StringBuilder();
                break;
            case INDENT, NEWLINE, PUNCTUATION, TAB:
                break;
            case STRING:
                Statistics.INCOMPLETE_EOF_LINE.prepareLog(log).log("Unclosed String");
                buff.append("]".repeat(commentDepth));
                buff.append('"');
                //$FALL-THROUGH$
            case WORD:
                output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
                buff = new StringBuilder();
                break;
            default:
                throw invalidTypeException();
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
                case INDENT, NEWLINE, PUNCTUATION, TAB:
                    type = Type.NEWLINE;
                    output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
                    break;
                case COMMENT, STRING:
                    buff.appendCodePoint(codePoint);
                    break;
                case WORD:
                    output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
                    buff = new StringBuilder();
                    type = Type.NEWLINE;
                    output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
                    break;
                default:
                    throw invalidTypeException();
                }
                line++;
            } else if(codePoint == '\t') {
                switch(type) {
                case INDENT, NEWLINE:
                    type = Type.INDENT;
                    output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
                    break;
                case PUNCTUATION, TAB:
                    type = Type.TAB;
                    output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
                    break;
                case COMMENT, STRING:
                    buff.appendCodePoint(codePoint);
                    break;
                case WORD:
                    output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
                    buff = new StringBuilder();
                    type = Type.TAB;
                    output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
                    break;
                default:
                    throw invalidTypeException();
                }
            } else if(Character.isWhitespace(codePoint)) {
                switch(type) {
                case INDENT, NEWLINE:
                    type = Type.INDENT;
                    output.accept(new SourcedToken(new Token(type, Character.toString(codePoint)), line, src, source));
                    break;
                case COMMENT, STRING:
                    buff.appendCodePoint(codePoint);
                    break;
                case PUNCTUATION, TAB:
                    break;
                case WORD:
                    output.accept(new SourcedToken(new Token(type, buff.toString()), line, src, source));
                    buff = new StringBuilder();
                    type = Type.TAB;
                    break;
                default:
                    throw invalidTypeException();
                }
            } else if(Character.isLetterOrDigit(codePoint)) {
                switch(type) {
                case COMMENT, STRING:
                    buff.appendCodePoint(codePoint);
                    break;
                case INDENT, NEWLINE, PUNCTUATION, TAB:
                    type = Type.WORD;
                    //$FALL-THROUGH$
                case WORD:
                    buff.appendCodePoint(codePoint);
                    break;
                default:
                    throw invalidTypeException();
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
                        break;
                    default:
                        break;
                    }
                    break;
                case INDENT, NEWLINE, PUNCTUATION, TAB:
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
                        output.accept(new SourcedToken(
                            new Token(type, Character.toString(codePoint)),
                            line,
                            src,
                            source
                        ));
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
                    default:
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
                        output.accept(new SourcedToken(
                            new Token(type, Character.toString(codePoint)),
                            line,
                            src,
                            source
                        ));
                        break;
                    }
                    break;
                default:
                    throw invalidTypeException();
                }
            }
            if(commentDepth < 0) {
                log.error(
                    "Comment depth is negative on char {}.[type = {}; old = {}]\n{}",
                    Character.toString(codePoint),
                    oldType,
                    oldOld,
                    buff
                );
                System.exit(2);
            }
        }
    }
    
    public static class CommentRemover implements Consumer<SourcedToken>, Runnable {
        private final Consumer<? super SourcedToken> output;
        private final Runnable oflush;
        private final List<SourcedToken> buff = new LinkedList<>();
        private boolean hasComment = false;
        private boolean hasNonWhitespace = false;
        
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
				} else {
					buff.clear();
				}
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
