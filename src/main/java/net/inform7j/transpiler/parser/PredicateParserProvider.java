package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.impl.deferring.*;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.FunctionParserProvider.*;
import static net.inform7j.transpiler.parser.Patterns.*;

public class PredicateParserProvider implements CombinedParser.Provider {
    private static final TokenPattern WHETHER = new Single(new TokenPredicate(Pattern.compile(
        "if|whether",
        Pattern.CASE_INSENSITIVE
    )));
    private static final TokenPattern DEFINITION = TokenPattern.quoteIgnoreCase("definition:");
    private static final String CAPTURE_THIS_VAR = "thisVar";
    private static final String CAPTURE_KIND = "kind";
    private static final String CAPTURE_BODY = "body";
    private static final List<? extends CombinedParser> PARSERS = Stream.of(new DeferringImpl.Parser<>(
            DEFINITION.concat(WHETHER)
                .concat(PARAM_GLOB.loop())
                .concat(":")
                .concat(ENDLINE),
            ctx -> new DeferringPredicate(
                ctx.story(),
                ctx.source().source(),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                getNextBody(ctx.supplier())
            )
        ), new DeferringImpl.Parser<>(
            DEFINITION.concat(AN)
                .concat(WORD_LOOP.capture(CAPTURE_KIND))
                .concat(TokenPattern.quoteIgnoreCase("(called")
                    .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_THIS_VAR))
                    .concat(")")
                    .omittable())
                .concat(TokenPattern.quoteIgnoreCase("is").orIgnoreCase("has").orIgnoreCase("can"))
                .concat(PARAM_GLOB.loop(true))
                .concat(":")
                .concat(ENDLINE),
            ctx -> new DeferringPredicate(
                ctx.story(),
                ctx.source().source(),
                Stream.concat(Stream.of(new DeferringFunction.DeferredParameter(
                    ctx.story(),
                    ctx.source().source(),
                    ctx.result().capOpt(CAPTURE_THIS_VAR).orElse(new TokenString(new Token(
                        Token.Type.WORD,
                        "it"
                    ))),
                    ctx.result().cap(CAPTURE_KIND)
                )), parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS))),
                getNextBody(ctx.supplier())
            )
        ), new DeferringImpl.Parser<>(
            DEFINITION.concat(AN)
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_KIND))
                .concat(TokenPattern.quoteIgnoreCase("(called")
                    .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_THIS_VAR))
                    .concat(")")
                    .omittable())
                .concat(TokenPattern.quoteIgnoreCase("is").orIgnoreCase("has").orIgnoreCase("can"))
                .concat(PARAM_GLOB.loop(true))
                .concat("if")
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_BODY))
                .concat(ENDMARKER),
            ctx -> new DeferringPredicate(
                ctx.story(),
                ctx.source().source(),
                Stream.concat(
                    Stream.of(new DeferringFunction.DeferredParameter(
                        ctx.story(),
                        ctx.source().source(),
                        ctx.result().capOpt(CAPTURE_THIS_VAR).orElse(new TokenString(new Token(
                            Token.Type.WORD,
                            "it"
                        ))),
                        ctx.result().cap(CAPTURE_KIND)
                    )),
                    parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS))
                ),
                new RawLineStatement(new TokenString(Stream.concat(
                    Token.Generator.parseLiteral("decide on"),
                    ctx.result().cap(CAPTURE_BODY).stream()
                )), ctx.source().line(), ctx.source().src(), ctx.source().source())
            )
        ), new DeferringImpl.Parser<>(
            DEFINITION.concatOptionalIgnoreCase("the")
                .concat(NOT_ENDMARKER.capture(CAPTURE_NAME_PARAMS).loop(true))
                .concat(TokenPattern.quoteIgnoreCase("is")
                    .orIgnoreCase("has")
                    .orIgnoreCase("can")
                    .capture(CAPTURE_NAME_PARAMS))
                .concat(PARAM_GLOB.loop(true))
                .concat(":")
                .concat(ENDLINE),
            ctx -> new DeferringPredicate(
                ctx.story(),
                ctx.source().source(),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                getNextBody(ctx.supplier())
            )
        ))
        .map(SimpleCombinedParser.bind(
            4,
            DeferringStory::replace,
            DeferringStory::addPredicate
        ))
        .toList();
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream();
    }
}
