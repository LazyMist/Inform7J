package net.inform7j.transpiler.parser;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.IntakeReader;
import net.inform7j.transpiler.language.IFunction;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringFunction;
import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.impl.deferring.RawLineStatement;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.tokenizer.pattern.End;
import net.inform7j.transpiler.tokenizer.pattern.Single;
import net.inform7j.transpiler.util.StatementSupplier;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

@Slf4j
public class FunctionParserProvider implements CombinedParser.Provider {
    static final String CAPTURE_NAME = "name";
    static final String CAPTURE_PARAMETER_TYPE = "paramType";
    static final TokenPattern PARAMETER_PATTERN = TokenPattern.quote("(")
        .concat(WORD_LOOP.capture(CAPTURE_NAME))
        .concat("-").concat(AN.omittable())
        .concat(WORD_LOOP.capture(CAPTURE_PARAMETER_TYPE))
        .concat(")");
    static DeferringFunction.DeferredParameter parseParameter(DeferringImpl.ParseContext ctx) {
        return new DeferringFunction.DeferredParameter(
            ctx.story(),
            ctx.source().source(),
            ctx.result().cap(CAPTURE_NAME),
            ctx.result().cap(CAPTURE_PARAMETER_TYPE)
        );
    }
    static Stream<IFunction.SignatureElement> parseSignatures(DeferringImpl.ParseContext ctx, List<TokenString> names) {
        Stream.Builder<IFunction.SignatureElement> name = Stream.builder();
        for(TokenString e : names) {
            Optional<Result> results = PARAMETER_PATTERN.concat(End.PATTERN).matches(e).findFirst();
            if(results.isEmpty()) {
                results = NAME_GLOB.concat(End.PATTERN).matches(e).findFirst();
                if(results.isEmpty()) throw new RuntimeException("Not a nameElement");
                name.accept(new IFunction.NameElement(results.get().capMulti(CAPTURE_NAME).stream()
                    .map(l -> l.stream().map(Token::content).collect(Collectors.joining(" ")))
                    .collect(Collectors.toUnmodifiableSet())));
            } else {
                name.accept(parseParameter(new DeferringImpl.ParseContext(ctx, results.get())));
            }
        }
        return name.build();
    }
    public static IStatement getNextBody(StatementSupplier sup) {
        IStatement ret = sup.getNext(IStatement.class);
        while(ret instanceof RawLineStatement line && line.isBlank()) ret = sup.getNext(IStatement.class);
        return ret;
    }
    static void consumeRawBlock(DeferringImpl.ParseContext ctx) {
        StatementSupplier sup = ctx.supplier();
        while(true) {
            Optional<? extends IStatement> opt = sup.getNextOptional(IStatement.class);
            if(opt.isPresent()) {
                if(IntakeReader.tailMatch(opt.get(), IntakeReader.RAW_END, true)) break;
            } else {
                log.error("Unclosed Raw block starting in: {}@{}", ctx.source().src(), ctx.source().line());
            }
        }
    }
    static final TokenPattern NAME_GLOB = Single.WORD.capture(CAPTURE_NAME)
        .concat(TokenPattern.quote("/")
            .concat(Single.WORD.or("--").capture(CAPTURE_NAME))
            .loop()
            .omittable())
        .or(Single.PUNCTUATION.capture(CAPTURE_NAME));
    static final TokenPattern PARAM_GLOB = NAME_GLOB.or(PARAMETER_PATTERN)
        .clearCapture();
    private static final TokenPattern TO_DECIDE_WHICH = TokenPattern.quoteIgnoreCase("to decide")
        .concat(new Single(new TokenPredicate(Pattern.compile(
            "which|what",
            Pattern.CASE_INSENSITIVE
        ))));
    
    static final String CAPTURE_RETURN_TYPE = "returnType";
    static final String CAPTURE_NAME_PARAMS = "nameParams";
    private static final List<? extends CombinedParser> PARSERS = Stream.of(new DeferringImpl.Parser<>(
            TO_DECIDE_WHICH
                .concat(
                    new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false)
                        .capture(CAPTURE_RETURN_TYPE)
                )
                .concat("is")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(":")
                .concat(ENDLINE),
            ctx -> new DeferringFunction(
                ctx.story(),
                ctx.source().source(),
                ctx.result().cap(CAPTURE_RETURN_TYPE),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                getNextBody(ctx.supplier())
            )
        ), new DeferringImpl.Parser<>(
            TO_DECIDE_WHICH
                .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(
                    CAPTURE_RETURN_TYPE))
                .concat("is")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(": (-")
                .concat(ENDLINE),
            ctx -> {
                DeferringFunction ret = new DeferringFunction(
                    ctx.story(),
                    ctx.source().source(),
                    ctx.result().cap(CAPTURE_RETURN_TYPE),
                    parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                    null
                );
                consumeRawBlock(ctx);
                return ret;
            }
        ), new DeferringImpl.Parser<>(
            TO_DECIDE_WHICH
                .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(
                    CAPTURE_RETURN_TYPE))
                .concat("is")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(": (-")
                .concat(NOT_ENDMARKER_LOOP)
                .concat("-)")
                .concat(ENDMARKER),
            ctx -> new DeferringFunction(
                ctx.story(),
                ctx.source().source(),
                ctx.result().cap(CAPTURE_RETURN_TYPE),
                parseSignatures(ctx, ctx.result().capMulti(CAPTURE_NAME_PARAMS)),
                null
            )
        ))
        .map(SimpleCombinedParser.bind(
            5,
            DeferringStory::replace,
            DeferringStory::addFunction
        ))
        .toList();
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream();
    }
}
