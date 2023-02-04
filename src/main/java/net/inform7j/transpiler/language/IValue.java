package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IValue extends IStory.Element {
	Optional<? extends IProperty> property();
	IObject holder();
	TokenString value();
}
