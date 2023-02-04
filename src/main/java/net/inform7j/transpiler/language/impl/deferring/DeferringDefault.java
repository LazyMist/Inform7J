package net.inform7j.transpiler.language.impl.deferring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IDefault;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringDefault extends DeferringImpl implements IDefault {
	public static final TokenPattern USUALLY = new TokenPattern.Single(new TokenPredicate(Pattern.compile("usually|normally", Pattern.CASE_INSENSITIVE)));
	public static final String CAPTURE_PROPERTY = "property";
	public static final String CAPTURE_OBJECT = "object";
	public static final String CAPTURE_VALUE = "value";
	public static final List<Parser<DeferringDefault>> PARSERS = Collections.unmodifiableList(Arrays.asList(
			new Parser<>(
					WORD_LOOP.capture(CAPTURE_PROPERTY).concatIgnoreCase("of").omittable()
					.concat(new TokenPattern.Replacement(DeferringStory.OBJECT_NAME_REPLACEMENT, false).capture(CAPTURE_OBJECT))
					.concatIgnoreCase("is").concat(USUALLY)
					.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VALUE))
					.concat(ENDMARKER)
					/*Pattern.compile("^(?:(?<property>.+?) of )?(?<object>.+?) is (?:usually|normally) (?<value>.+?)\\s*(?:\\.|;|$)", Pattern.CASE_INSENSITIVE)*/,
					DeferringDefault::new
					),
			new Parser<>(
					WORD_LOOP.capture(CAPTURE_PROPERTY).concatIgnoreCase("of").omittable()
					.concat(new TokenPattern.Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_OBJECT))
					.concatIgnoreCase("is").concat(USUALLY)
					.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VALUE))
					.concat(ENDMARKER)
					/*Pattern.compile("^(?:(?<property>.+?) of )?(?<object>.+?) is (?:usually|normally) (?<value>.+?)\\s*(?:\\.|;|$)", Pattern.CASE_INSENSITIVE)*/,
					DeferringDefault::new
					)
			));

	public final TokenString LABEL;
	public final TokenString VALUE;
	public final Optional<TokenString> PROPERTY;

	public DeferringDefault(DeferringStory story, Source source, Optional<TokenString> property, TokenString lABEL, TokenString vALUE) {
		super(story, source);
		PROPERTY = property;
		LABEL = lABEL;
		VALUE = vALUE;
	}

	public DeferringDefault(ParseContext ctx) {
		super(ctx);
		TokenPattern.Result m = ctx.result();
		PROPERTY = m.capOpt(CAPTURE_PROPERTY);
		LABEL = m.cap(CAPTURE_OBJECT);
		VALUE = m.cap(CAPTURE_VALUE);
	}

	@Override
	public Optional<? extends DeferringProperty> property() {
		return PROPERTY.map(p -> story.getProperty(story().getObject(p).getType(), LABEL));
	}

	@Override
	public Optional<? extends DeferringObject> object() {
		if(PROPERTY.isPresent()) return Optional.empty();
		return Optional.of(story().getObject(LABEL));
	}

	@Override
	public TokenString value() {
		return VALUE;
	}
}
