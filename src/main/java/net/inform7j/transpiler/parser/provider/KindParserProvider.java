package net.inform7j.transpiler.parser.provider;

import net.inform7j.transpiler.language.impl.deferring.DeferringKind;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.SimpleCombinedParser;
import net.inform7j.transpiler.tokenizer.Replacement;

import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

public class KindParserProvider implements CombinedParser.Provider {
    private static final String CAPTURE_NAME = "name";
    private static final String CAPTURE_SUPERKIND = "superkind";
    private final SimpleCombinedParser<DeferringKind> parser = new SimpleCombinedParser<>(
        0,
        AN.omittable().concat(WORD_LOOP.capture(CAPTURE_NAME))
            .concatIgnoreCase("is a kind of")
            .concat(new Replacement(
                DeferringStory.KIND_NAME_REPLACEMENT,
                false
            ).capture(CAPTURE_SUPERKIND))
            .concat(ENDMARKER),
        DeferringStory::replace,
        ctx -> new DeferringKind(
            ctx.story(),
            ctx.source().source(),
            ctx.result().cap(CAPTURE_NAME),
            ctx.result().cap(CAPTURE_SUPERKIND)
        ),
        DeferringStory::addKind
    );
    @Override
    public Stream<? extends CombinedParser> get() {
        return Stream.of(parser);
    }
}
