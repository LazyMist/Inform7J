package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStory;

import java.util.stream.Stream;

public class DeferringPredicate extends DeferringFunction {
    public DeferringPredicate(
        DeferringStory story,
        Source source,
        Stream<? extends SignatureElement> params,
        IStatement body
    ) {
        super(story, source, IStory.BaseKind.TRUTH_STATE, params, body);
    }
}
