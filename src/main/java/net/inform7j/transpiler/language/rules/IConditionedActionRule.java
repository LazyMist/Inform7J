package net.inform7j.transpiler.language.rules;

import java.util.List;
import java.util.Optional;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IConditionedActionRule extends IActionRule {
	public List<TokenString> condition();
	@Override
	default Optional<TokenString> name() {
		return Optional.empty();
	}
}
