package net.inform7j.transpiler.language.impl.deferring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.inform7j.Logging;
import net.inform7j.Logging.Severity;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.Intake.IntakeReader;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStatement.StatementSupplier;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;

public class DeferringPrint extends DeferringRoutine {
	@SuppressWarnings("hiding")
	public static final List<Parser<DeferringPrint>> PARSERS = Collections.unmodifiableList(Arrays.asList(
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to say")
					.concat(PARAM_GLOB.loop())
					.concat(":\n")
					/*Pattern.compile("^to say (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+):\\s*$", Pattern.CASE_INSENSITIVE)*/,
					DeferringPrint::new
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to say")
					.concat(PARAM_GLOB.loop())
					.concat(":(-\n")
					/*Pattern.compile("^to say (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+): \\(-\\s*$", Pattern.CASE_INSENSITIVE)*/,
					DeferringPrint::Raw
					),
			new Parser<>(
					TokenPattern.quoteIgnoreCase("to say")
					.concat(PARAM_GLOB.loop())
					.concat(":(-")
					.concat(new TokenPattern.Single(TokenPredicate.NEWLINE.negate()).loop())
					.concat("-)\n")
					/*Pattern.compile("^to say (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+): \\(-.*?-\\)\\.?\\s*$", Pattern.CASE_INSENSITIVE)*/,
					DeferringPrint::RawClosed
					)
			));

	public DeferringPrint(DeferringStory story, Source source, Stream<? extends SignatureElement> params, IStatement body) {
		super(story, source, params, body);
	}
	
	public DeferringPrint(ParseContext ctx, IStatement body) {
		super(ctx, body);
	}
	
	public static DeferringPrint RawClosed(ParseContext ctx) {
		return new DeferringPrint(ctx, null);
	}
	
	public static DeferringPrint Raw(ParseContext ctx) {
		StatementSupplier sup = ctx.supplier();
		while(true) {
			Optional<? extends IStatement> opt = sup.getNextOptional(IStatement.class);
			if(Logging.log_assert(opt.isPresent(), Severity.FATAL, "Unclosed Raw block starting in: %s@%d", ctx.source().src(), ctx.source().line())) if(IntakeReader.tailMatch(opt.get(), IntakeReader.RAW_END, true)) break;
		}
		return RawClosed(ctx);
	}
	
	public DeferringPrint(ParseContext ctx) {
		super(ctx);
	}

}
