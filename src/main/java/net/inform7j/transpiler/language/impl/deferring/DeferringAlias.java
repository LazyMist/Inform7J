package net.inform7j.transpiler.language.impl.deferring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IAlias;
import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringAlias extends DeferringImpl implements IAlias<DeferringImpl> {
	public static final String CAPTURE_ALIAS = "alias";
	public static final String CAPTURE_ORIGINAL = "original";
	private static final TokenPattern PATTERN_PREFIX = TokenPattern.quoteIgnoreCase("understand").concat(TokenPattern.Single.STRING.capture(CAPTURE_ALIAS))
			.concat(TokenPattern.quoteIgnoreCase("and").orIgnoreCase("or").concat(TokenPattern.Single.STRING.capture(CAPTURE_ALIAS)).loop().omittable())
			.concatIgnoreCase("as");
	
	public static TokenPattern getAliasPattern(String replacement, String ...replacements) {
		TokenPattern repl = new TokenPattern.Replacement(replacement, false);
		for(String s:replacements) {
			repl = repl.or(new TokenPattern.Replacement(s, false));
		}
		return PATTERN_PREFIX.concat(repl.capture(CAPTURE_ORIGINAL))
			.concat(ENDMARKER);
	}
	
	public static final Parser<DeferringAlias> PARSER = new Parser<>(
			getAliasPattern(DeferringStory.ACTION_NAME_REPLACEMENT, DeferringStory.OBJECT_NAME_REPLACEMENT)
			/*Pattern.compile("^Understand \"(?<alias>.+?)\" as (?<original>.+?)(?:\\.|$)", Pattern.CASE_INSENSITIVE)*/,
			DeferringAlias::new
			);
	
	public final List<TokenString> ALIASES;
	public final TokenString ORIGINAL;
	public DeferringAlias(DeferringStory story, Source source, List<TokenString> aLIASES, TokenString oRIGINAL) {
		super(story, source);
		ALIASES = Collections.unmodifiableList(new ArrayList<>(aLIASES));
		ORIGINAL = oRIGINAL;
	}
	public DeferringAlias(ParseContext ctx) {
		super(ctx);
		TokenPattern.Result m = ctx.result();
		ALIASES = Collections.unmodifiableList(new ArrayList<>(m.capMulti(CAPTURE_ALIAS)));
		ORIGINAL = m.cap(CAPTURE_ORIGINAL);
	}
	@Override
	public List<TokenString> aliases() {
		return ALIASES;
	}
	@Override
	public DeferringImpl original() {
		DeferringImpl ret = story.getObject(ORIGINAL);
		if(ret == null) ret = story.getAction(ORIGINAL);
		return ret;
	}
	@SuppressWarnings("unchecked")
	@Override
	public <U extends Element> Optional<? extends IAlias<U>> cast(Class<U> clazz) {
		DeferringImpl original = original();
		if(original == null || clazz.isInstance(original)) return Optional.of((IAlias<U>)this);
		return Optional.empty();
	}
}
