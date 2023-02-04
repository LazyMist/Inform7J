package net.inform7j.transpiler.language.rules;

import net.inform7j.transpiler.language.IAction;

public interface IActionRule extends IRule {
	public static enum ActionTrigger {
		CHECK,
		PRE,
		EXECUTE,
		INSTEAD,
		POST;
	}
	
	public ActionTrigger trigger();
	public IAction action();
}
