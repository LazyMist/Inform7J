package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IValue extends IStory.Element {
	public Optional<? extends IProperty> property();
	public IObject holder();
	public TokenString value();
}
