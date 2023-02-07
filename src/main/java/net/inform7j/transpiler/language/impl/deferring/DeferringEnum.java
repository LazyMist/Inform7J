package net.inform7j.transpiler.language.impl.deferring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IEnum;
import net.inform7j.transpiler.tokenizer.Replacement;
import net.inform7j.transpiler.tokenizer.Result;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;

public class DeferringEnum extends DeferringImpl implements IEnum {
    public static final String CAPTURE_NAME = "name";
    public static final String CAPTURE_VALUES = "values";
    public static final List<Parser<DeferringEnum>> PARSERS = Collections.unmodifiableList(Arrays.asList(
        new Parser<>(
            TokenPattern.quoteIgnoreCase("the")
                .concat(WORD_LOOP.concat(PLURAL_WORD).capture(CAPTURE_NAME))
                .concatIgnoreCase("are")
                .concat(WORD_LOOP.capture(CAPTURE_VALUES))
                .concat(
                    TokenPattern.quote(",").orIgnoreCase("and").orIgnoreCase(", and")
                        .concat(WORD_LOOP.capture(CAPTURE_VALUES)).loop()
                )
                .concat(ENDMARKER)
            /*Pattern.compile("The (?<name>.+?)s are (?<values>.+?)\\.\\s*+", Pattern.CASE_INSENSITIVE)*/,
            DeferringEnum::Kind),
        new Parser<>(
            AN.concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(CAPTURE_NAME))
                .concat(TokenPattern.quoteIgnoreCase("can be").orIgnoreCase("is either"))
                .concat(WORD_LOOP.capture(CAPTURE_VALUES))
                .concat(
                    TokenPattern.quote(",").orIgnoreCase("or").orIgnoreCase(",or")
                        .concat(WORD_LOOP.capture(CAPTURE_VALUES)).loop()
                )
                .concat(ENDMARKER)
            /*Pattern.compile("(?<name>.+?) can be (?<values>.+?)\\.\\s*+", Pattern.CASE_INSENSITIVE)*/,
            DeferringEnum::Property),
        new Parser<>(
            WORD_LOOP.capture(CAPTURE_NAME)
                .concat(TokenPattern.quoteIgnoreCase("can be").orIgnoreCase("is either"))
                .concat(WORD_LOOP.capture(CAPTURE_VALUES))
                .concat(
                    TokenPattern.quote(",").orIgnoreCase("or").orIgnoreCase(",or")
                        .concat(WORD_LOOP.capture(CAPTURE_VALUES)).loop()
                )
                .concat(ENDMARKER)
            /*Pattern.compile("(?<name>.+?) can be (?<values>.+?)\\.\\s*+", Pattern.CASE_INSENSITIVE)*/,
            DeferringEnum::Variable)
    ));
    
    //public static final Pattern AND_LIST = Pattern.compile("\\s*+(?:,|and|or)\\s*+", Pattern.CASE_INSENSITIVE);
    
    private final Category CATEGORY;
    private final TokenString NAME;
    private final List<TokenString> VALUES;
    
    public DeferringEnum(
        DeferringStory story,
        Source source,
        Category category,
        TokenString name,
        List<TokenString> values
    ) {
        super(story, source);
        CATEGORY = category;
        NAME = name;
        VALUES = Collections.unmodifiableList(new ArrayList<>(values));
    }
    
    public DeferringEnum(ParseContext ctx, Category category) {
        super(ctx);
        CATEGORY = category;
        final Result r = ctx.result();
        NAME = r.cap(CAPTURE_NAME);
        VALUES = Collections.unmodifiableList(new ArrayList<>(r.capMulti(CAPTURE_VALUES)));
    }
    
    public static DeferringEnum Kind(ParseContext ctx) {
        return new DeferringEnum(ctx, Category.KIND);
    }
    public static DeferringEnum Variable(ParseContext ctx) {
        return new DeferringEnum(ctx, Category.OBJECT);
    }
    public static DeferringEnum Property(ParseContext ctx) {
        return new DeferringEnum(ctx, Category.PROPERTY);
    }
    
    @Override
    public Category category() {
        return CATEGORY;
    }
    
    @Override
    public TokenString name() {
        return NAME;
    }
    
    @Override
    public Stream<TokenString> streamValues() {
        return VALUES.stream();
    }
    
}
