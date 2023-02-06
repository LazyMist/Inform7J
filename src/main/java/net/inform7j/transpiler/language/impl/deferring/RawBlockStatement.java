package net.inform7j.transpiler.language.impl.deferring;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.tokenizer.TokenString;

public record RawBlockStatement(List<? extends IStatement> blockContents) implements IStatement {
    @Override
    public TokenString raw() {
        return null;
    }
    
    @Override
    public boolean isBlank() {
        return blockContents.stream().allMatch(IStatement::isBlank);
    }
    
    @Override
    public long line() {
        return blockContents.get(0).line();
    }
    
    @Override
    public Path src() {
        return blockContents.get(0).src();
    }
    
    @Override
    public Source source() {
        return blockContents.get(0).source();
    }
    
    @Override
    public String toString(String indent) {
        return blockContents.stream().map(s -> s.toString(indent + "\t")).collect(Collectors.joining("\n"));
    }
    
    public static class Builder implements Supplier<RawBlockStatement>, Consumer<Supplier<? extends IStatement>> {
        private final LinkedList<Supplier<? extends IStatement>> contents = new LinkedList<>();
        
        @Override
        public RawBlockStatement get() {
            List<? extends IStatement> l = contents.stream().map(Supplier::get).toList();
            if(l.size() == 1 && l.get(0) instanceof RawBlockStatement r) return r;
            return new RawBlockStatement(l);
        }
        
        @Override
        public void accept(Supplier<? extends IStatement> arg0) {
            contents.add(arg0);
        }
        
        @Override
        public String toString() {
            return "Builder [contents=" + contents + "]";
        }
    }
}
