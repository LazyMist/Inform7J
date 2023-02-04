package net.inform7j.transpiler.language.impl.deferring;

import java.util.function.Function;
import java.util.regex.Pattern;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.util.StatementSupplier;
import net.inform7j.transpiler.language.IStory;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenPattern.Single;

public abstract class DeferringImpl implements IStory.Element {
	public static final String SPECIAL_CHARS = ".,(){}";
	public static final TokenPattern AN = new Single(new TokenPredicate(Pattern.compile("an?", Pattern.CASE_INSENSITIVE))),
			HAS = new Single(new TokenPredicate(Pattern.compile("has|have", Pattern.CASE_INSENSITIVE))),
			IS = new Single(new TokenPredicate(Pattern.compile("is|are", Pattern.CASE_INSENSITIVE))),
			PLURAL_WORD = new Single(new TokenPredicate(Pattern.compile(".+s", Pattern.CASE_INSENSITIVE))),
			ENDMARKER = new Single(TokenPredicate.END_MARKER).or(TokenPattern.END),
			ENDLINE = TokenPattern.END.or("\n"),
			WORD_LOOP = Single.WORD.loop(true),
			WORD_LOOP_GREEDY = Single.WORD.loop(),
			IDENTIFIER_TOKEN = Single.WORD.or(new Single(new TokenPredicate(Token.Type.PUNCTUATION, s -> !SPECIAL_CHARS.contains(s)))),
			IDENTIFIER_LOOP = IDENTIFIER_TOKEN.loop(true),
			IDENTIFIER_LOOP_GREEDY = IDENTIFIER_TOKEN.loop(),
			NOT_ENDMARKER = new Single(TokenPredicate.END_MARKER.negate()),
			NOT_ENDMARKER_LOOP = NOT_ENDMARKER.loop(true),
			NOT_ENDMARKER_LOOP_GREEDY = NOT_ENDMARKER.loop();

	protected DeferringStory story;
	protected final Source source;
	protected DeferringImpl(DeferringStory story, Source source) {
		this.story = story;
		this.source = source;
	}
	protected DeferringImpl(ParseContext ctx) {
		this(ctx.story(), ctx.source().source());
	}
	protected DeferringImpl() {
		this(null, null);
	}
	@Override
	public final DeferringStory story() {
		return this.story;
	}
	public void setStory(DeferringStory story) {
		this.story = story;
	}
	@Override
	public final Source source() {
		return this.source;
	}

	public static record ParseContext(
			DeferringStory story,
			IStatement source,
			TokenPattern.Result result,
			StatementSupplier supplier
			) {
		public ParseContext(ParseContext ctx, TokenPattern.Result result) {
			this(ctx.story, ctx.source, result, ctx.supplier);
		}
	}

	public static record Parser<T extends DeferringImpl>(TokenPattern pattern, Function<? super ParseContext, ? extends T> factory) {
	}
}
