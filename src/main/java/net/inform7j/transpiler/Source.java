package net.inform7j.transpiler;

import java.util.Optional;

import net.inform7j.transpiler.tokenizer.TokenString;

public sealed interface Source permits Source.Extension, Source.Story {
    class LazyString {
        private Optional<TokenString> opt = Optional.empty();
        public Optional<TokenString> get() {
            return opt;
        }
        public void set(TokenString name) {
            opt = opt.or(() -> Optional.of(name));
        }
    }
    
    record Extension(String author, String title, LazyString tokenName) implements Source {
        public Extension(String author, String title) {
            this(author, title, new LazyString());
        }
        @Override
        public String name() {
            return title + " by " + author;
        }
    }
    
    enum Story implements Source {
        MAIN;
    }
    String name();
}
