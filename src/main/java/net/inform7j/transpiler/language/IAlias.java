package net.inform7j.transpiler.language;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IAlias<T extends Element> extends Element {
	public static sealed interface AliasPattern permits LiteralAliasPattern, PlaceholderAliasPattern {
		public String raw();
	}
	public static record LiteralAliasPattern(String raw) implements AliasPattern {}
	public static record PlaceholderAliasPattern(String raw) implements AliasPattern {}
	public static Stream<AliasPattern> parseAliasText(String text) {
		Stream.Builder<AliasPattern> ret = Stream.builder();
		StringBuilder b = new StringBuilder();
		int comment = 0;
		for(char c:text.toCharArray()) {
			switch(c) {
			case '[':
				comment++;
				if(comment == 1) {
					if(!b.isEmpty()) {
						ret.accept(new LiteralAliasPattern(b.toString()));
						b = new StringBuilder();
					}
					continue;
				}
				break;
			case ']':
				comment--;
				if(comment<0) throw new IllegalStateException("Illegal alias");
				else if(comment == 0) {
					ret.accept(new PlaceholderAliasPattern(b.toString()));
					b = new StringBuilder();
					continue;
				}
			}
			b.append(c);
		}
		return ret.build();
	}
	@SuppressWarnings("unchecked")
	public default <U extends Element> Optional<? extends IAlias<U>> cast(Class<U> clazz) {
		final T orig = original();
		if(clazz.isInstance(orig)) return Optional.of((IAlias<U>)this);
		return Optional.empty();
	}
	public List<TokenString> aliases();
	public T original();
}
