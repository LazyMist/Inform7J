module inform7J {
	requires java.desktop;
	
	exports net.inform7j.transpiler;
	exports net.inform7j.transpiler.tokenizer;
	exports net.inform7j.transpiler.language;
	exports net.inform7j.transpiler.language.rules;
	
	exports net.inform7j.transpiler.language.impl.deferring to inform7J.tests;
	exports net.inform7j.transpiler.language.impl.deferring.rules to inform7J.tests;
}