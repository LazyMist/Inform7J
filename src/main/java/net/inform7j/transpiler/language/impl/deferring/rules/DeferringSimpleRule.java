package net.inform7j.transpiler.language.impl.deferring.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringFunction;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.rules.ISimpleRule;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

public final class DeferringSimpleRule extends DeferringRule implements ISimpleRule {
	public static Function<ParseContext,DeferringSimpleRule> simpleTriggerFactory(SimpleTrigger trigger) {
		return ctx -> {
			return new DeferringSimpleRule(ctx, DeferringFunction.getNextBody(ctx.supplier()), trigger);
		};
	}
	public static final String CAPTURE_RULE_CONDITION = "ruleCondition";
	public static final List<Parser<DeferringSimpleRule>> PARSERS = Collections.unmodifiableList(Arrays.asList(
			new Parser<>(
					TokenPattern.quoteIgnoreCase("When play begins")
					.concat(
							TokenPattern.quoteIgnoreCase("(this is")
							.concatOptionalIgnoreCase("the")
							.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_NAME))
							.concatIgnoreCase("rule)").omittable()
							)
					.concat(":").concat(ENDLINE)
					/*Pattern.compile("^When play begins(?> \\(this is(?: the)? (?<name>[^\"\\.]+?) rule\\))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
					simpleTriggerFactory(SimpleTrigger.PLAY_START)),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("When play ends")
					.concat(
							TokenPattern.quoteIgnoreCase("(this is")
							.concatOptionalIgnoreCase("the")
							.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_NAME))
							.concatIgnoreCase("rule)").omittable()
							)
					.concat(":").concat(ENDLINE)
					/*Pattern.compile("^When play ends(?> \\(this is(?: the)? (?<name>[^\"\\.]+?) rule\\))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
					simpleTriggerFactory(SimpleTrigger.PLAY_END)),
			new Parser<>(
					AN.omittable()
					.concat(TokenPattern.quote("every turn").orIgnoreCase("everyturn"))
					.concatIgnoreCase("rule")
					.concat(
							TokenPattern.quoteIgnoreCase("(this is")
							.concatOptionalIgnoreCase("the")
							.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_NAME))
							.concatIgnoreCase("rule)").omittable()
							)
					.concat(":").concat(ENDLINE)
					/*Pattern.compile("^(?:an )?every ?turn rule(?: \\(this is(?: the)? (?<name>[^\"\\.]+?) rule\\))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
					simpleTriggerFactory(SimpleTrigger.EVERY_TURN)),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("Every turn")
					.concat(TokenPattern.quoteIgnoreCase("while").orIgnoreCase("when").concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_CONDITION)).omittable())
					.concat(":").concat(ENDLINE)
					/*Pattern.compile("^Every turn(?: (?:while|when) (?<condition>.+?))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
					simpleTriggerFactory(SimpleTrigger.EVERY_TURN)),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("a").omittable()
					.concatIgnoreCase("postimport rule")
					.concat(
							TokenPattern.quoteIgnoreCase("(this is")
							.concatOptionalIgnoreCase("the")
							.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_NAME))
							.concatIgnoreCase("rule)").omittable()
							)
					.concat(":").concat(ENDLINE)
					/*Pattern.compile("^(?:a )?postimport rule(?: \\(this is(?: the)? (?<name>[^\"\\.]+?) rule\\))?:\\s*$", Pattern.CASE_INSENSITIVE)*/,
					simpleTriggerFactory(SimpleTrigger.POST_IMPORT))
			));
	
	
	public final SimpleTrigger TRIGGER;
	public final Optional<TokenString> CONDITION;
	
	public DeferringSimpleRule(DeferringStory story, Source source, IStatement bODY, SimpleTrigger tRIGGER, Optional<TokenString> nAME, Optional<TokenString> cONDITION) {
		super(story, source, nAME, bODY);
		TRIGGER = tRIGGER;
		CONDITION = cONDITION;
	}
	public DeferringSimpleRule(DeferringStory story, Source source, IStatement bODY, SimpleTrigger tRIGGER, Optional<TokenString> nAME) {
		this(story, source, bODY, tRIGGER, nAME, Optional.empty());
	}
	
	public DeferringSimpleRule(ParseContext ctx, IStatement body, SimpleTrigger trigger) {
		super(ctx, body);
		TRIGGER = trigger;
		CONDITION = ctx.result().capOpt(CAPTURE_RULE_CONDITION);
	}

	@Override
	public SimpleTrigger trigger() {
		return TRIGGER;
	}

	@Override
	public Optional<TokenString> condition() {
		return CONDITION;
	}
}
