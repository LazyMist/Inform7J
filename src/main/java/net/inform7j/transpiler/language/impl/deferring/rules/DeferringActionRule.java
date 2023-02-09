package net.inform7j.transpiler.language.impl.deferring.rules;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringAction;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.rules.IActionRule;
import net.inform7j.transpiler.parser.FunctionParserProvider;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public sealed class DeferringActionRule extends DeferringRule implements IActionRule
    permits DeferringConditionedActionRule {
    public static Function<? super ParseContext, ? extends DeferringActionRule> factoryWithTrigger(ActionTrigger trigger) {
        return ctx -> new DeferringActionRule(ctx, FunctionParserProvider.getNextBody(ctx.supplier()), trigger);
    }
    public static final String CAPTURE_ACTION = "action";
    public static final String CAPTURE_TARGET = "vname";
    public static final List<Parser<? extends DeferringActionRule>> PARSERS = Collections.unmodifiableList(Arrays.asList(
        new Parser<>(
            TokenPattern.quoteIgnoreCase("Carry out")
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
                .concat(
                    TokenPattern.quoteIgnoreCase("someone (called")
                        .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_TARGET))
                        .concat(")").omittable()
                )
                .concat(
                    TokenPattern.quoteIgnoreCase("(this is")
                        .concatOptionalIgnoreCase("the")
                        .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_NAME))
                        .concatIgnoreCase("rule)").omittable()
                )
                .concat(":").concat(ENDLINE)
            /*Pattern.compile("^Carry out (?<action>.+?)(?: someone \\(called (?<vname>.+?)\\))?(?: \\(this is the (?<name>.+?) rule\\))?:\\s*+$", Pattern.CASE_INSENSITIVE)*/,
            factoryWithTrigger(ActionTrigger.EXECUTE)),
        new Parser<>(
            TokenPattern.quoteIgnoreCase("Check")
                .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_ACTION))
                .concat(
                    TokenPattern.quoteIgnoreCase("someone (called")
                        .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_TARGET))
                        .concat(")").omittable()
                )
                .concat(
                    TokenPattern.quoteIgnoreCase("(this is")
                        .concatOptionalIgnoreCase("the")
                        .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_NAME))
                        .concatIgnoreCase("rule)").omittable()
                )
                .concat(":").concat(ENDLINE)
            /*Pattern.compile("^Check (?<action>.+?)(?: someone \\(called (?<vname>.+?)\\))?(?: \\(this is the (?<name>.+?) rule\\))?:\\s*+$", Pattern.CASE_INSENSITIVE)*/,
            factoryWithTrigger(ActionTrigger.CHECK))
    ));
    
    public final ActionTrigger trigger;
    public final TokenString action;
    
    public DeferringActionRule(
        DeferringStory story,
        Source source,
        IStatement body,
        ActionTrigger trigger,
        TokenString action,
        Optional<TokenString> name
    ) {
        super(story, source, name, body);
        this.trigger = trigger;
        this.action = action;
    }
    
    public DeferringActionRule(ParseContext ctx, IStatement body, ActionTrigger trigger) {
        super(ctx, body);
        this.trigger = trigger;
        this.action = ctx.result().cap(CAPTURE_ACTION);
    }
    
    @Override
    public ActionTrigger trigger() {
        return trigger;
    }
    
    @Override
    public DeferringAction action() {
        return story.getAction(action);
    }
    
}
