package net.inform7j.transpiler.tokenizer;

import java.util.*;

public record Result(Map<String, List<TokenString>> captures, int matchLength) implements Comparable<Result> {
    public static final Result EMPTY = new Result(0);
    
    public Result(int matchLength) {
        this(Collections.emptyMap(), matchLength);
    }
    
    public Optional<TokenString> capOpt(String name) {
        List<TokenString> choices = capMulti(name);
        if(choices.isEmpty()) return Optional.empty();
        return Optional.of(choices.get(choices.size() - 1));
    }
    
    public TokenString cap(String name) {
        return capOpt(name).orElseThrow();
    }
    
    public List<TokenString> capMulti(String name) {
        return captures.getOrDefault(name, Collections.emptyList());
    }
    
    @Override
    public int compareTo(Result r) {
        return Integer.compareUnsigned(matchLength, r.matchLength);
    }
    
    public boolean isEmpty() {
        return matchLength() == 0;
    }
    
    public boolean notEmpty() {
        return matchLength() > 0;
    }
    
    public Result concat(Result later) {
        Map<String, List<TokenString>> combinedCaptures = new HashMap<>(captures);
        for(Map.Entry<String, ? extends List<TokenString>> e : later.captures.entrySet()) {
            List<TokenString> l = Optional.ofNullable(combinedCaptures.get(e.getKey()))
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
            l.addAll(e.getValue());
            combinedCaptures.put(e.getKey(), l);
        }
        return new Result(combinedCaptures, matchLength + later.matchLength);
    }
    
    public Result mergeCapture(Set<String> name, TokenString cap) {
        Map<String, List<TokenString>> caps = new HashMap<>(captures);
        for(String s : name) {
            List<TokenString> l = Optional.ofNullable(caps.get(s)).map(ArrayList::new).orElseGet(ArrayList::new);
            l.add(cap);
            caps.put(s, l);
        }
        return new Result(caps, matchLength);
    }
}
