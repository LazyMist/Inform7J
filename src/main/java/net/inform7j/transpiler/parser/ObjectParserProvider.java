package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.IStory;
import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringObject;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.tokenizer.Replacement;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.List;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

public class ObjectParserProvider implements CombinedParser.Provider {
    private static final TokenString OF = new TokenString("of");
    private static final String CAPTURE_NAME = "name";
    private static final String CAPTURE_TYPE = "type";
    private static final String CAPTURE_OWNER = "owner";
    private static final List<CombinedParser> PARSERS = Stream.of(new DeferringImpl.Parser<>(
            TokenPattern.quoteIgnoreCase("the").omittable()
                .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
                .concat(IS).concat(AN)
                .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_TYPE))
                .concat(TokenPattern.quoteIgnoreCase("that varies").omittable())
                .concat(ENDMARKER)
            /*Pattern.compile("^(?<name>.+?) is an? (?<type>.+?)(?> that varies)?\\.", Pattern.CASE_INSENSITIVE)*/,
            ctx -> new DeferringObject(
                ctx.story(),
                ctx.source().source(),
                ctx.result().cap(CAPTURE_NAME),
                ctx.result().cap(CAPTURE_TYPE)
            )
        ), new DeferringImpl.Parser<>(
            TokenPattern.quoteIgnoreCase("the").omittable()
                .concat(new Replacement(DeferringStory.OBJECT_NAME_REPLACEMENT, false).capture(
                    CAPTURE_OWNER))
                .concat(HAS).concat(AN.omittable())
                .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_TYPE))
                .concat("called")
                .concat(TokenPattern.quoteIgnoreCase("the").omittable())
                .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
                .concat(ENDMARKER)
            /*Pattern.compile("^(?>the )?(?<owner>.+?) has an? (?<type>.+?) called (?>the )?(?<name>.+?)\\.", Pattern.CASE_INSENSITIVE)*/,
            ctx -> new DeferringObject(
                ctx.story(),
                ctx.source().source(),
                ctx.result().cap(CAPTURE_NAME).concat(OF, ctx.result().cap(CAPTURE_OWNER)),
                ctx.result().cap(CAPTURE_TYPE)
            )
        ), new DeferringImpl.Parser<>(
            TokenPattern.quoteIgnoreCase("the").omittable()
                .concat(new Replacement(DeferringStory.OBJECT_NAME_REPLACEMENT, false).capture(
                    CAPTURE_OWNER))
                .concatIgnoreCase("can be").concat(TokenPattern.quoteIgnoreCase("either").lookahead(true))
                .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
                .concat(ENDMARKER),
            ctx -> new DeferringObject(
                ctx.story(),
                ctx.source().source(),
                ctx.result().cap(CAPTURE_NAME).concat(OF, ctx.result().cap(CAPTURE_OWNER)),
                IStory.BaseKind.TRUTH_STATE
            )
        ))
        .map(SimpleCombinedParser.bind(3, DeferringStory::replace, DeferringStory::addObject))
        .toList();
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream();
    }
}
