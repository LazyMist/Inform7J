package net.inform7j.transpiler.language;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.nio.file.Path;
import java.util.List;

public interface IStatement {
	TokenString raw();
	boolean isBlank();
	long line();
	Path src();
	Source source();
	String toString(String indent);
	List<? extends IStatement> blockContents();
}
