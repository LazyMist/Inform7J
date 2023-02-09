package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;

import java.util.stream.Stream;

public class DeferringPrint extends DeferringRoutine {
    public DeferringPrint(
        DeferringStory story,
        Source source,
        Stream<? extends SignatureElement> params,
        IStatement body
    ) {
        super(story, source, params, body);
    }
    
}
