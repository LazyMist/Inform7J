package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IAlias;
import net.inform7j.transpiler.language.IStory.Element;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.List;
import java.util.Optional;

public class DeferringAlias extends DeferringImpl implements IAlias<DeferringImpl> {
    public final List<TokenString> aliases;
    public final TokenString original;
    public DeferringAlias(DeferringStory story, Source source, List<TokenString> aliases, TokenString original) {
        super(story, source);
        this.aliases = List.copyOf(aliases);
        this.original = original;
    }
    @Override
    public List<TokenString> aliases() {
        return aliases;
    }
    @Override
    public DeferringImpl original() {
        DeferringImpl ret = story.getObject(original);
        if(ret == null) ret = story.getAction(original);
        return ret;
    }
    @SuppressWarnings("unchecked")
    @Override
    public <U extends Element> Optional<? extends IAlias<U>> cast(Class<U> clazz) {
        DeferringImpl original = original();
        if(original == null || clazz.isInstance(original)) return Optional.of((IAlias<U>) this);
        return Optional.empty();
    }
}
