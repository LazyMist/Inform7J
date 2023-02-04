package net.inform7j.transpiler.language.rules;

import java.util.Optional;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface ISimpleRule extends IRule {
	public static enum SimpleTrigger {
		PLAY_START,
		PLAY_END,
		POST_IMPORT,
		EVERY_TURN;
	}
	
	public SimpleTrigger trigger();
	
	public Optional<TokenString> condition();
}
