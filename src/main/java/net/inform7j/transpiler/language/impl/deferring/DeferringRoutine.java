package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStory.BaseKind;

import java.util.stream.Stream;

public class DeferringRoutine extends DeferringFunction {
    public DeferringRoutine(
        DeferringStory story,
        Source source,
        Stream<? extends SignatureElement> params,
        IStatement body
    ) {
        super(story, source, BaseKind.VOID, params, body);
    }
}
