package net.inform7j.transpiler.language.impl.deferring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.IntakeReader;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.util.StatementSupplier;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPredicate;

@Slf4j
public class DeferringPrint extends DeferringRoutine {
    private static final TokenPattern TO_SAY = TokenPattern.quoteIgnoreCase("to say");
    @SuppressWarnings("hiding")
    public static final List<Parser<DeferringPrint>> PARSERS = Collections.unmodifiableList(Arrays.asList(
        new Parser<>(
            TO_SAY
                .concat(PARAM_GLOB.loop())
                .concat(":\n")
            /*Pattern.compile("^to say (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+):\\s*$", Pattern.CASE_INSENSITIVE)*/,
            DeferringPrint::new
        ),
        new Parser<>(
            TO_SAY
                .concat(PARAM_GLOB.loop())
                .concat(":(-\n")
            /*Pattern.compile("^to say (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+): \\(-\\s*$", Pattern.CASE_INSENSITIVE)*/,
            DeferringPrint::Raw
        ),
        new Parser<>(
            TO_SAY
                .concat(PARAM_GLOB.loop())
                .concat(":(-")
                .concat(new TokenPattern.Single(TokenPredicate.NEWLINE.negate()).loop())
                .concat("-)\n")
            /*Pattern.compile("^to say (?<nameParams>"+DeferringFunction.PARAM_GLOB+"+): \\(-.*?-\\)\\.?\\s*$", Pattern.CASE_INSENSITIVE)*/,
            DeferringPrint::RawClosed
        )
    ));
    
    public DeferringPrint(
        DeferringStory story,
        Source source,
        Stream<? extends SignatureElement> params,
        IStatement body
    ) {
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
            if(opt.isPresent()) {
                if(IntakeReader.tailMatch(opt.get(), IntakeReader.RAW_END, true)) break;
            } else {
                log.error("Unclosed Raw block starting in: {}@{}", ctx.source().src(), ctx.source().line());
            }
        }
        return RawClosed(ctx);
    }
    
    public DeferringPrint(ParseContext ctx) {
        super(ctx);
    }
    
}
