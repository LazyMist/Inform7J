package net.inform7j.transpiler.language.impl.deferring.rules;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.rules.IConditionedActionRule;
import net.inform7j.transpiler.parser.FunctionParserProvider;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class DeferringConditionedActionRule extends DeferringActionRule implements IConditionedActionRule {
    public static Function<ParseContext, DeferringConditionedActionRule> actionTriggerFactory(ActionTrigger trig) {
        return ctx -> new DeferringConditionedActionRule(ctx, FunctionParserProvider.getNextBody(ctx.supplier()), trig);
    }
    public static final String CAPTURE_CONDITION = "condition";
    public static final TokenPattern WHILE = new Single(new TokenPredicate(Pattern.compile("while|when", Pattern.CASE_INSENSITIVE)));
    @SuppressWarnings("hiding")
    public static final List<Parser<DeferringConditionedActionRule>> PARSERS = Collections.unmodifiableList(Arrays.asList(
        new Parser<>(
            TokenPattern.quoteIgnoreCase("After")
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
                .concat(
                    WHILE.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_CONDITION))
                        .omittable()
                )
                .concat(":").concat(ENDLINE)
            /*Pattern.compile("^After (?<action>.+?)(?: (?<params>.+?))?(?: (?:while|when) (?<condition>.+?))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
            actionTriggerFactory(ActionTrigger.POST)),
        new Parser<>(
            TokenPattern.quoteIgnoreCase("Before")
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
                .concat(
                    WHILE.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_CONDITION))
                        .omittable()
                )
                .concat(":").concat(ENDLINE)
            /*Pattern.compile("^Before (?<action>.+?)(?: (?<params>.+?))?(?: (?:while|when) (?<condition>.+?))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
            actionTriggerFactory(ActionTrigger.PRE)),
        new Parser<>(
            TokenPattern.quoteIgnoreCase("Instead of")
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
                .concat(
                    WHILE.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_CONDITION))
                        .omittable()
                )
                .concat(":").concat(ENDLINE)
            /*Pattern.compile("^Before (?<action>.+?)(?: (?<params>.+?))?(?: (?:while|when) (?<condition>.+?))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
            actionTriggerFactory(ActionTrigger.INSTEAD))
    ));
    
    public final List<TokenString> condition;
    public DeferringConditionedActionRule(
        DeferringStory story,
        Source source,
        TokenString action,
        ActionTrigger trigger,
        Optional<TokenString> name,
        Optional<TokenString> condition,
        IStatement bODY
    ) {
        super(story, source, bODY, trigger, action, name);
        this.condition = condition.stream().toList();
    }
    
    public DeferringConditionedActionRule(ParseContext ctx, IStatement body, ActionTrigger trigger) {
        super(ctx, body, trigger);
        condition = ctx.result().capMulti(CAPTURE_CONDITION);
    }
    
    @Override
    public List<TokenString> condition() {
        return condition;
    }
}
