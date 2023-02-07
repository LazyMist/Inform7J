package net.inform7j.transpiler.language.impl.deferring;

import java.util.Optional;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IAction;
import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringAction extends DeferringImpl implements IAction {
    public static final String CAPTURE_NAME = "name";
    public static final String CAPTURE_PRIMARY = "primary";
    public static final String CAPTURE_SECONDARY = "secondary";
    public static final String CAPTURE_REQUIREMENTS = "requirements";
    public static final Parser<DeferringAction> PARSER = new Parser<>(
        WORD_LOOP.capture(CAPTURE_NAME)
            .concatIgnoreCase("is an Action")
            .concatOptionalIgnoreCase("out of world")
            .concatIgnoreCase("applying to")
            .concat(
                TokenPattern.quoteIgnoreCase("nothing")
                    .or(
                        AN.orIgnoreCase("one").concat(WORD_LOOP.capture(CAPTURE_PRIMARY))
                            .concat(TokenPattern.quoteIgnoreCase("and")
                                .concat(AN.omittable())
                                .concat(WORD_LOOP.capture(CAPTURE_SECONDARY))
                                .omittable())
                    )
                    .or(
                        TokenPattern.quoteIgnoreCase("two")
                            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_PRIMARY, CAPTURE_SECONDARY))))
            .concat(TokenPattern.quoteIgnoreCase("requiring")
                .concat(WORD_LOOP.capture(CAPTURE_REQUIREMENTS))
                .omittable())
            .concat(ENDMARKER)
        /*Pattern.compile("^(?<name>.+?) is an Action applying to (?:nothing|(?<amnt>an?|one|two) (?<primary>.+?)(?: and(?: an?)? (?<secondary>.+?)))?(?: and requiring (?<requirements>.+?))?\\.", Pattern.CASE_INSENSITIVE)*/,
        DeferringAction::new
    );
    
    public final TokenString NAME;
    public final Optional<TokenString> PRIMARY;
    public final Optional<TokenString> SECONDARY;
    public final Optional<TokenString> REQUIREMENTS;
    
    public DeferringAction(
        DeferringStory story,
        Source source,
        TokenString nAME,
        Optional<TokenString> pRIMARY,
        Optional<TokenString> sECONDARY,
        Optional<TokenString> rEQUIREMENTS
    ) {
        super(story, source);
        NAME = nAME;
        PRIMARY = pRIMARY;
        SECONDARY = sECONDARY;
        REQUIREMENTS = rEQUIREMENTS;
    }
    
    public DeferringAction(ParseContext ctx) {
        super(ctx);
        Result m = ctx.result();
        NAME = m.cap(CAPTURE_NAME);
        PRIMARY = m.capOpt(CAPTURE_PRIMARY);
        SECONDARY = m.capOpt(CAPTURE_SECONDARY);
        REQUIREMENTS = m.capOpt(CAPTURE_REQUIREMENTS);
    }
    
    @Override
    public TokenString name() {
        return NAME;
    }
    
    @Override
    public Optional<? extends DeferringKind> primary() {
        return PRIMARY.map(story::getKind);
    }
    
    @Override
    public Optional<? extends DeferringKind> secondary() {
        return SECONDARY.map(story::getKind);
    }
    
    @Override
    public Optional<TokenString> requirements() {
        return REQUIREMENTS;
    }
}
