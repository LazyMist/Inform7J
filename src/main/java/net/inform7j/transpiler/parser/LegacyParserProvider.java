package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.impl.deferring.*;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringActionRule;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringConditionedActionRule;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringNamedRule;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringSimpleRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class LegacyParserProvider implements CombinedParser.Provider {
    public static final Collection<? extends CombinedParser> CPARSERS;
    static {
        List<CombinedParser> l = new LinkedList<>();
        DeferringEnum.PARSERS.stream()
            .map(SimpleCombinedParser.bind(1, DeferringStory::replace, DeferringStory::addEnum))
            .forEachOrdered(l::add);
        DeferringProperty.PARSERS.stream().map(SimpleCombinedParser.bind(
            2,
            DeferringStory::replace,
            DeferringStory::addProperty
        )).forEachOrdered(l::add);
        DeferringObject.PARSERS.stream()
            .map(SimpleCombinedParser.bind(3, DeferringStory::replace, DeferringStory::addObject))
            .forEachOrdered(l::add);
        DeferringPredicate.PARSERS.stream().map(SimpleCombinedParser.bind(
            4,
            DeferringStory::replace,
            DeferringStory::addPredicate
        )).forEachOrdered(l::add);
        DeferringFunction.PARSERS.stream().map(SimpleCombinedParser.bind(
            5,
            DeferringStory::replace,
            DeferringStory::addFunction
        )).forEachOrdered(l::add);
        DeferringPrint.PARSERS.stream()
            .map(SimpleCombinedParser.bind(6, DeferringStory::replace, DeferringStory::addPrint))
            .forEachOrdered(l::add);
        DeferringRoutine.PARSERS.stream()
            .map(SimpleCombinedParser.bind(7, DeferringStory::replace, DeferringStory::addRoutine))
            .forEachOrdered(l::add);
        l.add(new SimpleCombinedParser<>(8, DeferringAction.PARSER, DeferringStory::replace, DeferringStory::addAction));
        l.add(new SimpleCombinedParser<>(
            9,
            DeferringTable.DeferringContinuation.PARSER,
            DeferringStory::replace,
            DeferringStory::addContinuation
        ));
        l.add(new SimpleCombinedParser<>(10, DeferringTable.PARSER, DeferringStory::replace, DeferringStory::addTable));
        l.add(new SimpleCombinedParser<>(11, DeferringAlias.PARSER, DeferringStory::replace, DeferringStory::addAlias));
        DeferringConditionedActionRule.PARSERS.stream().map(SimpleCombinedParser.bind(
            12,
            DeferringStory::replace,
            DeferringStory::addRule
        )).forEachOrdered(l::add);
        l.add(new SimpleCombinedParser<>(13, DeferringNamedRule.PARSER, DeferringStory::replace, DeferringStory::addRule));
        DeferringActionRule.PARSERS.stream()
            .map(SimpleCombinedParser.bind(14, DeferringStory::replace, DeferringStory::addRule))
            .forEachOrdered(l::add);
        DeferringSimpleRule.PARSERS.stream()
            .map(SimpleCombinedParser.bind(15, DeferringStory::replace, DeferringStory::addRule))
            .forEachOrdered(l::add);
        DeferringDefault.PARSERS.stream()
            .map(SimpleCombinedParser.bind(16, DeferringStory::replace, DeferringStory::addDefault))
            .forEachOrdered(l::add);
        DeferringValue.PARSERS.stream()
            .map(SimpleCombinedParser.bind(17, DeferringStory::replace, DeferringStory::addValue))
            .forEachOrdered(l::add);
        
        CPARSERS = Collections.unmodifiableCollection(l);
    }
    @Override
    public Stream<? extends CombinedParser> get() {
        return CPARSERS.stream();
    }
}
