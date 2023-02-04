package net.inform7j.transpiler.language;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface ITable<T extends ITable.IColumn<T>> extends Element {
	public static interface IColumn<T extends IColumn<T>> {
		public ITable<? extends T> table();
		public TokenString name();
		public Optional<? extends IKind> explicitKind();
	}
	public Optional<TokenString> number();
	public Optional<TokenString> name();
	public Stream<? extends T> columns();
	public Stream<? extends Map<T, TokenString>> rows();
}
