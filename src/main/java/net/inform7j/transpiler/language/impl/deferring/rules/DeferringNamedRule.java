package net.inform7j.transpiler.language.impl.deferring.rules;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.parser.provider.FunctionParserProvider;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.Optional;

public final class DeferringNamedRule extends DeferringRule {
    public static final Parser<DeferringNamedRule> PARSER = new Parser<>(
        TokenPattern.quoteIgnoreCase("This is")
            .concatOptionalIgnoreCase("the")
            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_RULE_NAME))
            .concatIgnoreCase("rule:")
            .concat(ENDLINE)
        /*Pattern.compile("^This is (?:the )?(?<name>.+?) rule:\\s*$", Pattern.CASE_INSENSITIVE)*/,
        DeferringNamedRule::new);
    
    public DeferringNamedRule(DeferringStory story, Source source, IStatement bODY, TokenString nAME) {
        super(story, source, Optional.of(nAME), bODY);
    }
    
    public DeferringNamedRule(ParseContext ctx) {
        super(ctx, FunctionParserProvider.getNextBody(ctx.supplier()));
    }
}
