package net.inform7j.transpiler.parser.provider.rule;

import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringConditionedActionRule;
import net.inform7j.transpiler.language.rules.IActionRule;
import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.SimpleCombinedParser;
import net.inform7j.transpiler.parser.provider.FunctionParserProvider;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.inform7j.transpiler.language.impl.deferring.rules.DeferringActionRule.CAPTURE_ACTION;
import static net.inform7j.transpiler.parser.Patterns.*;

public class ConditionalActionRuleProvider implements CombinedParser.Provider {
    public static Function<DeferringImpl.ParseContext, DeferringConditionedActionRule> actionTriggerFactory(IActionRule.ActionTrigger trig) {
        return ctx -> new DeferringConditionedActionRule(
            ctx.story(),
            ctx.source().source(),
            ctx.result().cap(CAPTURE_ACTION),
            trig,
            Optional.empty(),
            ctx.result().capOpt(CAPTURE_CONDITION),
            FunctionParserProvider.getNextBody(ctx.supplier())
        );
    }
    private static final String CAPTURE_CONDITION = "condition";
    private static final TokenPattern WHILE = new Single(new TokenPredicate(Pattern.compile("while|when", Pattern.CASE_INSENSITIVE)));
    private static final List<? extends CombinedParser> PARSERS = Stream.of(new DeferringImpl.Parser<>(
        TokenPattern.quoteIgnoreCase("After")
            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
            .concat(
                WHILE.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_CONDITION))
                    .omittable()
            )
            .concat(":").concat(ENDLINE),
        actionTriggerFactory(IActionRule.ActionTrigger.POST)), new DeferringImpl.Parser<>(
        TokenPattern.quoteIgnoreCase("Before")
            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
            .concat(
                WHILE.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_CONDITION))
                    .omittable()
            )
            .concat(":").concat(ENDLINE),
        actionTriggerFactory(IActionRule.ActionTrigger.PRE)), new DeferringImpl.Parser<>(
        TokenPattern.quoteIgnoreCase("Instead of")
            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
            .concat(
                WHILE.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_CONDITION))
                    .omittable()
            )
            .concat(":").concat(ENDLINE),
        actionTriggerFactory(IActionRule.ActionTrigger.INSTEAD))
    ).map(SimpleCombinedParser.bind(
        12,
        DeferringStory::replace,
        DeferringStory::addRule
    )).toList();
    @Override
    public Stream<? extends CombinedParser> get() {
        return PARSERS.stream();
    }
}
