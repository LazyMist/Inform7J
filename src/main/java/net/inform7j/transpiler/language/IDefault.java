package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IDefault extends Element {
	public Optional<? extends IProperty> property();
	public Optional<? extends IObject> object();
	public TokenString value();
}
