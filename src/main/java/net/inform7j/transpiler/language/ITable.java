package net.inform7j.transpiler.language;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface ITable<T extends ITable.IColumn<T>> extends Element {
	interface IColumn<T extends IColumn<T>> {
		ITable<? extends T> table();
		TokenString name();
		Optional<? extends IKind> explicitKind();
	}
	Optional<TokenString> number();
	Optional<TokenString> name();
	Stream<? extends T> columns();
	Stream<? extends Map<T, TokenString>> rows();
}
