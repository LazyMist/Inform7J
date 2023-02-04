package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IAction extends Element {
	public TokenString name();
	public Optional<? extends IKind> primary();
	public Optional<? extends IKind> secondary();
	public Optional<TokenString> requirements();
}
