package net.inform7j.transpiler.language.rules;

import java.util.Optional;

import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IRule extends Element {
	public IStatement body();
	public Optional<TokenString> name();
}
