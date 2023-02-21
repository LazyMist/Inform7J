package net.inform7j.transpiler.parser.provider;

import net.inform7j.transpiler.language.impl.deferring.DeferringAlias;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.SimpleCombinedParser;
import net.inform7j.transpiler.tokenizer.Replacement;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.pattern.Single;

import java.util.stream.Stream;

import static net.inform7j.transpiler.parser.Patterns.*;

public class AliasParserProvider implements CombinedParser.Provider {
    public static final String CAPTURE_ALIAS = "alias";
    public static final String CAPTURE_ORIGINAL = "original";
    private static final TokenPattern PATTERN_PREFIX = TokenPattern.quoteIgnoreCase("understand")
        .concat(Single.STRING.capture(CAPTURE_ALIAS))
        .concat(TokenPattern.quoteIgnoreCase("and").orIgnoreCase("or").concat(Single.STRING.capture(
            CAPTURE_ALIAS)).loop().omittable())
        .concatIgnoreCase("as");
    
    public static TokenPattern getAliasPattern(String replacement, String... replacements) {
        TokenPattern repl = new Replacement(replacement, false);
        for(String s : replacements) {
            repl = repl.or(new Replacement(s, false));
        }
        return PATTERN_PREFIX.concat(repl.capture(CAPTURE_ORIGINAL))
            .concat(ENDMARKER);
    }
    private static final SimpleCombinedParser<DeferringAlias> PARSER = new SimpleCombinedParser<>(
        11,
        getAliasPattern(DeferringStory.ACTION_NAME_REPLACEMENT, DeferringStory.OBJECT_NAME_REPLACEMENT),
        DeferringStory::replace,
        ctx -> new DeferringAlias(
            ctx.story(),
            ctx.source().source(),
            ctx.result().capMulti(CAPTURE_ALIAS),
            ctx.result().cap(CAPTURE_ORIGINAL)
        ),
        DeferringStory::addAlias
    );
    @Override
    public Stream<? extends CombinedParser> get() {
        return Stream.of(PARSER);
    }
}
