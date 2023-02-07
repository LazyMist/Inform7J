package net.inform7j.transpiler.language.impl.deferring;

import java.util.List;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IKind;
import net.inform7j.transpiler.language.IProperty;
import net.inform7j.transpiler.language.IStory.BaseKind;
import net.inform7j.transpiler.tokenizer.Replacement;
import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringProperty extends DeferringImpl implements IProperty {
    public static final TokenPattern SOME = AN.orIgnoreCase("some");
    public static final String CAPTURE_OWNER = "owner";
    public static final String CAPTURE_TYPE = "type";
    public static final String CAPTURE_NAME = "name";
    public static final List<Parser<DeferringProperty>> PARSERS = List.of(new Parser<>(
        AN.concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_OWNER))
            .concat(HAS).concat(SOME)
            .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_TYPE))
            .concatIgnoreCase("called")
            .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
            .concat(ENDMARKER)
        /*Pattern.compile("^an? (?<owner>.+?) has (?:an?|some) (?<type>.+?) called (?<name>.+?)\\.", Pattern.CASE_INSENSITIVE)*/,
        DeferringProperty::new
    ), new Parser<>(
        new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_OWNER)
            .concat(HAS).concat(SOME)
            .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_TYPE))
            .concatIgnoreCase("called")
            .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
            .concat(ENDMARKER)
        /*Pattern.compile("^(?<owner>.+?)s have (?:an?|some) (?<type>.+?) called (?<name>.+?)\\.", Pattern.CASE_INSENSITIVE)*/,
        DeferringProperty::new
    ), new Parser<>(
        AN.concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_OWNER))
            .concatIgnoreCase("can be").concat(TokenPattern.quoteIgnoreCase("either").lookahead(true))
            .concat(IDENTIFIER_LOOP.capture(CAPTURE_NAME))
            .concat(ENDMARKER)
        /*Pattern.compile("^an? (?<owner>.+?) can be (?!either)(?<name>.+?)\\.", Pattern.CASE_INSENSITIVE)*/,
        ctx -> new DeferringProperty(ctx, BaseKind.TRUTH_STATE)
    ));
    
    public final TokenString OWNER;
    public final TokenString NAME;
    public final TokenString TYPE;
    public DeferringProperty(DeferringStory story, Source src, TokenString oWNER, TokenString nAME, TokenString tYPE) {
        super(story, src);
        OWNER = oWNER;
        NAME = nAME;
        TYPE = tYPE;
    }
    
    public DeferringProperty(DeferringStory story, Source src, BaseKind oWNER, TokenString nAME, BaseKind tYPE) {
        this(story, src, oWNER.writtenName, nAME, tYPE.writtenName);
    }
    
    public DeferringProperty(ParseContext ctx) {
        this(ctx, ctx.result().cap(CAPTURE_TYPE));
    }
    
    protected DeferringProperty(ParseContext ctx, TokenString type) {
        super(ctx);
        final Result m = ctx.result();
        OWNER = m.cap(CAPTURE_OWNER);
        NAME = m.cap(CAPTURE_NAME);
        TYPE = type;
    }
    
    public DeferringProperty(ParseContext ctx, BaseKind type) {
        this(ctx, type.writtenName);
    }
    
    @Override
    public IKind getPropertyOwner() {
        return story.getKind(OWNER);
    }
    
    @Override
    public IKind getPropertyType() {
        return story.getKind(TYPE);
    }
    
    @Override
    public TokenString getPropertyName() {
        return NAME;
    }
    
}
