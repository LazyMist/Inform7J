package net.inform7j.transpiler.language.impl.deferring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IValue;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.util.LazyLookup;

public class DeferringValue extends DeferringImpl implements IValue {
	public static final TokenPattern FILE = new TokenPattern.Single(new TokenPredicate(Pattern.compile("file", Pattern.CASE_INSENSITIVE)));
	public static final String CAPTURE_NAME = "name", CAPTURE_OBJECT = "object", CAPTURE_VALUE = "value";
	private static final TokenPattern.Replacement OBJ = new TokenPattern.Replacement(DeferringStory.OBJECT_NAME_REPLACEMENT, false),
			PROP = new TokenPattern.Replacement(DeferringStory.OBJECT_PROPERTY_NAME_REPLACEMENT, false),
			GPROP = new TokenPattern.Replacement(DeferringStory.PROPERTY_NAME_REPLACEMENT, false);
	public static final List<Parser<DeferringValue>> PARSERS = Collections.unmodifiableList(Arrays.asList(
			new Parser<>(
					TokenPattern.Single.STRING.capture(CAPTURE_VALUE)
					.concat(ENDMARKER)
					/*Pattern.compile("^(?<value>\"[^\"]++\")(?>\\s*+(?>\\.|;))?")*/,
					DeferringValue::Desc
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to decide").orIgnoreCase("Rule for").lookahead(true)
					.concat(TokenPattern.quoteIgnoreCase("the").omittable())
					.concat(OBJ.capture(CAPTURE_OBJECT, DeferringStory.PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE))
					.concat(TokenPattern.quoteIgnoreCase("is not").orIgnoreCase("isn't")).concat(PROP.capture(CAPTURE_NAME))
					.concat(ENDMARKER),
					DeferringValue::FalseProp
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to decide").orIgnoreCase("Rule for").lookahead(true)
					.concat(TokenPattern.quoteIgnoreCase("the").omittable())
					.concat(OBJ.capture(CAPTURE_OBJECT, DeferringStory.PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE))
					.concatIgnoreCase("is").concat(PROP.capture(CAPTURE_NAME))
					.concat(ENDMARKER),
					DeferringValue::TrueProp
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to decide").orIgnoreCase("Rule for").lookahead(true)
					.concat(TokenPattern.quoteIgnoreCase("the").omittable())
					.concat(GPROP.capture(CAPTURE_NAME).concatIgnoreCase("of").omittable())
					.concat(OBJ.capture(CAPTURE_OBJECT))
					.concatIgnoreCase("is").concat(TokenPattern.quoteIgnoreCase("a kind").orIgnoreCase("an action").lookahead(true))
					.concat(TokenPattern.quoteIgnoreCase("called").or(TokenPattern.quoteIgnoreCase("the").concatIgnoreCase("file")).omittable())
					.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VALUE))
					.concat(ENDMARKER)
					/*Pattern.compile("^(?>the )?(?>(?<name>[^\"\\.]+?) of )?(?<object>[^\"\\.]+?)\\s+(?>is) (?>called |the file )?(?<value>(?>\\{\\s*+)?(?:(?:\"[^\"]*+\"|[^\"{},\\.;]+)(?:\\s*+,\\s*+)?)+(?>\\s*+\\})?)\\s*+(?:\\.|;|$)", Pattern.CASE_INSENSITIVE)*/,
					DeferringValue::new
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to decide").orIgnoreCase("Rule for").lookahead(true)
					.concat(TokenPattern.quoteIgnoreCase("the").omittable())
					.concat(OBJ.capture(CAPTURE_OBJECT, DeferringStory.PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE))
					.concatIgnoreCase("has").concat(AN)
					.concat(PROP.capture(CAPTURE_NAME))
					.concat(TokenPattern.Single.STRING.capture(CAPTURE_VALUE))
					.concat(ENDMARKER),
					DeferringValue::new
					)
			));
	
	public final LazyLookup<TokenString, DeferringObject> OWNER;
	public final TokenString VALUE;
	public final Optional<LazyLookup<TokenString, DeferringProperty>> NAME;

	public DeferringValue(DeferringStory story, Source source, DeferringObject oWNER, Optional<? extends DeferringProperty> nAME, TokenString vALUE) {
		super(story, source);
		NAME = nAME.map(p -> new LazyLookup<>(p.NAME, p));
		OWNER = new LazyLookup<>(oWNER.NAME, oWNER);
		VALUE = vALUE;
	}
	
	private DeferringValue(ParseContext ctx, TokenString owner, Optional<TokenString> name) {
		super(ctx);
		final TokenPattern.Result m = ctx.result();
		OWNER = new LazyLookup<>(owner, story::getObject);
		NAME = name.map(n -> new LazyLookup<>(n, p -> story.getProperty(OWNER.get().getType(), p)));
		VALUE = m.cap(CAPTURE_VALUE);
	}
	
	private DeferringValue(ParseContext ctx, TokenString value) {
		super(ctx);
		final TokenPattern.Result m = ctx.result();
		OWNER = new LazyLookup<>(m.cap(CAPTURE_OBJECT), story::getObject);
		NAME = m.capOpt(CAPTURE_NAME).map(n -> new LazyLookup<>(n, p -> story.getProperty(OWNER.get().getType(), p)));
		VALUE = value;
	}
	
	public static DeferringValue Desc(ParseContext ctx) {
		return new DeferringValue(ctx, new TokenString(new Token(Token.Type.WORD, "it")), Optional.of(new TokenString(new Token(Token.Type.WORD, "description"))));
	}
	
	public static DeferringValue TrueProp(ParseContext ctx) {
		return new DeferringValue(ctx, new TokenString(new Token(Token.Type.WORD, "true")));
	}
	
	public static DeferringValue FalseProp(ParseContext ctx) {
		return new DeferringValue(ctx, new TokenString(new Token(Token.Type.WORD, "false")));
	}
	
	public DeferringValue(ParseContext ctx) {
		super(ctx);
		final TokenPattern.Result m = ctx.result();
		OWNER = new LazyLookup<>(m.cap(CAPTURE_OBJECT), story::getObject);
		NAME = m.capOpt(CAPTURE_NAME).map(n -> new LazyLookup<>(n, p -> story.getProperty(OWNER.get().getType(), p)));
		VALUE = m.cap(CAPTURE_VALUE);
	}

	@Override
	public DeferringObject holder() {
		return OWNER.get();
	}

	@Override
	public Optional<? extends DeferringProperty> property() {
		return NAME.map(LazyLookup::get);
	}

	@Override
	public TokenString value() {
		return VALUE;
	}

	@Override
	public String toString() {
		return String.format("%s of %s is %s", NAME.map(LazyLookup::key).orElse(TokenString.EMPTY), OWNER.key(), VALUE);
	}
}
