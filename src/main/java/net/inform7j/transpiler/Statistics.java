package net.inform7j.transpiler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
public enum Statistics {
	ERRORS(Logger::atError),
	UNKNOWN_LINES(ERRORS),
	BAD_BODY_STATEMENTS(ERRORS),
	ERROR_EXCEPTIONS(ERRORS),
	INCOMPLETE_INCLUDE(ERROR_EXCEPTIONS),
	UNPROCESSED_BLOCK(ERRORS),
	MISSING_FILES(Logger::atError),
	WARNINGS(Logger::atWarn),
	INCOMPLETE_EOF_LINE(WARNINGS),
	INCOMPLETE_LINES(WARNINGS),
	ELEMENTS(Logger::atTrace),
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
	LINES(Logger::atDebug);
	private final Optional<Statistics> parent;
	private final Function<Logger, LoggingEventBuilder> logSeverity;
	private int count = 0;
	
	Statistics(Function<Logger, LoggingEventBuilder> severity) {
		this.logSeverity = severity;
		this.parent = Optional.empty();
	}
	Statistics(Statistics parent) {
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
	public LoggingEventBuilder prepareLog(Logger log) {
		return increment().logSeverity.apply(log);
	}
	public static void printStats(Function<Logger, LoggingEventBuilder> severity) {
		for(Statistics s:Statistics.values()) {
			severity.apply(log).log("{}: {}\n", s.name(), s.count());
		}
	}
}
