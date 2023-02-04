package net.inform7j.transpiler.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.inform7j.transpiler.tokenizer.Token.SourcedToken;

public interface TokenPattern {
	record Result(Map<String,List<TokenString>> captures, int matchLength) implements Comparable<Result> {
		public static final Result EMPTY = new Result(0);

		public Result(int matchLength) {
			this(Collections.emptyMap(), matchLength);
		}

		public Optional<TokenString> capOpt(String name) {
			List<TokenString> choices = capMulti(name);
			if(choices.isEmpty()) return Optional.empty();
			return Optional.of(choices.get(choices.size()-1));
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
			for(Entry<String, ? extends List<TokenString>> e: later.captures.entrySet()) {
				List<TokenString> l = Optional.ofNullable(combinedCaptures.get(e.getKey())).map(ArrayList::new).orElseGet(ArrayList::new);
				l.addAll(e.getValue());
				combinedCaptures.put(e.getKey(), l);
			}
			return new Result(combinedCaptures, matchLength + later.matchLength);
		}

		public Result mergeCapture(Set<String> name, TokenString cap) {
			Map<String,List<TokenString>> caps = new HashMap<>(captures);
			for(String s: name) {
				List<TokenString> l = Optional.ofNullable(caps.get(s)).map(ArrayList::new).orElseGet(ArrayList::new);
				l.add(cap);
				caps.put(s, l);
			}
			return new Result(caps, matchLength);
		}
	}

	Stream<Result> matches(TokenString string);

	default Stream<Result> matchesWrapped(List<SourcedToken> src) {
		return matches(new TokenString(src.stream().map(SourcedToken::tok)));
	}

	default TokenPattern loop(Comparator<Result> order) {
		return new Loop(this, order);
	}

	default TokenPattern loop(boolean min) {
		return loop(min ? Comparator.naturalOrder() : Comparator.reverseOrder());
	}

	default TokenPattern loop() {
		return loop(false);
	}

	default TokenPattern omittable(Comparator<Result> order) {
		return new Omittable(this, order);
	}

	default TokenPattern omittable(boolean min) {
		return omittable(min ? Comparator.naturalOrder() : Comparator.reverseOrder());
	}

	default TokenPattern omittable() {
		return omittable(false);
	}

	default TokenPattern concat(TokenPattern p) {
		return new Concat(List.of(this, p));
	}

	default TokenPattern concatReplacement(String key) {
		return concat(new Replacement(key, false));
	}

	default TokenPattern concatNoCapReplacement(String key) {
		return concat(new Replacement(key, true));
	}

	default TokenPattern concat(String s) {
		return concat(quote(s));
	}
	default TokenPattern concatIgnoreCase(String s) {
		return concat(quoteIgnoreCase(s));
	}

	default TokenPattern concatOptional(String s) {
		return concat(quote(s).omittable());
	}
	default TokenPattern concatOptionalIgnoreCase(String s) {
		return concat(quoteIgnoreCase(s).omittable());
	}

	default TokenPattern concatConjunction(Collection<? extends TokenPattern> patterns) {
		return concat(new Conjunction(patterns));
	}

	default TokenPattern or(TokenPattern p) {
		return new Conjunction(List.of(this, p));
	}

	default TokenPattern or(String s) {
		return or(quote(s));
	}

	default TokenPattern orIgnoreCase(String s) {
		return or(quoteIgnoreCase(s));
	}

	default TokenPattern capture(Collection<String> name) {
		return new Capture(this, Set.copyOf(name));
	}

	default TokenPattern capture(String ...name) {
		return capture(Arrays.asList(name));
	}

	default TokenPattern lookahead(boolean invert) {
		return new Lookahead(this, invert);
	}

	static TokenPattern quote(Stream<Token> tokens, BiPredicate<Token, Token> eq) {
		return new Concat(tokens.map(t -> (Predicate<Token>)(x -> eq.test(t, x))).map(Single::new).toList());
	}

	static TokenPattern quote(Stream<Token> tokens) {
		return quote(tokens, Token::equals);
	}

	static TokenPattern quoteIgnoreCase(Stream<Token> tokens) {
		return quote(tokens, Token::equalsIgnoreCase);
	}

	static TokenPattern quote(TokenString tokens, BiPredicate<Token, Token> eq) {
		return new Concat(tokens.stream().map(t -> new Single(x -> eq.test(t, x))).toList());
	}

	static TokenPattern quote(TokenString tokens) {
		return quote(tokens, Token::equals);
	}

	static TokenPattern quoteIgnoreCase(TokenString tokens) {
		return quote(tokens, Token::equalsIgnoreCase);
	}

	static TokenPattern quote(String tokens) {
		return quote(Token.Generator.parseLiteral(tokens));
	}

	static TokenPattern quoteIgnoreCase(String tokens) {
		return quoteIgnoreCase(Token.Generator.parseLiteral(tokens));
	}

	TokenPattern clearCapture();

	TokenPattern replace(Function<? super String,? extends TokenPattern> replacer);
	
	TokenPattern replaceCaptures(Result previous);

	default Stream<TokenString> splitAsStream(TokenString src) {
		Stream.Builder<TokenString> out = Stream.builder();
		L:while(!src.isEmpty()) {
			for(int i=0; i<src.length(); i++) {
				TokenString sub = src.substring(i);
				Optional<Result> res = matches(sub).findFirst();
				if(res.isPresent()) {
					if(i>0) out.accept(src.substring(0, i));
					src = sub.substring(res.get().matchLength, sub.length());
					continue L;
				}
			}
			out.accept(src);
			break;
		}
		return out.build();
	}

	record Loop(TokenPattern pattern, Comparator<Result> order) implements TokenPattern {
		@Override
		public Stream<Result> matches(TokenString src) {
			List<Result> ret = new ArrayList<>();
			List<Result> fmatch = pattern.matches(src).toList();
			List<Result> empty = fmatch.stream().filter(Result::isEmpty).toList();
			fmatch.stream().filter(Result::notEmpty).forEach(ret::add);
			for(int i=0; i<ret.size(); i++) {
				final Result res = ret.get(i);
				pattern.matches(src.substring(res.matchLength, src.length())).filter(Result::notEmpty).map(res::concat).forEachOrdered(ret::add);
			}
			ret.addAll(0, empty);
			return ret.stream().sorted(order);
		}

		@Override
		public TokenPattern clearCapture() {
			TokenPattern n = pattern.clearCapture();
			if(n == pattern) return this;
			return new Loop(n, order);
		}

		@Override
		public TokenPattern loop(Comparator<Result> order) {
			if(this.order == order) return this;
			return new Loop(pattern, this.order.thenComparing(order));
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			TokenPattern n = pattern.replace(replacer);
			if(n == pattern) return this;
			return new Loop(n, order);
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			TokenPattern n = pattern.replaceCaptures(previous);
			if(n == pattern) return this;
			return new Loop(n, order);
		}
	}

	record Omittable(TokenPattern pattern, Comparator<Result> order) implements TokenPattern {
		@Override
		public Stream<Result> matches(TokenString src) {
			return Stream.concat(pattern.matches(src), Stream.of(Result.EMPTY)).sorted(order);
		}

		@Override
		public TokenPattern clearCapture() {
			TokenPattern n = pattern.clearCapture();
			if(n == pattern) return this;
			return new Omittable(n, order);
		}

		@Override
		public TokenPattern omittable(Comparator<Result> order) {
			if(order == this.order) return this;
			return new Omittable(pattern, this.order.thenComparing(order));
		}

		@Override
		public TokenPattern loop(Comparator<Result> order) {
			return pattern.loop(order).omittable(this.order);
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			TokenPattern n = pattern.replace(replacer);
			if(n == pattern) return this;
			return new Omittable(n, order);
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			TokenPattern n = pattern.replaceCaptures(previous);
			if(n == pattern) return this;
			return new Omittable(n, order);
		}
	}

	record Concat(List<? extends TokenPattern> patterns) implements TokenPattern {
		@Override
		public Stream<Result> matches(TokenString src) {
			Stream<Result> results = Stream.of(Result.EMPTY);
			for(TokenPattern p:patterns) {
				final TokenPattern fp = p;
				results = results.flatMap(r -> fp.replaceCaptures(r).matches(src.substring(r.matchLength, src.length())).map(r::concat));
			}
			return results;
		}

		@Override
		public TokenPattern clearCapture() {
			List<TokenPattern> n = patterns.stream().map(TokenPattern::clearCapture).toList();
			Iterator<? extends TokenPattern> a = patterns.iterator();
			Iterator<? extends TokenPattern> b = n.iterator();
			while(a.hasNext()) {
				if(a.next() != b.next()) return new Concat(n);
			}
			return this;
		}

		@Override
		public TokenPattern concat(TokenPattern p) {
			List<TokenPattern> combine = new ArrayList<>(patterns);
			if(p instanceof Concat c) combine.addAll(c.patterns);
			else combine.add(p);
			return new Concat(Collections.unmodifiableList(combine));
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			List<? extends TokenPattern> n = patterns.stream().map(p -> p.replace(replacer)).toList();
			Iterator<? extends TokenPattern> a = patterns.iterator();
			Iterator<? extends TokenPattern> b = n.iterator();
			while(a.hasNext() && b.hasNext()) {
				if(a.next() != b.next()) return new Concat(n);
			}
			if(a.hasNext() || b.hasNext()) return new Concat(n);
			return this;
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			List<? extends TokenPattern> n = patterns.stream().map(p -> p.replaceCaptures(previous)).toList();
			Iterator<? extends TokenPattern> a = patterns.iterator();
			Iterator<? extends TokenPattern> b = n.iterator();
			while(a.hasNext() && b.hasNext()) {
				if(a.next() != b.next()) return new Concat(n);
			}
			if(a.hasNext() || b.hasNext()) return new Concat(n);
			return this;
		}
	}

	record Conjunction(Collection<? extends TokenPattern> patterns) implements TokenPattern {
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
				if(a.next() != b.next()) return new Conjunction(n);
			}
			return this;
		}

		@Override
		public TokenPattern or(TokenPattern p) {
			Collection<TokenPattern> pat = new HashSet<>(patterns);
			if(p instanceof Conjunction c) pat.addAll(c.patterns);
			else pat.add(p);
			return new Conjunction(Collections.unmodifiableCollection(pat));
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			List<? extends TokenPattern> n = patterns.stream().map(p -> p.replace(replacer)).toList();
			Iterator<? extends TokenPattern> a = patterns.iterator();
			Iterator<? extends TokenPattern> b = n.iterator();
			while(a.hasNext() && b.hasNext()) {
				if(a.next() != b.next()) return new Conjunction(n);
			}
			if(a.hasNext() || b.hasNext()) return new Conjunction(n);
			return this;
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			List<? extends TokenPattern> n = patterns.stream().map(p -> p.replaceCaptures(previous)).toList();
			Iterator<? extends TokenPattern> a = patterns.iterator();
			Iterator<? extends TokenPattern> b = n.iterator();
			while(a.hasNext() && b.hasNext()) {
				if(a.next() != b.next()) return new Conjunction(n);
			}
			if(a.hasNext() || b.hasNext()) return new Conjunction(n);
			return this;
		}
	}

	record Single(Predicate<? super Token> pred) implements TokenPattern {
		public static final Single STRING = new Single(r -> r.type() == Token.Type.STRING),
				TAB = new Single(r -> r.type() == Token.Type.TAB),
				WORD = new Single(r -> r.type() == Token.Type.WORD),
				PUNCTUATION = new Single(r -> r.type() == Token.Type.PUNCTUATION);
		@Override
		public Stream<Result> matches(TokenString src) {
			if(src.isEmpty() || !pred.test(src.get(0))) return Stream.empty();
			return Stream.of(new Result(1));
		}

		@Override
		public TokenPattern clearCapture() {
			return this;
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			return this;
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			return this;
		}
	}

	record Capture(TokenPattern pattern, Set<String> name) implements TokenPattern {
		@Override
		public Stream<Result> matches(TokenString src) {
			return pattern.matches(src).map(r -> r.mergeCapture(name, src.substring(0,r.matchLength)));
		}

		@Override
		public TokenPattern clearCapture() {
			return pattern.clearCapture();
		}

		@Override
		public TokenPattern capture(Collection<String> name) {
			Set<String> n = new HashSet<>(this.name);
			n.addAll(name);
			return new Capture(pattern, Collections.unmodifiableSet(n));
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			TokenPattern n = pattern.replace(replacer);
			if(n == pattern) return this;
			return new Capture(n, name);
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			TokenPattern n = pattern.replaceCaptures(previous);
			if(n == pattern) return this;
			return new Capture(n, name);
		}
	}

	record Lookahead(TokenPattern pattern, boolean invert) implements TokenPattern {
		@Override
		public Stream<Result> matches(TokenString src) {
			if(pattern.matches(src).findAny().isPresent() != invert) return Stream.of(Result.EMPTY);
			return Stream.empty();
		}

		@Override
		public TokenPattern clearCapture() {
			TokenPattern n = pattern.clearCapture();
			if(n == pattern) return this;
			return new Lookahead(n, invert);
		}

		@Override
		public TokenPattern lookahead(boolean invert) {
			if(invert) return new Lookahead(pattern, !this.invert);
			return this;
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			TokenPattern n = pattern.replace(replacer);
			if(n == pattern) return this;
			return new Lookahead(n, invert);
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			TokenPattern n = pattern.replaceCaptures(previous);
			if(n == pattern) return this;
			return new Lookahead(n, invert);
		}
	}

	record Replacement(String name, boolean cc) implements TokenPattern {
		@Override
		public Stream<Result> matches(TokenString src) {
			throw new IllegalStateException("Trying to evaluate unreplaced replacement pattern: "+name);
		}

		@Override
		public TokenPattern clearCapture() {
			if(cc) return this;
			return new Replacement(name, true);
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			TokenPattern pat = replacer.apply(name);
			if(pat == null) pat = this;
			else if(cc) pat = pat.clearCapture();
			return pat;
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			return this;
		}
	}

	record CaptureReplacement(String capture, Function<? super TokenString, ? extends TokenPattern> replace, boolean cc) implements TokenPattern {
		@Override
		public Stream<Result> matches(TokenString src) {
			throw new IllegalStateException("Trying to evaluate unreplaced capture replacement pattern: "+capture);
		}

		@Override
		public TokenPattern clearCapture() {
			if(cc) return this;
			return new CaptureReplacement(capture, replace, true);
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			return this;
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			Optional<TokenPattern> pat = previous.capOpt(capture).map(replace);
			if(cc) pat = pat.map(TokenPattern::clearCapture);
			return pat.orElse(this);
		}
	}
	
	TokenPattern END = new TokenPattern() {
		@Override
		public Stream<Result> matches(TokenString src) {
			if(src.isEmpty()) return Stream.of(Result.EMPTY);
			return Stream.empty();
		}

		@Override
		public TokenPattern clearCapture() {
			return this;
		}

		@Override
		public TokenPattern replace(Function<? super String, ? extends TokenPattern> replacer) {
			return this;
		}
		
		@Override
		public TokenPattern replaceCaptures(Result previous) {
			return this;
		}
	};
}
