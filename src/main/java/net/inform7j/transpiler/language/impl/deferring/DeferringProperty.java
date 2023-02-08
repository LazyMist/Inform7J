package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IKind;
import net.inform7j.transpiler.language.IProperty;
import net.inform7j.transpiler.language.IStory.BaseKind;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.util.LazyLookup;

public class DeferringProperty extends DeferringImpl implements IProperty {
    public final LazyLookup<TokenString, ? extends DeferringKind> owner;
    public final TokenString name;
    public final LazyLookup<TokenString, ? extends DeferringKind> type;
    public DeferringProperty(DeferringStory story, Source src, TokenString owner, TokenString name, TokenString type) {
        super(story, src);
        this.owner = new LazyLookup<>(owner, story::getKind);
        this.name = name;
        this.type = new LazyLookup<>(type, story::getKind);
    }
    
    public DeferringProperty(DeferringStory story, Source src, BaseKind owner, TokenString name, BaseKind type) {
        super(story, src);
        this.owner = new LazyLookup<>(owner.writtenName, story.getBaseKind(owner));
        this.name = name;
        this.type = new LazyLookup<>(type.writtenName, story.getBaseKind(type));
    }
    
    @Override
    public IKind getPropertyOwner() {
        return owner.get();
    }
    
    @Override
    public IKind getPropertyType() {
        return type.get();
    }
    
    @Override
    public TokenString getPropertyName() {
        return name;
    }
    
}
