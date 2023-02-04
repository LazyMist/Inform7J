package net.inform7j.transpiler.language.rules;

import net.inform7j.transpiler.language.IAction;

public interface IActionRule extends IRule {
	enum ActionTrigger {
		CHECK,
		PRE,
		EXECUTE,
		INSTEAD,
		POST
	}
	
	ActionTrigger trigger();
	IAction action();
}
