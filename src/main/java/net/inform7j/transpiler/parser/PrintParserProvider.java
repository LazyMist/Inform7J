package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringPrint;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.List;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.FunctionParserProvider.*;
import static net.inform7j.transpiler.parser.Patterns.*;

public class PrintParserProvider implements CombinedParser.Provider {
    private static final TokenPattern TO_SAY = TokenPattern.quoteIgnoreCase("to say");
    private static final List<? extends CombinedParser> PARSERS = Stream.of(new DeferringImpl.Parser<>(
            TO_SAY
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(":")
                .concat(ENDLINE),
            ctx -> new DeferringPrint(
                ctx.story(),
                ctx.source().source(),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                getNextBody(ctx.supplier())
            )
        ), new DeferringImpl.Parser<>(
            TO_SAY
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(":(-")
                .concat(ENDLINE),
            ctx -> {
                DeferringPrint ret = new DeferringPrint(
                    ctx.story(),
                    ctx.source().source(),
                    parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                    null
                );
                consumeRawBlock(ctx);
                return ret;
            }
        ), new DeferringImpl.Parser<>(
            TO_SAY
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(":(-")
                .concat(new Single(TokenPredicate.NEWLINE.negate()).loop())
                .concat("-)")
                .concat(ENDLINE),
            ctx -> new DeferringPrint(
                ctx.story(),
                ctx.source().source(),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                null
            )
        ))
        .map(SimpleCombinedParser.bind(
            6,
            DeferringStory::replace,
            DeferringStory::addPrint
        ))
        .toList();
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream();
    }
}
