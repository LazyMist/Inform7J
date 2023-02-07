package net.inform7j.transpiler.language.impl.deferring;

import java.util.Optional;
import java.util.regex.Pattern;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IKind;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.tokenizer.pattern.Single;
import net.inform7j.transpiler.util.LazyLookup;

public class DeferringKind extends DeferringImpl implements IKind {
    public static final TokenPattern KIND = new Single(new TokenPredicate(Pattern.compile(
        "kind",
        Pattern.CASE_INSENSITIVE
    )));
    public static final String CAPTURE_NAME = "name";
    public static final String CAPTURE_SUPERKIND = "superkind";
    public static final Parser<DeferringKind> PARSER = new Parser<>(
        AN.omittable().concat(WORD_LOOP.capture(CAPTURE_NAME))
            .concatIgnoreCase("is a kind of")
            .concat(new Replacement(
                DeferringStory.KIND_NAME_REPLACEMENT,
                false
            ).capture(CAPTURE_SUPERKIND))
            .concat(ENDMARKER),
        DeferringKind::new
    );
    
    public final TokenString NAME;
    public final Optional<LazyLookup<TokenString, ? extends DeferringKind>> SUPER_KIND;
    
    public DeferringKind(
        DeferringStory story,
        Source src,
        TokenString name,
        Optional<? extends DeferringKind> super_kind
    ) {
        super(story, src);
        NAME = name;
        SUPER_KIND = super_kind.map(k -> new LazyLookup<>(k.NAME, k));
    }
    public DeferringKind(ParseContext ctx) {
        super(ctx);
        Result m = ctx.result();
        NAME = m.cap(CAPTURE_NAME);
        SUPER_KIND = Optional.of(new LazyLookup<>(m.cap(CAPTURE_SUPERKIND), story::getKind));
    }
    
    @Override
    public TokenString name() {
        return NAME;
    }
    
    @Override
    public Optional<? extends DeferringKind> superKind() {
        return SUPER_KIND.map(LazyLookup::get);
    }
    
    public Optional<TokenString> superKindName() {
        return SUPER_KIND.map(LazyLookup::key);
    }
    
    @Override
    public Optional<? extends DeferringProperty> getProperty(TokenString prop) {
        return Optional.ofNullable(story.getProperty(this, prop));
    }
    
    @Override
    public String toString() {
        return String.format(
            "%s is a kind of %s",
            NAME,
            SUPER_KIND.stream().map(LazyLookup::key).map(TokenString::toString).findAny().orElse("")
        );
    }
}
