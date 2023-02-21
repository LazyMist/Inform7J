package net.inform7j.transpiler.language.impl.deferring.rules;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.rules.IConditionedActionRule;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.List;
import java.util.Optional;

public final class DeferringConditionedActionRule extends DeferringActionRule implements IConditionedActionRule {
    public final List<TokenString> condition;
    public DeferringConditionedActionRule(
        DeferringStory story,
        Source source,
        TokenString action,
        ActionTrigger trigger,
        Optional<TokenString> name,
        Optional<TokenString> condition,
        IStatement bODY
    ) {
        super(story, source, bODY, trigger, action, name);
        this.condition = condition.stream().toList();
    }
    
    @Override
    public List<TokenString> condition() {
        return condition;
    }
}
