package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.IStory;
import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringProperty;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.tokenizer.Replacement;
import net.inform7j.transpiler.tokenizer.TokenPattern;

import java.util.List;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

public class PropertyParserProvider implements CombinedParser.Provider {
    private static final TokenPattern SOME = AN.orIgnoreCase("some");
    private static final String CAPTURE_OWNER = "owner";
    private static final String CAPTURE_TYPE = "type";
    private static final String CAPTURE_NAME = "name";
    private static final List<CombinedParser> PARSERS = Stream.of(new DeferringImpl.Parser<>(
        AN.omittable().concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_OWNER))
            .concat(HAS).concat(SOME)
            .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_TYPE))
            .concatIgnoreCase("called")
            .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
            .concat(ENDMARKER)
        /*Pattern.compile("^an? (?<owner>.+?) has (?:an?|some) (?<type>.+?) called (?<name>.+?)\\.", Pattern.CASE_INSENSITIVE)*/,
        ctx -> new DeferringProperty(
            ctx.story(),
            ctx.source().source(),
            ctx.result().cap(CAPTURE_OWNER),
            ctx.result().cap(CAPTURE_NAME),
            ctx.result().cap(CAPTURE_TYPE)
        )
    ), new DeferringImpl.Parser<>(
        AN.omittable().concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_OWNER))
            .concatIgnoreCase("can be").concat(TokenPattern.quoteIgnoreCase("either").lookahead(true))
            .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
            .concat(ENDMARKER)
        /*Pattern.compile("^an? (?<owner>.+?) can be (?!either)(?<name>.+?)\\.", Pattern.CASE_INSENSITIVE)*/,
        ctx -> new DeferringProperty(
            ctx.story(),
            ctx.source().source(),
            ctx.result().cap(CAPTURE_OWNER),
            ctx.result().cap(CAPTURE_NAME),
            IStory.BaseKind.TRUTH_STATE.writtenName
        )
    )).map(SimpleCombinedParser.bind(
        2,
        DeferringStory::replace,
        DeferringStory::addProperty
    )).toList();
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream();
    }
}
