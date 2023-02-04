package net.inform7j.transpiler.language;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IAlias<T extends Element> extends Element {
	sealed interface AliasPattern permits LiteralAliasPattern, PlaceholderAliasPattern {
		String raw();
	}
	record LiteralAliasPattern(String raw) implements AliasPattern {}
	record PlaceholderAliasPattern(String raw) implements AliasPattern {}
	static Stream<AliasPattern> parseAliasText(String text) {
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
				break;
			}
			b.append(c);
		}
		return ret.build();
	}
	@SuppressWarnings("unchecked")
	default <U extends Element> Optional<? extends IAlias<U>> cast(Class<U> clazz) {
		final T orig = original();
		if(clazz.isInstance(orig)) return Optional.of((IAlias<U>)this);
		return Optional.empty();
	}
	List<TokenString> aliases();
	T original();
}
