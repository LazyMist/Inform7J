package net.inform7j.transpiler.language.impl.deferring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IObject;
import net.inform7j.transpiler.language.IStory.BaseKind;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.util.LazyLookup;

public class DeferringObject extends DeferringImpl implements IObject {
	public static final String CAPTURE_NAME = "name";
	public static final String CAPTURE_TYPE = "type";
	public static final String CAPTURE_OWNER = "owner";
	public static final List<Parser<DeferringObject>> PARSERS = Collections.unmodifiableList(Arrays.asList(
			new Parser<>(
					TokenPattern.quoteIgnoreCase("the").omittable()
					.concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
					.concat(IS).concat(AN)
					.concat(new TokenPattern.Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_TYPE))
					.concat(TokenPattern.quoteIgnoreCase("that varies").omittable())
					.concat(ENDMARKER)
					/*Pattern.compile("^(?<name>.+?) is an? (?<type>.+?)(?> that varies)?\\.", Pattern.CASE_INSENSITIVE)*/,
					DeferringObject::new),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("the").omittable()
					.concat(new TokenPattern.Replacement(DeferringStory.OBJECT_NAME_REPLACEMENT, false).capture(CAPTURE_OWNER))
					.concat(HAS).concat(AN.omittable())
					.concat(new TokenPattern.Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_TYPE))
					.concat("called")
					.concat(TokenPattern.quoteIgnoreCase("the").omittable())
					.concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
					.concat(ENDMARKER)
					/*Pattern.compile("^(?>the )?(?<owner>.+?) has an? (?<type>.+?) called (?>the )?(?<name>.+?)\\.", Pattern.CASE_INSENSITIVE)*/,
					DeferringObject::new),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("the").omittable()
					.concat(new TokenPattern.Replacement(DeferringStory.OBJECT_NAME_REPLACEMENT, false).capture(CAPTURE_OWNER))
					.concatIgnoreCase("can be").concat(TokenPattern.quoteIgnoreCase("either").lookahead(true))
					.concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
					.concat(ENDMARKER),
					ctx -> new DeferringObject(ctx, BaseKind.TRUTH_STATE))
			));
	
	public final TokenString NAME;
	public final LazyLookup<TokenString,? extends DeferringKind> TYPE;
	
	public DeferringObject(DeferringStory story, Source source, TokenString nAME, DeferringKind tYPE) {
		super(story, source);
		NAME = nAME;
		TYPE = new LazyLookup<>(tYPE.NAME, tYPE);
	}
	public DeferringObject(DeferringStory story, Source source, TokenString nAME, BaseKind baseKind) {
		this(story, source, nAME, story.getBaseKind(baseKind));
	}
	protected DeferringObject(ParseContext ctx, LazyLookup<TokenString,? extends DeferringKind> type) {
		super(ctx);
		final TokenPattern.Result m = ctx.result();
		TokenString nam = m.cap(CAPTURE_NAME);
		nam = nam.concat(m.capOpt(CAPTURE_OWNER).map(o -> new TokenString(new Token(Token.Type.WORD, "of")).concat(o)).orElse(TokenString.EMPTY));
		NAME = nam;
		TYPE = type;
	}
	public DeferringObject(ParseContext ctx, BaseKind baseKind) {
		this(ctx, new LazyLookup<>(baseKind.writtenName, ctx.story().getBaseKind(baseKind)));
	}
	public DeferringObject(ParseContext ctx) {
		this(ctx, new LazyLookup<>(ctx.result().cap(CAPTURE_TYPE), ctx.story()::getKind));
	}
	
	@Override
	public DeferringKind getType() {
		return TYPE.get();
	}
	
	@Override
	public TokenString getTypeName() {
		return TYPE.key();
	}

	@Override
	public TokenString getName() {
		return NAME;
	}
	
	@Override
	public String toString() {
		return String.format("%s is a %s", NAME, TYPE.key());
	}
}
