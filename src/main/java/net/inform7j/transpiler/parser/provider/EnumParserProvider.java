package net.inform7j.transpiler.parser.provider;

import net.inform7j.transpiler.language.IEnum;
import net.inform7j.transpiler.language.impl.deferring.DeferringEnum;
import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.SimpleCombinedParser;
import net.inform7j.transpiler.tokenizer.Replacement;
import net.inform7j.transpiler.tokenizer.TokenPattern;

import java.util.List;
import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

public class EnumParserProvider implements CombinedParser.Provider {
    private static final String CAPTURE_NAME = "name";
    private static final String CAPTURE_VALUES = "values";
    private static final List<SimpleCombinedParser<? extends DeferringEnum>> PARSERS = Stream.of(new DeferringImpl.Parser<>(
        TokenPattern.quoteIgnoreCase("the")
            .concat(WORD_LOOP.concat(PLURAL_WORD).capture(CAPTURE_NAME))
            .concatIgnoreCase("are")
            .concat(WORD_LOOP.capture(CAPTURE_VALUES))
            .concat(
                TokenPattern.quote(",").orIgnoreCase("and").orIgnoreCase(", and")
                    .concat(WORD_LOOP.capture(CAPTURE_VALUES)).loop()
            )
            .concat(ENDMARKER)
        /*Pattern.compile("The (?<name>.+?)s are (?<values>.+?)\\.\\s*+", Pattern.CASE_INSENSITIVE)*/,
        ctx -> new DeferringEnum(
            ctx.story(),
            ctx.source().source(),
            IEnum.Category.KIND,
            ctx.result().cap(CAPTURE_NAME),
            ctx.result().capMulti(CAPTURE_VALUES)
        )
    ), new DeferringImpl.Parser<>(
        AN.concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_NAME))
            .concat(TokenPattern.quoteIgnoreCase("can be").orIgnoreCase("is either"))
            .concat(WORD_LOOP.capture(CAPTURE_VALUES))
            .concat(
                TokenPattern.quote(",").orIgnoreCase("or").orIgnoreCase(",or")
                    .concat(WORD_LOOP.capture(CAPTURE_VALUES)).loop()
            )
            .concat(ENDMARKER)
        /*Pattern.compile("(?<name>.+?) can be (?<values>.+?)\\.\\s*+", Pattern.CASE_INSENSITIVE)*/,
        ctx -> new DeferringEnum(
            ctx.story(),
            ctx.source().source(),
            IEnum.Category.PROPERTY,
            ctx.result().cap(CAPTURE_NAME),
            ctx.result().capMulti(CAPTURE_VALUES)
        )
    ), new DeferringImpl.Parser<>(
        WORD_LOOP.capture(CAPTURE_NAME)
            .concat(TokenPattern.quoteIgnoreCase("can be").orIgnoreCase("is either"))
            .concat(WORD_LOOP.capture(CAPTURE_VALUES))
            .concat(
                TokenPattern.quote(",").orIgnoreCase("or").orIgnoreCase(",or")
                    .concat(WORD_LOOP.capture(CAPTURE_VALUES)).loop()
            )
            .concat(ENDMARKER)
        /*Pattern.compile("(?<name>.+?) can be (?<values>.+?)\\.\\s*+", Pattern.CASE_INSENSITIVE)*/,
        ctx -> new DeferringEnum(
            ctx.story(),
            ctx.source().source(),
            IEnum.Category.OBJECT,
            ctx.result().cap(CAPTURE_NAME),
            ctx.result().capMulti(CAPTURE_VALUES)
        )
    )).map(SimpleCombinedParser.bind(
        1,
        DeferringStory::replace,
        DeferringStory::addEnum
    )).toList();
    @Override
    public Stream<? extends SimpleCombinedParser<? extends DeferringEnum>> get() {
        return PARSERS.stream()
            ;
    }
}
