package net.inform7j.transpiler.language;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IObject {
	public IKind getType();
	public default TokenString getTypeName() {
		return getType().name();
	}
	public TokenString getName();
}
