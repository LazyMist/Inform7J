package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IDefault;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class DeferringDefault extends DeferringImpl implements IDefault {
    public static final TokenPattern USUALLY = new Single(new TokenPredicate(Pattern.compile(
        "usually|normally",
        Pattern.CASE_INSENSITIVE
    )));
    public static final String CAPTURE_PROPERTY = "property";
    public static final String CAPTURE_OBJECT = "object";
    public static final String CAPTURE_VALUE = "value";
    public static final List<Parser<DeferringDefault>> PARSERS = List.of(new Parser<>(
        AN.concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(
                CAPTURE_OBJECT,
                DeferringStory.PROPERTY_NAME_REPLACEMENT_KIND_CAPTURE
            ))
            .concatIgnoreCase("is").concat(USUALLY)
            .concat(TokenPattern.quoteIgnoreCase("not").capture(CAPTURE_VALUE).omittable())
            .concat(new Replacement(DeferringStory.KIND_PROPERTY_NAME_REPLACEMENT, false).capture(
                CAPTURE_PROPERTY))
            .concat(ENDMARKER)
        /*Pattern.compile("^(?:(?<property>.+?) of )?(?<object>.+?) is (?:usually|normally) (?<value>.+?)\\s*(?:\\.|;|$)", Pattern.CASE_INSENSITIVE)*/,
        DeferringDefault::shortBool
    ), new Parser<>(
        IDENTIFIER_LOOP.capture(CAPTURE_PROPERTY).concatIgnoreCase("of").omittable()
            .concat(new Replacement(DeferringStory.OBJECT_NAME_REPLACEMENT, false).capture(CAPTURE_OBJECT))
            .concatIgnoreCase("is").concat(USUALLY)
            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VALUE))
            .concat(ENDMARKER)
        /*Pattern.compile("^(?:(?<property>.+?) of )?(?<object>.+?) is (?:usually|normally) (?<value>.+?)\\s*(?:\\.|;|$)", Pattern.CASE_INSENSITIVE)*/,
        DeferringDefault::new
    ), new Parser<>(
        IDENTIFIER_LOOP.capture(CAPTURE_PROPERTY).concatIgnoreCase("of").omittable()
            .concat(AN.omittable())
            .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_OBJECT))
            .concatIgnoreCase("is").concat(USUALLY)
            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VALUE))
            .concat(ENDMARKER)
        /*Pattern.compile("^(?:(?<property>.+?) of )?(?<object>.+?) is (?:usually|normally) (?<value>.+?)\\s*(?:\\.|;|$)", Pattern.CASE_INSENSITIVE)*/,
        DeferringDefault::new
    ));
    
    public final TokenString LABEL;
    public final TokenString VALUE;
    public final Optional<TokenString> PROPERTY;
    
    public DeferringDefault(
        DeferringStory story,
        Source source,
        Optional<TokenString> property,
        TokenString lABEL,
        TokenString vALUE
    ) {
        super(story, source);
        PROPERTY = property;
        LABEL = lABEL;
        VALUE = vALUE;
    }
    
    public DeferringDefault(ParseContext ctx) {
        super(ctx);
        Result m = ctx.result();
        PROPERTY = m.capOpt(CAPTURE_PROPERTY);
        LABEL = m.cap(CAPTURE_OBJECT);
        VALUE = m.cap(CAPTURE_VALUE);
    }
    
    public static DeferringDefault shortBool(ParseContext ctx) {
        Result m = ctx.result();
        return new DeferringDefault(
            ctx.story(),
            ctx.source().source(),
            m.capOpt(CAPTURE_PROPERTY),
            m.cap(CAPTURE_OBJECT),
            new TokenString(m.capOpt(CAPTURE_VALUE).isEmpty() ? "true" : "false")
        );
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
