package net.inform7j.transpiler.language.impl.deferring;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.tokenizer.Token.SourcedToken;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;

public record RawLineStatement(TokenString raw, long line, Path src, Source source) implements IStatement, Supplier<RawLineStatement> {
	public RawLineStatement(List<SourcedToken> raw) {
		this(new TokenString(raw.stream().map(SourcedToken::tok)), raw.get(0).line(), raw.get(0).src(), raw.get(0).source());
	}
	@Override
	public List<IStatement> blockContents() {
		return null;
	}
	@Override
	public RawLineStatement get() {
		return this;
	}
	@Override
	public boolean isBlank() {
		return raw.stream().allMatch(TokenPredicate.IS_WHITESPACE);
	}
	@Override
	public String toString(String indent) {
		return String.format("Line %d @ %s:%s%s", line, src, indent, raw);
	}
}
