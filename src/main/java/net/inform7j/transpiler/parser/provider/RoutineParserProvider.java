package net.inform7j.transpiler.parser.provider;

import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringRoutine;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.SimpleCombinedParser;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.provider.FunctionParserProvider.*;
import static net.inform7j.transpiler.parser.Patterns.*;

public class RoutineParserProvider implements CombinedParser.Provider {
    public static final List<DeferringImpl.Parser<DeferringRoutine>> PARSERS = Collections.unmodifiableList(Arrays.asList(
        new DeferringImpl.Parser<>(
            TokenPattern.quoteIgnoreCase("to")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(":")
                .concat(ENDLINE),
            ctx -> new DeferringRoutine(
                ctx.story(),
                ctx.source().source(),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                getNextBody(ctx.supplier())
            )
        ),
        new DeferringImpl.Parser<>(
            TokenPattern.quoteIgnoreCase("to")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(": (-")
                .concat(ENDLINE),
            ctx -> {
                DeferringRoutine ret = new DeferringRoutine(
                    ctx.story(),
                    ctx.source().source(),
                    parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                    null
                );
                consumeRawBlock(ctx);
                return ret;
            }
        ),
        new DeferringImpl.Parser<>(
            TokenPattern.quoteIgnoreCase("to")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(": (-")
                .concat(new Single(TokenPredicate.NEWLINE.negate()).loop())
                .concat("-)")
                .concat(ENDMARKER),
            ctx -> new DeferringRoutine(
                ctx.story(),
                ctx.source().source(),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                null
            )
        )
    ));
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream().map(SimpleCombinedParser.bind(
            7,
            DeferringStory::replace,
            DeferringStory::addRoutine
        ));
    }
}
