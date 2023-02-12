package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.impl.deferring.DeferringTable;
import net.inform7j.transpiler.language.impl.deferring.RawLineStatement;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.tokenizer.pattern.End;
import net.inform7j.transpiler.tokenizer.pattern.Single;
import net.inform7j.transpiler.util.StatementSupplier;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

public class TableParserProvider implements CombinedParser.Provider {
    private static final String CAPTURE_NUMBER = "number";
    private static final String CAPTURE_OF = "of";
    private static final String CAPTURE_NAME = "name";
    private static final String CAPTURE_TYPE = "type";
    private static final String CAPTURE_COUNT = "count";
    private static final TokenPattern EMPTY = TokenPattern.quote("--");
    private static final TokenPattern TABLE = new Single(new TokenPredicate(Pattern.compile(
        "table",
        Pattern.CASE_INSENSITIVE
    )));
    private static final TokenPattern HEADER = Single.WORD.or(new Single(new TokenPredicate(
            Token.Type.PUNCTUATION,
            s -> !"(".equals(
                s)
        ))).loop()
        .capture(CAPTURE_NAME).concat(TokenPattern.quote("(")
            .concat(WORD_LOOP.capture(CAPTURE_TYPE))
            .concat(")")
            .omittable());
    private static final TokenPattern HEADER_FULL = HEADER.concat(End.PATTERN);
    private static DeferringTable.DeferringContinuation.ContinuedColumn parseContinuedColumns(TokenString s) {
        Optional<Result> m = HEADER.matches(s).findFirst();
        if(m.isEmpty()) throw new IllegalArgumentException("Not a Column Header: " + s);
        Result r = m.get();
        return new DeferringTable.DeferringContinuation.ContinuedColumn(r.cap("name"), r.capOpt("type"));
    }
    private static Function<DeferringTable, DeferringTable.DeferringColumn> parseColumns(TokenString s) {
        final Optional<Result> m = HEADER_FULL.matches(s).findFirst();
        if(m.isEmpty()) throw new IllegalArgumentException("Invalid column header: " + s);
        Result r = m.get();
        return d -> new DeferringTable.DeferringColumn(d, r.cap("name"), r.capOpt("type"));
    }
    private static final TokenPattern FINALIZER = TokenPattern.quoteIgnoreCase("with")
        .concat(Single.WORD.capture(CAPTURE_COUNT))
        .concatIgnoreCase("blank")
        .concat(TokenPattern.quoteIgnoreCase("row").orIgnoreCase("rows"))
        .concat(ENDMARKER);
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
                        if(EMPTY.concat(End.PATTERN).matches(t).findFirst().isPresent()) {
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
                if(EMPTY.concat(End.PATTERN).matches(t).findFirst().isPresent()) {
                    str.accept(TokenString.EMPTY);
                } else {
                    str.accept(t);
                }
            }
            entries.accept(str.build());
        }
        return entries.build();
    }
    private static Stream<TokenString> splitHeadRow(StatementSupplier supplier) {
        RawLineStatement hdrLine = supplier.getNext(RawLineStatement.class);
        Stream.Builder<TokenString> str = Stream.builder();
        TokenString t = TokenString.EMPTY;
        for(Token tok : hdrLine.raw()) {
            if(tok.type() == Token.Type.TAB) {
                if(!t.isEmpty()) {
                    if(EMPTY.concat(End.PATTERN).matches(t).findFirst().isPresent()) {
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
            if(EMPTY.concat(End.PATTERN).matches(t).findFirst().isPresent()) {
                str.accept(TokenString.EMPTY);
            } else {
                str.accept(t);
            }
        }
        return str.build();
    }
    private static DeferringTable.DeferringContinuation parseContinuation(DeferringImpl.ParseContext ctx) {
        final Result m = ctx.result();
        TokenString name = m.cap(CAPTURE_NAME);
        return new DeferringTable.DeferringContinuation(
            ctx.story(),
            ctx.source().source(),
            name,
            m.capOpt(CAPTURE_OF).isEmpty(),
            splitHeadRow(ctx.supplier()).map(TableParserProvider::parseContinuedColumns),
            parseEntries(ctx.supplier())
        );
    }
    private static DeferringTable parseTable(DeferringImpl.ParseContext ctx) {
        final Result m = ctx.result();
        return new DeferringTable(
            ctx.story(),
            ctx.source().source(),
            m.capOpt(CAPTURE_NUMBER),
            m.capOpt(CAPTURE_NAME),
            splitHeadRow(ctx.supplier()).map(TableParserProvider::parseColumns),
            parseEntries(ctx.supplier())
        );
    }
    private static final List<? extends CombinedParser> PARSERS = List.of(
        new SimpleCombinedParser<>(
            9,
            TABLE.concat(TokenPattern.quoteIgnoreCase("of").capture(CAPTURE_OF).omittable())
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_NAME))
                .concat(TokenPattern.quoteIgnoreCase("(continued)"))
                .concat(ENDMARKER),
            DeferringStory::replace,
            TableParserProvider::parseContinuation,
            DeferringStory::addContinuation
        ),
        new SimpleCombinedParser<>(
            10,
            TABLE.concat((
                    Single.WORD.capture(CAPTURE_NUMBER)
                        .concat(TokenPattern.quote("-").concat(WORD_LOOP.capture(CAPTURE_NAME)).omittable())
                ).or(
                    TokenPattern.quoteIgnoreCase("of").concat(WORD_LOOP.capture(CAPTURE_NAME))
                ))
                .concat(ENDLINE),
            DeferringStory::replace,
            TableParserProvider::parseTable,
            DeferringStory::addTable
        )
    );
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream();
    }
}
