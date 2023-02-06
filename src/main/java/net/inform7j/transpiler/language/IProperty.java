package net.inform7j.transpiler.language;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IProperty {
    IKind getPropertyOwner();
    IKind getPropertyType();
    TokenString getPropertyName();
}
