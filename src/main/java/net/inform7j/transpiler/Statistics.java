package net.inform7j.transpiler;

import java.io.PrintStream;
import java.util.Optional;

import net.inform7j.Logging.Severity;

public enum Statistics {
	ERRORS(Severity.ERROR),
	UNKNOWN_LINES(ERRORS),
	BAD_BODY_STATEMENTS(ERRORS),
	ERROR_EXCEPTIONS(ERRORS),
	INCOMPLETE_INCLUDE(ERROR_EXCEPTIONS),
	UNPROCESSED_BLOCK(ERRORS),
	MISSING_FILES(Severity.FATAL),
	WARNINGS(Severity.WARN),
	INCOMPLETE_EOF_LINE(WARNINGS),
	INCOMPLETE_LINES(WARNINGS),
	ELEMENTS(Severity.DEBUG),
	KINDS(ELEMENTS),
	OBJECTS(ELEMENTS),
	PROPERTIES(ELEMENTS),
	FUNCTIONS(ELEMENTS),
	ACTIONS(ELEMENTS),
	RULES(ELEMENTS),
	TABLES(ELEMENTS),
	CONTINUED_TABLES(ELEMENTS),
	VALUES(ELEMENTS),
	ALIASES(ELEMENTS),
	LINES(Severity.DEBUG);
	private final Optional<Statistics> parent;
	public final Severity logSeverity;
	private int count = 0;
	
	private Statistics(Severity severity) {
		this.logSeverity = severity;
		this.parent = Optional.empty();
	}
	private Statistics(Statistics parent) {
		this.logSeverity = parent.logSeverity;
		this.parent = Optional.of(parent);
	}
	
	public int count() {
		return count;
	}
	public Statistics increment(int amount) {
		parent.ifPresent(s -> s.increment(amount));
		count += amount;
		return this;
	}
	public Statistics decrement(int amount) {
		parent.ifPresent(s -> s.decrement(amount));
		count -= amount;
		return this;
	}
	public Statistics increment() {
		return increment(1);
	}
	public Statistics decrement() {
		return decrement(1);
	}
	
	public static void printStats(PrintStream out) {
		for(Statistics s:Statistics.values()) {
			out.printf("%s: %d\n", s.name(), s.count());
		}
	}
}
