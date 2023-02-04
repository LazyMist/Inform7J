package net.inform7j.transpiler.language.impl.deferring.rules;

import java.util.Optional;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.rules.IRule;
import net.inform7j.transpiler.tokenizer.TokenString;

public abstract sealed class DeferringRule extends DeferringImpl implements IRule permits DeferringActionRule, DeferringSimpleRule, DeferringNamedRule {
	public static final String CAPTURE_RULE_NAME = "ruleName";
	
	public final IStatement BODY;
	public final Optional<TokenString> NAME;

	public DeferringRule(DeferringStory story, Source source, Optional<TokenString> nAME, IStatement bODY) {
		super(story, source);
		BODY = bODY;
		NAME = nAME;
	}
	public DeferringRule(ParseContext ctx, IStatement bODY) {
		super(ctx);
		BODY = bODY;
		NAME = ctx.result().capOpt(CAPTURE_RULE_NAME);
	}

	@Override
	public IStatement body() {
		return BODY;
	}
	
	@Override
	public Optional<TokenString> name() {
		return NAME;
	}
}
