package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IDefault extends Element {
	Optional<? extends IProperty> property();
	Optional<? extends IObject> object();
	TokenString value();
}
