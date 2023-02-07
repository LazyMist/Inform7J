package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.impl.deferring.DeferringKind;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;

import java.util.stream.Stream;

public class KindParserProvider implements CombinedParser.Provider {
    private final SimpleCombinedParser<DeferringKind> parser = new SimpleCombinedParser<>(
        0,
        DeferringKind.PARSER,
        DeferringStory::replace,
        DeferringStory::addKind
    );
    @Override
    public Stream<? extends CombinedParser> get() {
        return Stream.of(parser);
    }
}
