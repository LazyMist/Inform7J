package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IObject;
import net.inform7j.transpiler.language.IStory.BaseKind;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.util.LazyLookup;

public class DeferringObject extends DeferringImpl implements IObject {
    public final TokenString name;
    public final LazyLookup<TokenString, ? extends DeferringKind> type;
    
    public DeferringObject(DeferringStory story, Source source, TokenString name, DeferringKind type) {
        super(story, source);
        this.name = name;
        this.type = new LazyLookup<>(type.name, type);
    }
    public DeferringObject(DeferringStory story, Source source, TokenString name, TokenString type) {
        super(story, source);
        this.name = name;
        this.type = new LazyLookup<>(type, story::getKind);
    }
    public DeferringObject(DeferringStory story, Source source, TokenString name, BaseKind baseKind) {
        this(story, source, name, story.getBaseKind(baseKind));
    }
    
    @Override
    public DeferringKind getType() {
        return type.get();
    }
    
    @Override
    public TokenString getTypeName() {
        return type.key();
    }
    
    @Override
    public TokenString getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return String.format("%s is a %s", name, type.key());
    }
}
