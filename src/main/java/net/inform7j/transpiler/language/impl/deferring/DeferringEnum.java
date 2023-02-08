package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IEnum;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class DeferringEnum extends DeferringImpl implements IEnum {
    public final Category category;
    public final TokenString name;
    private final List<TokenString> values;
    
    public DeferringEnum(
        DeferringStory story,
        Source source,
        Category category,
        TokenString name,
        List<TokenString> values
    ) {
        super(story, source);
        this.category = category;
        this.name = name;
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }
    
    @Override
    public Category category() {
        return category;
    }
    
    @Override
    public TokenString name() {
        return name;
    }
    
    @Override
    public Stream<TokenString> streamValues() {
        return values.stream();
    }
    
}
