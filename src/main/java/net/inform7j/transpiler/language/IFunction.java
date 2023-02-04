package net.inform7j.transpiler.language;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IFunction extends IStory.Element {
	public static sealed interface SignatureElement permits NameElement, ParameterElement {
		public String toSignatureString();
	}
	public static record NameElement(Set<String> values) implements SignatureElement {
		public NameElement(String ...values) {
			this(Set.of(values));
		}
		
		@Override
		public String toSignatureString() {
			return values.stream().findFirst().orElse("");
		}
	}
	public static non-sealed interface ParameterElement extends SignatureElement, IStory.Element {
		@Override
		default String toSignatureString() {
			return "$"+kind().name()+"$";
		}
		public TokenString name();
		public IKind kind();
	}
	public static String computeSignature(Stream<? extends SignatureElement> elements) {
		return elements.map(SignatureElement::toSignatureString).collect(Collectors.joining(" "));
	}
	
	public default Stream<? extends IKind> streamParameters() {
		return streamName().map(s -> (s instanceof ParameterElement p) ? p.kind() : null).filter(p -> p != null);
	}
	public Stream<? extends SignatureElement> streamName();
	public default String getSignature() {
		return computeSignature(streamName());
	}
	public IKind returnType();
	public IStatement body();
}
