package net.inform7j.transpiler.tokenizer.pattern;

import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public record Conjunction(Collection<? extends TokenPattern> patterns) implements TokenPattern {
    @Override
    public Stream<Result> matches(TokenString src) {
        return patterns.stream().flatMap(p -> p.matches(src));
    }
    
    @Override
    public TokenPattern clearCapture() {
        List<TokenPattern> n = patterns.stream().map(TokenPattern::clearCapture).toList();
        Iterator<? extends TokenPattern> a = patterns.iterator();
        Iterator<? extends TokenPattern> b = n.iterator();
        while(a.hasNext()) {
            if(a.next() != b.next()) return new net.inform7j.transpiler.tokenizer.pattern.Conjunction(n);
        }
        return this;
    }
    
    @Override
    public TokenPattern or(TokenPattern p) {
        Collection<TokenPattern> pat = new HashSet<>(patterns);
        if(p instanceof net.inform7j.transpiler.tokenizer.pattern.Conjunction c) {
            pat.addAll(c.patterns);
        } else {
            pat.add(p);
        }
        return new net.inform7j.transpiler.tokenizer.pattern.Conjunction(Collections.unmodifiableCollection(pat));
    }
    
    @Override
    public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
        List<? extends TokenPattern> n = patterns.stream().map(p -> p.replace(replacer)).toList();
        Iterator<? extends TokenPattern> a = patterns.iterator();
        Iterator<? extends TokenPattern> b = n.iterator();
        while(a.hasNext() && b.hasNext()) {
            if(a.next() != b.next()) return new net.inform7j.transpiler.tokenizer.pattern.Conjunction(n);
        }
        if(a.hasNext() || b.hasNext()) return new net.inform7j.transpiler.tokenizer.pattern.Conjunction(n);
        return this;
    }
    
    @Override
    public TokenPattern replaceCaptures(Result previous) {
        List<? extends TokenPattern> n = patterns.stream().map(p -> p.replaceCaptures(previous)).toList();
        Iterator<? extends TokenPattern> a = patterns.iterator();
        Iterator<? extends TokenPattern> b = n.iterator();
        while(a.hasNext() && b.hasNext()) {
            if(a.next() != b.next()) return new net.inform7j.transpiler.tokenizer.pattern.Conjunction(n);
        }
        if(a.hasNext() || b.hasNext()) return new net.inform7j.transpiler.tokenizer.pattern.Conjunction(n);
        return this;
    }
}
