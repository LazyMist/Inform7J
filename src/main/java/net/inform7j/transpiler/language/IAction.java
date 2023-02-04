package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IAction extends Element {
	TokenString name();
	Optional<? extends IKind> primary();
	Optional<? extends IKind> secondary();
	Optional<TokenString> requirements();
}
