package net.inform7j.transpiler.language;

import java.util.stream.Stream;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IEnum extends Element {
	enum Category {
		KIND, OBJECT, PROPERTY
	}
	Category category();
	TokenString name();
	Stream<TokenString> streamValues();
}
