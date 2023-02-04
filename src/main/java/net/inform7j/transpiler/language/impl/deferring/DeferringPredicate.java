package net.inform7j.transpiler.language.impl.deferring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStory;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringPredicate extends DeferringFunction {
	public static final TokenPattern WHETHER = new TokenPattern.Single(new TokenPredicate(Pattern.compile("if|whether", Pattern.CASE_INSENSITIVE)));
	private static final TokenPattern DEFINITION = TokenPattern.quoteIgnoreCase("definition:");
	public static final String CAPTURE_THIS_VAR = "thisVar";
	public static final String CAPTURE_KIND = "kind";
	public static final String CAPTURE_BODY = "body";
	@SuppressWarnings("hiding")
	public static final List<Parser<DeferringPredicate>> PARSERS = Collections.unmodifiableList(Arrays.asList(
			new Parser<>(
					DEFINITION.concat(WHETHER)
					.concat(PARAM_GLOB.loop())
					.concat(":")
					.concat(ENDLINE)
					/*Pattern.compile("^to decide (?:if|whether) (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+):\\s*+$", Pattern.CASE_INSENSITIVE)*/,
					DeferringPredicate::new),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("definition:").concat(AN)
					.concat(WORD_LOOP.capture(CAPTURE_KIND))
					.concat(TokenPattern.quoteIgnoreCase("(called").concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_THIS_VAR)).concat(")").omittable())
					.concat(TokenPattern.quoteIgnoreCase("is").orIgnoreCase("has").orIgnoreCase("can"))
					.concat(PARAM_GLOB.loop(true))
					.concat(":")
					.concat(ENDLINE)
					/*Pattern.compile("^Definition: an? (?<kind>.+?)(?: \\(called (?<thisVar>.+?)\\))? (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+):\\s*+$", Pattern.CASE_INSENSITIVE)*/,
					ctx -> {
						final TokenPattern.Result m = ctx.result();
						TokenString thisVar = m.capOpt(CAPTURE_THIS_VAR).orElse(new TokenString(new Token(Token.Type.WORD, "it")));
						return new DeferringPredicate(ctx.story(), ctx.source().source(), Stream.concat(Stream.of(new DeferredParameter(ctx.story(), ctx.source().source(), thisVar, m.cap(CAPTURE_KIND))), DeferringFunction.parseSignatures(ctx, m.capMulti(CAPTURE_NAME_PARAMS))), getNextBody(ctx.supplier()));
					}),
			new Parser<>(
					DEFINITION.concat(AN)
					.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_KIND))
					.concat(TokenPattern.quoteIgnoreCase("(called").concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_THIS_VAR)).concat(")").omittable())
					.concat(TokenPattern.quoteIgnoreCase("is").orIgnoreCase("has").orIgnoreCase("can"))
					.concat(PARAM_GLOB.loop(true))
					.concat("if")
					.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_BODY))
					.concat(ENDMARKER)
					/*Pattern.compile("^Definition: an? (?<kind>.+?)(?: \\(called (?<thisVar>.+?)\\))? (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+):\\s*+$", Pattern.CASE_INSENSITIVE)*/,
					ctx -> {
						final TokenPattern.Result m = ctx.result();
						TokenString thisVar = m.capOpt(CAPTURE_THIS_VAR).orElse(new TokenString(new Token(Token.Type.WORD, "it")));
						return new DeferringPredicate(ctx.story(), ctx.source().source(),
								Stream.concat(
										Stream.of(new DeferredParameter(ctx.story(), ctx.source().source(), thisVar, m.cap(CAPTURE_KIND))),
										DeferringFunction.parseSignatures(ctx, m.capMulti(CAPTURE_NAME_PARAMS))
										),
								new RawLineStatement(new TokenString(Stream.concat(Token.Generator.parseLiteral("decide on"), m.cap(CAPTURE_BODY).stream())), ctx.source().line(), ctx.source().src(), ctx.source().source())
								);
					}),
			new Parser<>(
					DEFINITION.concatOptionalIgnoreCase("the")
					.concat(NOT_ENDMARKER.capture(CAPTURE_NAME_PARAMS).loop(true))
					.concat(TokenPattern.quoteIgnoreCase("is").orIgnoreCase("has").orIgnoreCase("can").capture(CAPTURE_NAME_PARAMS))
					.concat(PARAM_GLOB.loop(true))
					.concat(":")
					.concat(ENDLINE)
					/*Pattern.compile("^Definition: an? (?<kind>.+?)(?: \\(called (?<thisVar>.+?)\\))? (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+):\\s*+$", Pattern.CASE_INSENSITIVE)*/,
					DeferringPredicate::new)
			));
	
	public DeferringPredicate(DeferringStory story, Source source, Stream<? extends SignatureElement> params, IStatement body) {
		super(story, source, IStory.BaseKind.TRUTH_STATE, params, body);
	}
	
	public DeferringPredicate(ParseContext ctx) {
		super(ctx, IStory.BaseKind.TRUTH_STATE);
	}
}
