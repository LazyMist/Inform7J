package net.inform7j.transpiler.language.impl.deferring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.inform7j.Logging;
import net.inform7j.Logging.Severity;
import net.inform7j.transpiler.Intake.IntakeReader;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStatement.StatementSupplier;
import net.inform7j.transpiler.language.IStory.BaseKind;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;

public class DeferringRoutine extends DeferringFunction {
	@SuppressWarnings("hiding")
	public static final List<Parser<DeferringRoutine>> PARSERS = Collections.unmodifiableList(Arrays.asList(
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to").concat(PARAM_GLOB.loop())
					.concat(":").concat(ENDLINE)
					/*Pattern.compile("^to (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+):\\s*$", Pattern.CASE_INSENSITIVE)*/,
					DeferringRoutine::new
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to").concat(PARAM_GLOB.loop())
					.concat(": (-")
					.concat(ENDLINE)
					/*Pattern.compile("^to (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+): \\(-\\s*$", Pattern.CASE_INSENSITIVE)*/,
					DeferringRoutine::Raw
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to").concat(PARAM_GLOB.loop())
					.concat(": (-")
					.concat(new TokenPattern.Single(TokenPredicate.NEWLINE.negate()).loop())
					.concat("-)")
					.concat(ENDMARKER)
					/*Pattern.compile("^to (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+): \\(-.*?-\\)\\.?\\s*$", Pattern.CASE_INSENSITIVE)*/,
					DeferringRoutine::RawClosed
					)
			));
	
	public DeferringRoutine(DeferringStory story, Source source, Stream<? extends SignatureElement> params, IStatement body) {
		super(story, source, BaseKind.VOID, params, body);
	}
	
	public DeferringRoutine(ParseContext ctx, IStatement body) {
		super(ctx, BaseKind.VOID, body);
	}
	
	public static DeferringRoutine Raw(ParseContext ctx) {
		StatementSupplier sup = ctx.supplier();
		while(true) {
			Optional<? extends IStatement> opt = sup.getNextOptional(IStatement.class);
			if(Logging.log_assert(opt.isPresent(), Severity.FATAL, "Unclosed Raw block starting in: %s@%d", ctx.source().src(), ctx.source().line())) if(IntakeReader.tailMatch(opt.get(), IntakeReader.RAW_END, true)) break;
		}
		return RawClosed(ctx);
	}
	
	public static DeferringRoutine RawClosed(ParseContext ctx) {
		return new DeferringRoutine(ctx, null);
	}
	
	public DeferringRoutine(ParseContext ctx) {
		super(ctx, BaseKind.VOID);
	}
}
