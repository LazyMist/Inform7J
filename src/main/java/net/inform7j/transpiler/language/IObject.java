package net.inform7j.transpiler.language;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IObject {
    IKind getType();
    default TokenString getTypeName() {
        return getType().name();
    }
    TokenString getName();
}
