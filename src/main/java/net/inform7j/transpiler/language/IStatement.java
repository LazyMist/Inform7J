package net.inform7j.transpiler.language;

import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import net.inform7j.Logging;
import net.inform7j.Logging.Severity;
import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.Statistics;
import net.inform7j.transpiler.language.impl.deferring.RawBlockStatement;
import net.inform7j.transpiler.language.impl.deferring.RawLineStatement;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IStatement {
	public TokenString raw();
	public boolean isBlank();
	public long line();
	public Path src();
	public Source source();
	public String toString(String indent);
	public List<? extends IStatement> blockContents();
	public class StatementSupplier {
		private final ListIterator<? extends IStatement> iter;
		private int advance = 0;
		
		public StatementSupplier(ListIterator<? extends IStatement> iter) {
			this.iter = iter;
		}
		
		public <T extends IStatement> Optional<T> getNextOptional(Class<T> clazz, boolean important) {
			if(!iter.hasNext()) {
				if(important) Logging.log(Statistics.BAD_BODY_STATEMENTS, "Statement requested when none are available.");
				return Optional.empty();
			}
			IStatement st = iter.next();
			try {
				Optional<T> ret = Optional.of(clazz.cast(st));
				advance++;
				return ret;
			} catch(ClassCastException ex) {
				if(important) {
					Logging.log(Statistics.BAD_BODY_STATEMENTS, "Tried to get %s, got %s @%d in %s", clazz.getCanonicalName(), st.getClass().getCanonicalName(), st.line(), st.src());
					if(st instanceof RawLineStatement rl) Logging.log(Severity.WARN, "%s", rl.toString(""));
					else if(st instanceof RawBlockStatement rb) Logging.log(Severity.WARN, "%s", rb.toString(""));
				}
				iter.previous();
				return Optional.empty();
			}
		}

		public <T extends IStatement> Optional<T> getNextOptional(Class<T> clazz) {
			return getNextOptional(clazz, false);
		}

		public <T extends IStatement> T getNext(Class<T> clazz) {
			return getNextOptional(clazz, true).get();
		}

		public void reverse() throws IllegalStateException {
			if(advance==0) throw new IllegalStateException("Cannot reverse at the beginning");
			iter.previous();
			advance--;
		}

		public void commit() {
			advance = 0;
		}
	}
}
