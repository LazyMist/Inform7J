package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.impl.deferring.DeferringAction;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.tokenizer.TokenPattern;

import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

public class ActionParserProvider implements CombinedParser.Provider {
    private static final String CAPTURE_NAME = "name";
    private static final String CAPTURE_PRIMARY = "primary";
    private static final String CAPTURE_SECONDARY = "secondary";
    private static final String CAPTURE_REQUIREMENTS = "requirements";
    private static final SimpleCombinedParser<DeferringAction> PARSER = new SimpleCombinedParser<>(
        8,
        WORD_LOOP.capture(CAPTURE_NAME)
            .concatIgnoreCase("is an Action")
            .concatOptionalIgnoreCase("out of world")
            .concatIgnoreCase("applying to")
            .concat(
                TokenPattern.quoteIgnoreCase("nothing")
                    .or(
                        AN.orIgnoreCase("one").concat(WORD_LOOP.capture(CAPTURE_PRIMARY))
                            .concat(TokenPattern.quoteIgnoreCase("and")
                                .concat(AN.omittable())
                                .concat(WORD_LOOP.capture(CAPTURE_SECONDARY))
                                .omittable())
                    )
                    .or(
                        TokenPattern.quoteIgnoreCase("two")
                            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_PRIMARY, CAPTURE_SECONDARY))))
            .concat(TokenPattern.quoteIgnoreCase("requiring")
                .concat(WORD_LOOP.capture(CAPTURE_REQUIREMENTS))
                .omittable())
            .concat(ENDMARKER),
        DeferringStory::replace,
        ctx -> new DeferringAction(
            ctx.story(),
            ctx.source().source(),
            ctx.result().cap(CAPTURE_NAME),
            ctx.result().capOpt(CAPTURE_PRIMARY),
            ctx.result().capOpt(CAPTURE_SECONDARY),
            ctx.result().capOpt(CAPTURE_REQUIREMENTS)
        ),
        DeferringStory::addAction
    );
    @Override
    public Stream<? extends CombinedParser> get() {
        return Stream.of(PARSER);
    }
}
