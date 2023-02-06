package net.inform7j.transpiler.util;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.Statistics;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.impl.deferring.RawBlockStatement;
import net.inform7j.transpiler.language.impl.deferring.RawLineStatement;

import java.util.ListIterator;
import java.util.Optional;

@Slf4j
public class StatementSupplier {
    private final ListIterator<? extends IStatement> iter;
    private int advance = 0;
    
    public StatementSupplier(ListIterator<? extends IStatement> iter) {
        this.iter = iter;
    }
    
    public <T extends IStatement> Optional<T> getNextOptional(Class<T> clazz, boolean important) {
        if(!iter.hasNext()) {
            if(important) {
                Statistics.BAD_BODY_STATEMENTS.prepareLog(log).log(
                    "Statement requested when none are available.");
            }
            return Optional.empty();
        }
        IStatement st = iter.next();
        try {
            Optional<T> ret = Optional.of(clazz.cast(st));
            advance++;
            return ret;
        } catch(ClassCastException ex) {
            if(important) {
                Statistics.BAD_BODY_STATEMENTS.prepareLog(log).log(
                    "Tried to get {}, got {} @{} in {}",
                    clazz.getCanonicalName(),
                    st.getClass().getCanonicalName(),
                    st.line(),
                    st.src()
                );
                if(st instanceof RawLineStatement rl) {
                    log.warn(rl.toString(""));
                } else if(st instanceof RawBlockStatement rb) log.warn(rb.toString(""));
            }
            iter.previous();
            return Optional.empty();
        }
    }
    
    public <T extends IStatement> Optional<T> getNextOptional(Class<T> clazz) {
        return getNextOptional(clazz, false);
    }
    
    public <T extends IStatement> T getNext(Class<T> clazz) {
        return getNextOptional(clazz, true).orElseThrow();
    }
    
    public void reverse() throws IllegalStateException {
        if(advance == 0) throw new IllegalStateException("Cannot reverse at the beginning");
        iter.previous();
        advance--;
    }
    
    public void commit() {
        advance = 0;
    }
}
