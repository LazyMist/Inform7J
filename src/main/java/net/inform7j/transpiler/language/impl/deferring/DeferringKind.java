package net.inform7j.transpiler.language.impl.deferring;

import java.util.Optional;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IKind;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.util.LazyLookup;

public class DeferringKind extends DeferringImpl implements IKind {
    
    public final TokenString name;
    public final Optional<LazyLookup<TokenString, ? extends DeferringKind>> superKind;
    
    public DeferringKind(
        DeferringStory story,
        Source src,
        TokenString name,
        DeferringKind superKind
    ) {
        super(story, src);
        this.name = name;
        this.superKind = Optional.of(superKind).map(k -> new LazyLookup<>(k.name, k));
    }
    public DeferringKind(
        DeferringStory story,
        Source src,
        TokenString name,
        TokenString superKind
    ) {
        super(story, src);
        this.name = name;
        this.superKind = Optional.of(superKind).map(k -> new LazyLookup<>(k, story::getKind));
    }
    public DeferringKind(
        DeferringStory story,
        Source src,
        TokenString name
    ) {
        super(story, src);
        this.name = name;
        this.superKind = Optional.empty();
    }
    
    @Override
    public TokenString name() {
        return name;
    }
    
    @Override
    public Optional<? extends DeferringKind> superKind() {
        return superKind.map(LazyLookup::get);
    }
    
    public Optional<TokenString> superKindName() {
        return superKind.map(LazyLookup::key);
    }
    
    @Override
    public Optional<? extends DeferringProperty> getProperty(TokenString prop) {
        return Optional.ofNullable(story.getProperty(this, prop));
    }
    
    @Override
    public String toString() {
        return String.format(
            "%s is a kind of %s",
            name,
            superKind.stream().map(LazyLookup::key).map(TokenString::toString).findAny().orElse("")
        );
    }
}
