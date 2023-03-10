package net.inform7j.transpiler.parser.provider;

import net.inform7j.transpiler.language.impl.deferring.*;
import net.inform7j.transpiler.language.impl.deferring.rules.*;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringNamedRule;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringSimpleRule;
import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.SimpleCombinedParser;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class LegacyParserProvider implements CombinedParser.Provider {
    private static final List<? extends CombinedParser> CPARSERS = Stream.<Stream<CombinedParser>>of(
            Stream.of(new SimpleCombinedParser<>(
                13,
                DeferringNamedRule.PARSER,
                DeferringStory::replace,
                DeferringStory::addRule
            )),
            DeferringActionRule.PARSERS.stream().map(SimpleCombinedParser.bind(
                14,
                DeferringStory::replace,
                DeferringStory::addRule
            )),
            DeferringSimpleRule.PARSERS.stream().map(SimpleCombinedParser.bind(
                15,
                DeferringStory::replace,
                DeferringStory::addRule
            )),
            DeferringDefault.PARSERS.stream().map(SimpleCombinedParser.bind(
                16,
                DeferringStory::replace,
                DeferringStory::addDefault
            )),
            DeferringValue.PARSERS.stream().map(SimpleCombinedParser.bind(
                17,
                DeferringStory::replace,
                DeferringStory::addValue
            ))
        )
        .flatMap(Function.identity())
        .toList();
    @Override
    public Stream<? extends CombinedParser> get() {
        return CPARSERS.stream();
    }
}
