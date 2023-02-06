package net.inform7j.transpiler.language.impl.deferring;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.util.StatementSupplier;
import net.inform7j.transpiler.language.ITable;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPattern.Result;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringTable extends DeferringImpl implements ITable<DeferringTable.DeferringColumn> {
    public record DeferringColumn(DeferringTable table, TokenString name, Optional<TokenString> kind)
        implements IColumn<DeferringColumn> {
        public static final TokenPattern HDR = TokenPattern.Single.WORD.or(new TokenPattern.Single(new TokenPredicate(
                Token.Type.PUNCTUATION,
                s -> !"(".equals(
                    s)
            ))).loop()
            .capture(CAPTURE_NAME).concat(TokenPattern.quote("(")
                .concat(WORD_LOOP.capture(CAPTURE_TYPE))
                .concat(")")
                .omittable()),
            HDR_FULL = HDR.concat(TokenPattern.END);
        //Pattern.compile("^(?<name>.+?)(?:\\s*\\((?<type>.+?)\\))?$")
        public static Function<DeferringTable, DeferringColumn> parse(TokenString s) {
            final Optional<Result> m = HDR_FULL.matches(s).findFirst();
            if(m.isEmpty()) throw new IllegalArgumentException("Invalid column header: " + s);
            Result r = m.get();
            return d -> new DeferringColumn(d, r.cap("name"), r.capOpt("type"));
        }
        @Override
        public Optional<? extends DeferringKind> explicitKind() {
            return kind.map(table.story::getKind);
        }
    }
    
    public static final TokenPattern TABLE = new TokenPattern.Single(new TokenPredicate(Pattern.compile(
        "table",
        Pattern.CASE_INSENSITIVE
    )));
    
    public static class DeferringContinuation extends DeferringImpl {
        public static final String CAPTURE_OF = "of";
        @SuppressWarnings("hiding")
        public static final Parser<DeferringContinuation> PARSER = new Parser<>(
            TABLE.concat(TokenPattern.quoteIgnoreCase("of").capture(CAPTURE_OF).omittable())
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_NAME))
                .concat(TokenPattern.quoteIgnoreCase("(continued)"))
                .concat(ENDMARKER)
            /*Pattern.compile("^Table (?:(?<number>\\w+)|of (?<name>[-\\w\\s]+))\\w*\\(continued\\)\\w*$", Pattern.CASE_INSENSITIVE)*/,
            DeferringContinuation::parse);
        private static DeferringContinuation parse(ParseContext ctx) {
            RawLineStatement hdrLine = ctx.supplier().getNext(RawLineStatement.class);
            Stream.Builder<TokenString> str = Stream.builder();
            TokenString t = TokenString.EMPTY;
            for(Token tok : hdrLine.raw()) {
                if(tok.type() == Token.Type.TAB) {
                    if(!t.isEmpty()) {
						if(EMPTY.concat(TokenPattern.END).matches(t).findFirst().isPresent()) {
							str.accept(TokenString.EMPTY);
						} else {
							str.accept(t);
						}
                        t = TokenString.EMPTY;
                    }
                    continue;
                }
                if(!TokenPredicate.IS_WHITESPACE.test(tok)) t = t.concat(new TokenString(tok));
            }
            if(!t.isEmpty()) {
				if(EMPTY.concat(TokenPattern.END).matches(t).findFirst().isPresent()) {
					str.accept(TokenString.EMPTY);
				} else {
					str.accept(t);
				}
            }
            final Result m = ctx.result();
            TokenString name = m.cap(CAPTURE_NAME);
            return new DeferringContinuation(
                ctx.story(),
                ctx.source().source(),
                name,
                m.capOpt(CAPTURE_OF).isEmpty(),
                str.build().map(ContinuedColumn::parse),
                parseEntries(ctx.supplier())
            );
        }
        
        public record ContinuedColumn(TokenString name, Optional<TokenString> kind) {
            public static ContinuedColumn parse(TokenString s) {
                Optional<Result> m = DeferringColumn.HDR.matches(s).findFirst();
                if(m.isEmpty()) throw new IllegalArgumentException("Not a Column Header: " + s);
                Result r = m.get();
                return new ContinuedColumn(r.cap("name"), r.capOpt("type"));
            }
        }
        
        public final TokenString TABLE_NAME;
        public final boolean NUMBER;
        public final Map<TokenString, TokenString> KINDSPEC;
        private final List<Map<TokenString, TokenString>> ROWS;
        public DeferringContinuation(
            DeferringStory story, Source source, TokenString tABLE, boolean nUMBER, Stream<ContinuedColumn> hdr,
            Stream<? extends Stream<TokenString>> rOWS
        ) {
            super(story, source);
            TABLE_NAME = tABLE;
            NUMBER = nUMBER;
            final List<ContinuedColumn> cols = hdr.toList();
            KINDSPEC = cols.stream().filter(c -> c.kind().isPresent()).collect(Collectors.toUnmodifiableMap(
                ContinuedColumn::name,
                c -> c.kind().get()
            ));
            ROWS = Collections.unmodifiableList(rOWS.map(Stream::toList).map(l -> {
				if(l.size() > cols.size()) {
					throw new IllegalArgumentException("Row Mismatch: expected " + cols.size() + " got " + l.size() + ":\n" + cols + "\n" + l);
				}
                return IntStream.range(0, l.size())
                    .boxed()
                    .collect(Collectors.toUnmodifiableMap(
                        i -> cols.get(i).name(),
                        l::get
                    ));
            }).toList());
        }
        public DeferringTable table() {
            return story.getTable(TABLE_NAME, NUMBER);
        }
        public Optional<? extends DeferringKind> kindSpec(DeferringColumn column) {
            TokenString spec = KINDSPEC.get(column.name);
            if(spec == null) return Optional.empty();
            return Optional.of(story.getKind(spec));
        }
        public Stream<Map<DeferringColumn, TokenString>> rows() {
            DeferringTable t = table();
            return ROWS.stream().map(m -> m.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                    e -> t.columns().filter(d -> d.name().equals(e.getKey())).findFirst().orElseThrow(),
                    Entry::getValue
                ))
            );
        }
    }
    
    public static final String CAPTURE_NUMBER = "number";
    public static final String CAPTURE_NAME = "name";
    public static final String CAPTURE_TYPE = "type";
    public static final String CAPTURE_COUNT = "count";
    public static final Parser<DeferringTable> PARSER = new Parser<>(
        TokenPattern.quoteIgnoreCase("Table")
            .concat(
                TokenPattern.Single.WORD.capture(CAPTURE_NUMBER)
                    .concat(TokenPattern.quote("-").concat(WORD_LOOP.capture(CAPTURE_NAME)).omittable())
                    .or(TokenPattern.quoteIgnoreCase("of").concat(WORD_LOOP.capture(CAPTURE_NAME))))
            .concat(ENDLINE)
        /*Pattern.compile("^Table (?:(?<number>\\w+)(?: - (?<name>.+?))|of (?<name2>.+?))\\s*$", Pattern.CASE_INSENSITIVE)*/,
        DeferringTable::parse);
    public static final TokenPattern EMPTY = TokenPattern.quote("--");
    public static final TokenPattern FINALIZER = TokenPattern.quoteIgnoreCase("with")
        .concat(TokenPattern.Single.WORD.capture(CAPTURE_COUNT))
        .concatIgnoreCase("blank")
        .concat(TokenPattern.quoteIgnoreCase("row").orIgnoreCase("rows"))
        .concat(ENDMARKER);
    //Pattern.compile("^with (?<count>\\d+) blank rows?\\.?\\s*$", Pattern.CASE_INSENSITIVE)
    private static Stream<Stream<TokenString>> parseEntries(StatementSupplier sup) {
        Stream.Builder<Stream<TokenString>> entries = Stream.builder();
        while(true) {
            Optional<RawLineStatement> entryLine = sup.getNextOptional(RawLineStatement.class);
            if(entryLine.isEmpty()) break;
            RawLineStatement line = entryLine.get();
            if(line.isBlank()) continue;
            Optional<Result> m2 = FINALIZER.matches(line.raw()).findFirst();
            if(m2.isPresent()) {
				for(int i = Integer.parseInt(m2.get()
					.cap(CAPTURE_COUNT)
					.stream()
					.map(Token::content)
					.collect(Collectors.joining(" "))); i > 0; i--) {
					entries.accept(Stream.empty());
				}
                break;
            }
            if(!line.raw().contains(new Token(Token.Type.TAB, "\t"))) {
                sup.reverse();
                break;
            }
            Stream.Builder<TokenString> str = Stream.builder();
            TokenString t = TokenString.EMPTY;
            for(Token tok : line.raw()) {
                if(tok.type() == Token.Type.TAB) {
                    if(!t.isEmpty()) {
						if(EMPTY.concat(TokenPattern.END).matches(t).findFirst().isPresent()) {
							str.accept(TokenString.EMPTY);
						} else {
							str.accept(t);
						}
                        t = TokenString.EMPTY;
                    }
                    continue;
                }
                if(!TokenPredicate.IS_WHITESPACE.test(tok)) t = t.concat(new TokenString(tok));
            }
            if(!t.isEmpty()) {
				if(EMPTY.concat(TokenPattern.END).matches(t).findFirst().isPresent()) {
					str.accept(TokenString.EMPTY);
				} else {
					str.accept(t);
				}
            }
            entries.accept(str.build());
        }
        return entries.build();
    }
    private static DeferringTable parse(ParseContext ctx) {
        RawLineStatement hdrLine = ctx.supplier().getNext(RawLineStatement.class);
        Stream.Builder<TokenString> str = Stream.builder();
        TokenString t = TokenString.EMPTY;
        for(Token tok : hdrLine.raw()) {
            if(tok.type() == Token.Type.TAB) {
                if(!t.isEmpty()) {
					if(EMPTY.concat(TokenPattern.END).matches(t).findFirst().isPresent()) {
						str.accept(TokenString.EMPTY);
					} else {
						str.accept(t);
					}
                    t = TokenString.EMPTY;
                }
                continue;
            }
            if(!TokenPredicate.IS_WHITESPACE.test(tok)) t = t.concat(new TokenString(tok));
        }
        if(!t.isEmpty()) {
			if(EMPTY.concat(TokenPattern.END).matches(t).findFirst().isPresent()) {
				str.accept(TokenString.EMPTY);
			} else {
				str.accept(t);
			}
        }
        final Result m = ctx.result();
        return new DeferringTable(
            ctx.story(),
            ctx.source().source(),
            m.capOpt(CAPTURE_NUMBER),
            m.capOpt(CAPTURE_NAME),
            str.build().map(DeferringColumn::parse),
            parseEntries(ctx.supplier())
        );
    }
    
    public final Optional<TokenString> NUMBER;
    public final Optional<TokenString> NAME;
    public final List<DeferringColumn> COLUMNS;
    private final List<Map<DeferringColumn, TokenString>> ROWS;
    public DeferringTable(
        DeferringStory story,
        Source source,
        Optional<TokenString> nUMBER,
        Optional<TokenString> nAME,
        Stream<? extends Function<? super DeferringTable, DeferringColumn>> cOLUMNS,
        Stream<? extends Stream<TokenString>> rOWS
    ) {
        super(story, source);
        NUMBER = nUMBER;
        NAME = nAME;
        COLUMNS = Collections.unmodifiableList(cOLUMNS.map(f -> f.apply(this)).toList());
        ROWS = Collections.unmodifiableList(rOWS.map(Stream::toList).map(l -> {
			if(l.size() > COLUMNS.size()) {
				throw new IllegalArgumentException("Row Mismatch: expected " + COLUMNS.size() + " got " + l.size() + ":\n" + l.stream()
					.map(TokenString::toString)
					.collect(Collectors.joining("\n")));
			}
            return IntStream.range(0, l.size()).boxed()
                .collect(Collectors.toUnmodifiableMap(
                    COLUMNS::get,
                    l::get
                ));
        }).toList());
    }
    
    @Override
    public Optional<TokenString> number() {
        return NUMBER;
    }
    @Override
    public Optional<TokenString> name() {
        return NAME;
    }
    @Override
    public Stream<DeferringColumn> columns() {
        return COLUMNS.stream();
    }
    @Override
    public Stream<? extends Map<DeferringColumn, TokenString>> rows() {
        return Stream.concat(ROWS.stream(), story.getContinuedTable(this).flatMap(DeferringContinuation::rows));
    }
    
}
