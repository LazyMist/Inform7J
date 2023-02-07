package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record Concat(List<? extends TokenPattern> patterns) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        Stream<Result> results = Stream.of(Result.EMPTY);
        for(TokenPattern p : patterns) {
            final TokenPattern fp = p;
            results = results.flatMap(r -> fp.replaceCaptures(r)
                .matches(src.substring(r.matchLength(), src.length()))
                .map(r::concat));
        }
        return results;
    }
    
    @Override
    public TokenPattern clearCapture() {
        List<TokenPattern> n = patterns.stream().map(TokenPattern::clearCapture).toList();
        Iterator<? extends TokenPattern> a = patterns.iterator();
        Iterator<? extends TokenPattern> b = n.iterator();
        while(a.hasNext()) {
            if(a.next() != b.next()) return new net.inform7j.transpiler.tokenizer.pattern.Concat(n);
        }
        return this;
    }
    
    @Override
    public TokenPattern concat(TokenPattern p) {
        List<TokenPattern> combine = new ArrayList<>(patterns);
        if(p instanceof net.inform7j.transpiler.tokenizer.pattern.Concat c) {
            combine.addAll(c.patterns);
        } else {
            combine.add(p);
        }
        return new net.inform7j.transpiler.tokenizer.pattern.Concat(Collections.unmodifiableList(combine));
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        List<? extends TokenPattern> n = patterns.stream().map(p -> p.replace(replacer)).toList();
        Iterator<? extends TokenPattern> a = patterns.iterator();
        Iterator<? extends TokenPattern> b = n.iterator();
        while(a.hasNext() && b.hasNext()) {
            if(a.next() != b.next()) return new net.inform7j.transpiler.tokenizer.pattern.Concat(n);
        }
        if(a.hasNext() || b.hasNext()) return new net.inform7j.transpiler.tokenizer.pattern.Concat(n);
        return this;
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        List<? extends TokenPattern> n = patterns.stream().map(p -> p.replaceCaptures(previous)).toList();
        Iterator<? extends TokenPattern> a = patterns.iterator();
        Iterator<? extends TokenPattern> b = n.iterator();
        while(a.hasNext() && b.hasNext()) {
            if(a.next() != b.next()) return new net.inform7j.transpiler.tokenizer.pattern.Concat(n);
        }
        if(a.hasNext() || b.hasNext()) return new net.inform7j.transpiler.tokenizer.pattern.Concat(n);
        return this;
    }
}
