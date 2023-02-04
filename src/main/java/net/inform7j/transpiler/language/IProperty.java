package net.inform7j.transpiler.language;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IProperty {
	public IKind getPropertyOwner();
	public IKind getPropertyType();
	public TokenString getPropertyName();
}
