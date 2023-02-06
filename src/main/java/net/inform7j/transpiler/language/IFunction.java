package net.inform7j.transpiler.language;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IFunction extends IStory.Element {
    sealed interface SignatureElement permits NameElement, ParameterElement {
        String toSignatureString();
    }
    
    record NameElement(Set<String> values) implements SignatureElement {
        public NameElement(String... values) {
            this(Set.of(values));
        }
        
        @Override
        public String toSignatureString() {
            return values.stream().findFirst().orElse("");
        }
    }
    
    non-sealed interface ParameterElement extends SignatureElement, IStory.Element {
        @Override
        default String toSignatureString() {
            return "$" + kind().name() + "$";
        }
        TokenString name();
        IKind kind();
    }
    static String computeSignature(Stream<? extends SignatureElement> elements) {
        return elements.map(SignatureElement::toSignatureString).collect(Collectors.joining(" "));
    }
    
    default Stream<? extends IKind> streamParameters() {
        return streamName().map(s -> (s instanceof ParameterElement p) ? p.kind() : null).filter(p -> p != null);
    }
    Stream<? extends SignatureElement> streamName();
    default String getSignature() {
        return computeSignature(streamName());
    }
    IKind returnType();
    IStatement body();
}
