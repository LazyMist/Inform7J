package net.inform7j.transpiler.language.impl.deferring;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IValue;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.tokenizer.pattern.Single;
import net.inform7j.transpiler.util.LazyLookup;

public class DeferringValue extends DeferringImpl implements IValue {
    public static final TokenPattern FILE = new Single(new TokenPredicate(Pattern.compile(
        "file",
        Pattern.CASE_INSENSITIVE
    )));
    public static final String CAPTURE_NAME = "name";
    public static final String CAPTURE_OBJECT = "object";
    public static final String CAPTURE_VALUE = "value";
    private static final Replacement OBJ = new Replacement(
        DeferringStory.OBJECT_NAME_REPLACEMENT,
        false
    );
    private static final Replacement PROP = new Replacement(
        DeferringStory.OBJECT_PROPERTY_NAME_REPLACEMENT,
        false
    );
    private static final Replacement GPROP = new Replacement(
        DeferringStory.PROPERTY_NAME_REPLACEMENT,
        false
    );
    private static final TokenPattern RULE_START = TokenPattern.quoteIgnoreCase("to decide").orIgnoreCase("Rule for");
    public static final List<Parser<DeferringValue>> PARSERS = List.of(new Parser<>(
        Single.STRING.capture(CAPTURE_VALUE)
            .concat(ENDMARKER)
        /*Pattern.compile("^(?<value>\"[^\"]++\")(?>\\s*+(?>\\.|;))?")*/,
        DeferringValue::descriptionValue
    ), new Parser<>(
        RULE_START.lookahead(true)
            .concat(TokenPattern.quoteIgnoreCase("the").omittable())
            .concat(OBJ.capture(CAPTURE_OBJECT, DeferringStory.PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE))
            .concat(TokenPattern.quoteIgnoreCase("is not").orIgnoreCase("isn't")).concat(PROP.capture(CAPTURE_NAME))
            .concat(ENDMARKER),
        DeferringValue::falsePropertyValue
    ), new Parser<>(
        RULE_START.lookahead(true)
            .concat(TokenPattern.quoteIgnoreCase("the").omittable())
            .concat(OBJ.capture(CAPTURE_OBJECT, DeferringStory.PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE))
            .concatIgnoreCase("is").concat(PROP.capture(CAPTURE_NAME))
            .concat(ENDMARKER),
        DeferringValue::truePropertyValue
    ), new Parser<>(
        RULE_START.lookahead(true)
            .concat(TokenPattern.quoteIgnoreCase("the").omittable())
            .concat(GPROP.capture(CAPTURE_NAME).concatIgnoreCase("of").omittable())
            .concat(OBJ.capture(CAPTURE_OBJECT))
            .concatIgnoreCase("is").concat(TokenPattern.quoteIgnoreCase("a kind")
                .orIgnoreCase("an action")
                .lookahead(true))
            .concat(TokenPattern.quoteIgnoreCase("called")
                .or(TokenPattern.quoteIgnoreCase("the").concatIgnoreCase("file"))
                .omittable())
            .concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VALUE))
            .concat(ENDMARKER)
        /*Pattern.compile("^(?>the )?(?>(?<name>[^\"\\.]+?) of )?(?<object>[^\"\\.]+?)\\s+(?>is) (?>called |the file )?(?<value>(?>\\{\\s*+)?(?:(?:\"[^\"]*+\"|[^\"{},\\.;]+)(?:\\s*+,\\s*+)?)+(?>\\s*+\\})?)\\s*+(?:\\.|;|$)", Pattern.CASE_INSENSITIVE)*/,
        DeferringValue::new
    ), new Parser<>(
        RULE_START.lookahead(true)
            .concat(TokenPattern.quoteIgnoreCase("the").omittable())
            .concat(OBJ.capture(CAPTURE_OBJECT, DeferringStory.PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE))
            .concatIgnoreCase("has").concat(AN)
            .concat(PROP.capture(CAPTURE_NAME))
            .concat(Single.STRING.capture(CAPTURE_VALUE))
            .concat(ENDMARKER),
        DeferringValue::new
    ));
    
    public final LazyLookup<TokenString, DeferringObject> owner;
    public final TokenString value;
    public final Optional<LazyLookup<TokenString, DeferringProperty>> name;
    
    public DeferringValue(
        DeferringStory story,
        Source source,
        DeferringObject owner,
        Optional<DeferringProperty> name,
        TokenString value
    ) {
        super(story, source);
        this.name = name.map(p -> new LazyLookup<>(p.name, p));
        this.owner = new LazyLookup<>(owner.name, owner);
        this.value = value;
    }
    
    private DeferringValue(ParseContext ctx, TokenString owner, Optional<TokenString> name) {
        super(ctx);
        final Result m = ctx.result();
        this.owner = new LazyLookup<>(owner, story::getObject);
        this.name = name.map(n -> new LazyLookup<>(n, p -> story.getProperty(this.owner.get().getType(), p)));
        this.value = m.cap(CAPTURE_VALUE);
    }
    
    private DeferringValue(ParseContext ctx, TokenString value) {
        super(ctx);
        final Result m = ctx.result();
        this.owner = new LazyLookup<>(m.cap(CAPTURE_OBJECT), story::getObject);
        this.name = m.capOpt(CAPTURE_NAME).map(n -> new LazyLookup<>(n, p -> story.getProperty(owner.get().getType(), p)));
        this.value = value;
    }
    
    public static DeferringValue descriptionValue(ParseContext ctx) {
        return new DeferringValue(
            ctx,
            new TokenString(new Token(Token.Type.WORD, "it")),
            Optional.of(new TokenString(new Token(Token.Type.WORD, "description")))
        );
    }
    
    public static DeferringValue truePropertyValue(ParseContext ctx) {
        return new DeferringValue(ctx, new TokenString(new Token(Token.Type.WORD, "true")));
    }
    
    public static DeferringValue falsePropertyValue(ParseContext ctx) {
        return new DeferringValue(ctx, new TokenString(new Token(Token.Type.WORD, "false")));
    }
    
    public DeferringValue(ParseContext ctx) {
        super(ctx);
        final Result m = ctx.result();
        this.owner = new LazyLookup<>(m.cap(CAPTURE_OBJECT), story::getObject);
        this.name = m.capOpt(CAPTURE_NAME).map(n -> new LazyLookup<>(n, p -> story.getProperty(owner.get().getType(), p)));
        this.value = m.cap(CAPTURE_VALUE);
    }
    
    @Override
    public DeferringObject holder() {
        return owner.get();
    }
    
    @Override
    public Optional<? extends DeferringProperty> property() {
        return name.map(LazyLookup::get);
    }
    
    @Override
    public TokenString value() {
        return value;
    }
    
    @Override
    public String toString() {
        return String.format("%s is %s", name.map(LazyLookup::key).map(s -> s+" of ").orElse("")+ owner.key(), value);
    }
}
