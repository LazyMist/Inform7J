package net.inform7j.transpiler.language;

import java.util.stream.Stream;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IEnum extends Element {
	public static enum Category {
		KIND, OBJECT, PROPERTY;
	}
	public Category category();
	public TokenString name();
	public Stream<TokenString> streamValues();
}
