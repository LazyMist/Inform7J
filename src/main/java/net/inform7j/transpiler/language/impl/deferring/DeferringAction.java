package net.inform7j.transpiler.language.impl.deferring;

import java.util.Optional;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IAction;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringAction extends DeferringImpl implements IAction {
    public final TokenString name;
    public final Optional<TokenString> primary;
    public final Optional<TokenString> secondary;
    public final Optional<TokenString> requirements;
    
    public DeferringAction(
        DeferringStory story,
        Source source,
        TokenString nAME,
        Optional<TokenString> primary,
        Optional<TokenString> secondary,
        Optional<TokenString> requirements
    ) {
        super(story, source);
        this.name = nAME;
        this.primary = primary;
        this.secondary = secondary;
        this.requirements = requirements;
    }
    
    @Override
    public TokenString name() {
        return name;
    }
    
    @Override
    public Optional<? extends DeferringKind> primary() {
        return primary.map(story::getKind);
    }
    
    @Override
    public Optional<? extends DeferringKind> secondary() {
        return secondary.map(story::getKind);
    }
    
    @Override
    public Optional<TokenString> requirements() {
        return requirements;
    }
}
