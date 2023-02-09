package net.inform7j.transpiler.parser;

import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.impl.deferring.DeferringImpl;
import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.util.StatementSupplier;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public record SimpleCombinedParser<T extends DeferringImpl>(
    int order,
    TokenPattern pattern,
    BiFunction<? super DeferringStory, ? super TokenPattern, ? extends TokenPattern> patMap,
    Function<? super DeferringImpl.ParseContext, T> factory,
    BiConsumer<? super DeferringStory, ? super T> consumer
) implements CombinedParser {
    public SimpleCombinedParser(
        int order,
        DeferringImpl.Parser<T> parser,
        BiFunction<? super DeferringStory, ? super TokenPattern, ? extends TokenPattern> patMap,
        BiConsumer<? super DeferringStory, ? super T> consumer
    ) {
        this(order, parser.pattern(), patMap, parser.factory(), consumer);
    }
    public TokenString cparse(DeferringStory story, IStatement source, StatementSupplier sup, TokenString src) {
        //Logging.log(Severity.DEBUG, "Parsing %s\nwith %s", src, parser.pattern().pattern());
        Optional<Result> results = patMap.apply(story, pattern).matches(src).findFirst();
        if(results.isEmpty()) return src;
        //Logging.log(Severity.DEBUG, "Parsing successful");
        consumer.accept(
            story,
            factory.apply(new DeferringImpl.ParseContext(story, source, results.get(), sup))
        );
        return src.substring(results.get().matchLength());
    }
    
    public static <T extends DeferringImpl> Function<DeferringImpl.Parser<? extends T>, SimpleCombinedParser<? extends T>> bind(
        int order,
        BiFunction<? super DeferringStory, ? super TokenPattern, ? extends TokenPattern> map,
        BiConsumer<? super DeferringStory, T> con
    ) {
        return p -> new SimpleCombinedParser<>(order, p, map, con);
    }
}
