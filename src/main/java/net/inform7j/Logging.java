package net.inform7j;

import java.io.PrintStream;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.inform7j.transpiler.Statistics;

public class Logging {
	public static enum Severity {
		DEBUG,
		INFO,
		WARN,
		ERROR,
		FATAL;
	}
	
	public static Severity MINIMUM_SEVERITY = Severity.WARN;
	public static PrintStream OUT = System.out;
	
	public static boolean log_assert(boolean assertion, Severity severity, String failformat, Object ...objects) {
		if(!assertion) log(severity, failformat, objects);
		return assertion;
	}
	
	public static <T> T debug_log(T obj) {
		log(Severity.DEBUG, "%s", obj);
		return obj;
	}
	public static <T> T debug_log(Function<? super T, ?> transformer, T obj) {
		log(Severity.DEBUG, "%s", transformer.apply(obj));
		return obj;
	}
	public static <T> UnaryOperator<? extends T> transformed_debug_log(Function<? super T,?> transformer) {
		return t -> debug_log(transformer, t);
	}
	
	public static void log(Severity severity, String format, Object ...objects) {
		if(MINIMUM_SEVERITY.compareTo(severity) > 0) return;
		OUT.printf("%s: ", severity);
		OUT.printf(format, objects);
		OUT.println();
	}
	
	public static void log(Statistics stat, String format, Object ...objects) {
		stat.increment();
		if(MINIMUM_SEVERITY.compareTo(stat.logSeverity) > 0) return;
		OUT.printf("%s: %s: ", stat.logSeverity, stat);
		OUT.printf(format, objects);
		OUT.println();
	}
	
	public static void log(Severity severity, Throwable thrown) {
		if(MINIMUM_SEVERITY.compareTo(severity) > 0) return;
		OUT.printf("%s: ", severity);
		thrown.printStackTrace(OUT);
		OUT.println();
	}
	
	public static void log(Statistics stat, Throwable thrown) {
		stat.increment();
		if(MINIMUM_SEVERITY.compareTo(stat.logSeverity) > 0) return;
		OUT.printf("%s: %s: ", stat.logSeverity, stat);
		thrown.printStackTrace(OUT);
		OUT.println();
	}
}
