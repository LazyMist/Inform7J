package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IFunction;
import net.inform7j.transpiler.language.IKind;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStory.BaseKind;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DeferringFunction extends DeferringImpl implements IFunction {
    public record DeferredParameter(DeferringStory story, Source source, TokenString name, TokenString kindName)
        implements ParameterElement {
        @Override
        public String toSignatureString() {
            return "$" + kindName + "$";
        }
        @Override
        public DeferringKind kind() {
            return story.getKind(kindName);
        }
    }
    public final TokenString returnType;
    public final List<? extends SignatureElement> name;
    public final IStatement body;
    public DeferringFunction(
        DeferringStory story, Source source, TokenString returnType, Stream<? extends SignatureElement> name,
        IStatement body
    ) {
        super(story, source);
        this.returnType = returnType;
        this.name = name.toList();
        this.body = body;
    }
    public DeferringFunction(
        DeferringStory story, Source source, BaseKind returnType, Stream<? extends SignatureElement> name,
        IStatement bODY
    ) {
        this(story, source, returnType.writtenName, name, bODY);
    }
    
    @Override
    public Stream<? extends DeferringKind> streamParameters() {
        return streamName().map(s -> (s instanceof DeferredParameter p) ? p.kind() : null).filter(Objects::nonNull);
    }
    
    @Override
    public Stream<? extends SignatureElement> streamName() {
        return name.stream();
    }
    
    @Override
    public IKind returnType() {
        return story.getKind(returnType);
    }
    @Override
    public IStatement body() {
        return body;
    }
}
