package net.inform7j.transpiler.parser;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.util.StatementSupplier;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Slf4j
public record ComplexCombinedParser(
    int order,
    TokenPattern pattern,
    BiFunction<? super DeferringStory, ? super TokenPattern, ? extends TokenPattern> patMap,
    Consumer<? super DeferringImpl.ParseContext> interpreter
) implements CombinedParser {
    @Override
    public TokenString cparse(DeferringStory story, IStatement source, StatementSupplier sup, TokenString src) {
        log.trace("Parsing {}\nwith {}", src, pattern);
        Optional<Result> results = patMap.apply(story, pattern).matches(src).findFirst();
        if(results.isEmpty()) return src;
        log.trace("Parsing successful");
        interpreter.accept(new DeferringImpl.ParseContext(story, source, results.get(), sup));
        return src.substring(results.get().matchLength());
    }
}
