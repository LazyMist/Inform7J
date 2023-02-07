package net.inform7j.transpiler.language.impl.deferring;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.IntakeReader;
import net.inform7j.transpiler.language.IFunction;
import net.inform7j.transpiler.language.IKind;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.tokenizer.*;
import net.inform7j.transpiler.tokenizer.pattern.End;
import net.inform7j.transpiler.tokenizer.pattern.Single;
import net.inform7j.transpiler.util.StatementSupplier;
import net.inform7j.transpiler.language.IStory.BaseKind;

@Slf4j
public class DeferringFunction extends DeferringImpl implements IFunction {
    public record DeferredParameter(DeferringStory story, Source source, TokenString name, TokenString kindName)
        implements ParameterElement {
        public static final String CAPTURE_TYPE = "type";
        public static final TokenPattern PATTERN = TokenPattern.quote("(")
            .concat(WORD_LOOP.capture(CAPTURE_NAME))
            .concat("-").concat(AN.omittable())
            .concat(WORD_LOOP.capture(CAPTURE_TYPE))
            .concat(")");
        //Pattern.compile("\\((?<name>\\w+) - an? (?<type>[-\\w\\s]+)\\)", Pattern.CASE_INSENSITIVE)
        public DeferredParameter(ParseContext ctx) {
            this(ctx.story(), ctx.source().source(), ctx.result().cap(CAPTURE_NAME), ctx.result().cap(CAPTURE_TYPE));
        }
        @Override
        public String toSignatureString() {
            return "$" + kindName + "$";
        }
        @Override
        public DeferringKind kind() {
            return story.getKind(kindName);
        }
    }
    
    protected static Stream<SignatureElement> parseSignatures(ParseContext ctx, List<TokenString> names) {
        Stream.Builder<SignatureElement> name = Stream.builder();
        for(TokenString e : names) {
            Optional<Result> results = DeferredParameter.PATTERN.concat(End.PATTERN).matches(e).findFirst();
			if(results.isEmpty()) {
				results = NAME_GLOB.concat(End.PATTERN).matches(e).findFirst();
				if(results.isEmpty()) throw new RuntimeException("Not a nameElement");
				name.accept(new NameElement(results.get().capMulti(CAPTURE_NAME).stream()
					.map(l -> l.stream().map(Token::content).collect(Collectors.joining(" ")))
					.collect(Collectors.toUnmodifiableSet())));
			} else {
				name.accept(new DeferredParameter(new ParseContext(ctx, results.get())));
			}
        }
        return name.build();
    }
    
    public static IStatement getNextBody(StatementSupplier sup) {
        IStatement ret = sup.getNext(IStatement.class);
        while(ret instanceof RawLineStatement line && line.isBlank()) ret = sup.getNext(IStatement.class);
        return ret;
    }
    
    public static final String CAPTURE_NAME = "name";
    public static final TokenPattern NAME_GLOB = Single.WORD.capture(CAPTURE_NAME)
        .concat(TokenPattern.quote("/")
            .concat(Single.WORD.or("--").capture(CAPTURE_NAME))
            .loop()
            .omittable())
        .or(Single.PUNCTUATION.capture(CAPTURE_NAME));
    public static final TokenPattern PARAM_GLOB = NAME_GLOB.or(DeferredParameter.PATTERN).clearCapture().capture(
        CAPTURE_NAME);
    public static final TokenPattern WHICH = new Single(new TokenPredicate(Pattern.compile(
        "which|what",
        Pattern.CASE_INSENSITIVE
    )));
    //"(?:[^()\\s]+ ?|\\(.+? -(?: an?)? .+?\\) ?)"
    
    public static final String CAPTURE_RETURN_TYPE = "returnType";
    public static final String CAPTURE_NAME_PARAMS = "nameParams";
    public static final List<Parser<DeferringFunction>> PARSERS = Collections.unmodifiableList(Arrays.asList(
        new Parser<>(
            TokenPattern.quoteIgnoreCase("to decide").concat(WHICH)
                .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(
                    CAPTURE_RETURN_TYPE))
                .concat("is")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(":")
                .concat(ENDLINE)
            /*Pattern.compile("^to decide (?:which|what) (?<returnType>.+?) is (?<nameParams>"+PARAM_GLOB+"+?):\\s*$", Pattern.CASE_INSENSITIVE)*/,
            DeferringFunction::new),
        new Parser<>(
            TokenPattern.quoteIgnoreCase("to decide").concat(WHICH)
                .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(
                    CAPTURE_RETURN_TYPE))
                .concat("is")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(": (-")
                .concat(ENDLINE),
            DeferringFunction::Raw
        ),
        new Parser<>(
            TokenPattern.quoteIgnoreCase("to decide").concat(WHICH)
                .concat(new Replacement(DeferringStory.KIND_NAME_REPLACEMENT, false).capture(
                    CAPTURE_RETURN_TYPE))
                .concat("is")
                .concat(PARAM_GLOB.capture(CAPTURE_NAME_PARAMS).loop())
                .concat(": (-")
                .concat(NOT_ENDMARKER_LOOP)
                .concat("-)")
                .concat(ENDMARKER),
            DeferringFunction::RawClosed
        )
    ));
    
    public final TokenString RETURN_TYPE;
    public final List<? extends SignatureElement> NAME;
    public final IStatement BODY;
    public DeferringFunction(
        DeferringStory story, Source source, TokenString returnType, Stream<? extends SignatureElement> name,
        IStatement bODY
    ) {
        super(story, source);
        RETURN_TYPE = returnType;
        NAME = name.toList();
        BODY = bODY;
    }
    public DeferringFunction(
        DeferringStory story, Source source, BaseKind returnType, Stream<? extends SignatureElement> name,
        IStatement bODY
    ) {
        this(story, source, returnType.writtenName, name, bODY);
    }
    public DeferringFunction(ParseContext ctx, TokenString returnType, IStatement body) {
        super(ctx);
        final Result m = ctx.result();
        RETURN_TYPE = returnType;
        NAME = parseSignatures(ctx, m.capMulti(CAPTURE_NAME_PARAMS)).toList();
        BODY = body;
    }
    public DeferringFunction(ParseContext ctx, TokenString returnType) {
        this(ctx, returnType, getNextBody(ctx.supplier()));
    }
    public DeferringFunction(ParseContext ctx, BaseKind returnType, IStatement body) {
        this(ctx, returnType.writtenName, body);
    }
    public DeferringFunction(ParseContext ctx, BaseKind returnType) {
        this(ctx, returnType.writtenName);
    }
    public DeferringFunction(ParseContext ctx, IStatement body) {
        this(ctx, ctx.result().cap(CAPTURE_RETURN_TYPE), body);
    }
    public DeferringFunction(ParseContext ctx) {
        this(ctx, getNextBody(ctx.supplier()));
    }
    
    public static DeferringFunction Raw(ParseContext ctx) {
        StatementSupplier sup = ctx.supplier();
        while(true) {
            Optional<? extends IStatement> opt = sup.getNextOptional(IStatement.class);
            if(opt.isPresent()) {
                if(IntakeReader.tailMatch(opt.get(), IntakeReader.RAW_END, true)) break;
            } else {
                log.error("Unclosed Raw block starting in: {}@{}", ctx.source().src(), ctx.source().line());
            }
        }
        return RawClosed(ctx);
    }
    
    public static DeferringFunction RawClosed(ParseContext ctx) {
        return new DeferringFunction(ctx, (IStatement) null);
    }
    
    @Override
    public Stream<? extends DeferringKind> streamParameters() {
        return streamName().map(s -> (s instanceof DeferredParameter p) ? p.kind() : null).filter(Objects::nonNull);
    }
    
    @Override
    public Stream<? extends SignatureElement> streamName() {
        return NAME.stream();
    }
    
    @Override
    public IKind returnType() {
        return story.getKind(RETURN_TYPE);
    }
    @Override
    public IStatement body() {
        return BODY;
    }
}
