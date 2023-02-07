package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.util.StatementSupplier;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface CombinedParser {
    interface Provider extends Supplier<Stream<? extends CombinedParser>> {}
    TokenString cparse(DeferringStory story, IStatement source, StatementSupplier sup, TokenString src);
    default int order() {
        return 0;
    }
}
